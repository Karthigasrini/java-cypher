package org.cypher.core.net;

import static org.cypher.core.config.Parameter.ChainConstant.BLOCK_PRODUCED_INTERVAL;

import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.cypher.common.backup.BackupServer;
import org.cypher.common.overlay.message.Message;
import org.cypher.common.overlay.server.ChannelManager;
import org.cypher.common.overlay.server.SyncPool;
import org.cypher.common.utils.Sha256Hash;
import org.cypher.core.ChainBaseManager;
import org.cypher.core.capsule.BlockCapsule;
import org.cypher.core.capsule.BlockCapsule.BlockId;
import org.cypher.core.capsule.PbftSignCapsule;
import org.cypher.core.capsule.TransactionCapsule;
import org.cypher.core.db.Manager;
import org.cypher.core.exception.AccountResourceInsufficientException;
import org.cypher.core.exception.BadBlockException;
import org.cypher.core.exception.BadItemException;
import org.cypher.core.exception.BadNumberBlockException;
import org.cypher.core.exception.ContractExeException;
import org.cypher.core.exception.ContractSizeNotEqualToOneException;
import org.cypher.core.exception.ContractValidateException;
import org.cypher.core.exception.DupTransactionException;
import org.cypher.core.exception.EventBloomException;
import org.cypher.core.exception.ItemNotFoundException;
import org.cypher.core.exception.NonCommonBlockException;
import org.cypher.core.exception.P2pException;
import org.cypher.core.exception.P2pException.TypeEnum;
import org.cypher.core.exception.ReceiptCheckErrException;
import org.cypher.core.exception.StoreException;
import org.cypher.core.exception.TaposException;
import org.cypher.core.exception.TooBigTransactionException;
import org.cypher.core.exception.TooBigTransactionResultException;
import org.cypher.core.exception.TransactionExpirationException;
import org.cypher.core.exception.UnLinkedBlockException;
import org.cypher.core.exception.VMIllegalException;
import org.cypher.core.exception.ValidateScheduleException;
import org.cypher.core.exception.ValidateSignatureException;
import org.cypher.core.exception.ZksnarkException;
import org.cypher.core.metrics.MetricsService;
import org.cypher.core.net.message.BlockMessage;
import org.cypher.core.net.message.MessageTypes;
import org.cypher.core.net.message.TransactionMessage;
import org.cypher.core.net.peer.PeerConnection;
import org.cypher.core.store.WitnessScheduleStore;
import org.cypher.protos.Protocol.Inventory.InventoryType;

@Slf4j(topic = "net")
@Component
public class CypherNetDelegate {

  @Autowired
  private SyncPool syncPool;

  @Autowired
  private ChannelManager channelManager;

  @Autowired
  private Manager dbManager;

  @Autowired
  private ChainBaseManager chainBaseManager;

  @Autowired
  private WitnessScheduleStore witnessScheduleStore;

  @Getter
  private Object blockLock = new Object();

  @Autowired
  private BackupServer backupServer;

  @Autowired
  private MetricsService metricsService;

  private volatile boolean backupServerStartFlag;

  private int blockIdCacheSize = 100;

  private long timeout = 1000;

  private Queue<BlockId> freshBlockId = new ConcurrentLinkedQueue<BlockId>() {
    @Override
    public boolean offer(BlockId blockId) {
      if (size() > blockIdCacheSize) {
        super.poll();
      }
      return super.offer(blockId);
    }
  };

  public void trustNode(PeerConnection peer) {
    channelManager.getTrustNodes().put(peer.getInetAddress(), peer.getNode());
  }

  public Collection<PeerConnection> getActivePeer() {
    return syncPool.getActivePeers();
  }

  public long getSyncBeginNumber() {
    return dbManager.getSyncBeginNumber();
  }

  public long getBlockTime(BlockId id) throws P2pException {
    try {
      return chainBaseManager.getBlockById(id).getTimeStamp();
    } catch (BadItemException | ItemNotFoundException e) {
      throw new P2pException(TypeEnum.DB_ITEM_NOT_FOUND, id.getString());
    }
  }

  public BlockId getHeadBlockId() {
    return chainBaseManager.getHeadBlockId();
  }

  public BlockId getSolidBlockId() {
    return chainBaseManager.getSolidBlockId();
  }

  public BlockId getGenesisBlockId() {
    return chainBaseManager.getGenesisBlockId();
  }

  public BlockId getBlockIdByNum(long num) throws P2pException {
    try {
      return chainBaseManager.getBlockIdByNum(num);
    } catch (ItemNotFoundException e) {
      throw new P2pException(TypeEnum.DB_ITEM_NOT_FOUND, "num: " + num);
    }
  }

  public BlockCapsule getGenesisBlock() {
    return chainBaseManager.getGenesisBlock();
  }

  public long getHeadBlockTimeStamp() {
    return chainBaseManager.getHeadBlockTimeStamp();
  }

  public boolean containBlock(BlockId id) {
    return chainBaseManager.containBlock(id);
  }

  public boolean containBlockInMainChain(BlockId id) {
    return chainBaseManager.containBlockInMainChain(id);
  }

  public List<BlockId> getBlockChainHashesOnFork(BlockId forkBlockHash) throws P2pException {
    try {
      return dbManager.getBlockChainHashesOnFork(forkBlockHash);
    } catch (NonCommonBlockException e) {
      throw new P2pException(TypeEnum.HARD_FORKED, forkBlockHash.getString());
    }
  }

  public boolean canChainRevoke(long num) {
    return num >= dbManager.getSyncBeginNumber();
  }

  public boolean contain(Sha256Hash hash, MessageTypes type) {
    if (type.equals(MessageTypes.BLOCK)) {
      return chainBaseManager.containBlock(hash);
    } else if (type.equals(MessageTypes.CYP)) {
      return dbManager.getTransactionStore().has(hash.getBytes());
    }
    return false;
  }

  public Message getData(Sha256Hash hash, InventoryType type) throws P2pException {
    try {
      switch (type) {
        case BLOCK:
          return new BlockMessage(chainBaseManager.getBlockById(hash));
        case CYP:
          TransactionCapsule tx = chainBaseManager.getTransactionStore().get(hash.getBytes());
          if (tx != null) {
            return new TransactionMessage(tx.getInstance());
          }
          throw new StoreException();
        default:
          throw new StoreException();
      }
    } catch (StoreException e) {
      throw new P2pException(TypeEnum.DB_ITEM_NOT_FOUND,
          "type: " + type + ", hash: " + hash.getByteString());
    }
  }

  public void processBlock(BlockCapsule block, boolean isSync) throws P2pException {
    BlockId blockId = block.getBlockId();
    synchronized (blockLock) {
      try {
        if (!freshBlockId.contains(blockId)) {
          if (block.getNum() <= getHeadBlockId().getNum()) {
            logger.warn("Receive a fork block {} witness {}, head {}",
                block.getBlockId().getString(),
                Hex.toHexString(block.getWitnessAddress().toByteArray()),
                getHeadBlockId().getString());
          }
          if (!isSync) {
            //record metrics
            metricsService.applyBlock(block);
          }
          dbManager.pushBlock(block);
          freshBlockId.add(blockId);
          logger.info("Success process block {}.", blockId.getString());
          if (!backupServerStartFlag
              && System.currentTimeMillis() - block.getTimeStamp() < BLOCK_PRODUCED_INTERVAL) {
            backupServerStartFlag = true;
            backupServer.initServer();
          }
        }
      } catch (ValidateSignatureException
          | ContractValidateException
          | ContractExeException
          | UnLinkedBlockException
          | ValidateScheduleException
          | AccountResourceInsufficientException
          | TaposException
          | TooBigTransactionException
          | TooBigTransactionResultException
          | DupTransactionException
          | TransactionExpirationException
          | BadNumberBlockException
          | BadBlockException
          | NonCommonBlockException
          | ReceiptCheckErrException
          | VMIllegalException
          | ZksnarkException
          | EventBloomException e) {
        metricsService.failProcessBlock(block.getNum(), e.getMessage());
        logger.error("Process block failed, {}, reason: {}.", blockId.getString(), e.getMessage());
        throw new P2pException(TypeEnum.BAD_BLOCK, e);
      }
    }
  }

  public void pushTransaction(TransactionCapsule cyp) throws P2pException {
    try {
      cyp.setTime(System.currentTimeMillis());
      dbManager.pushTransaction(cyp);
    } catch (ContractSizeNotEqualToOneException
        | VMIllegalException e) {
      throw new P2pException(TypeEnum.BAD_CYP, e);
    } catch (ContractValidateException
        | ValidateSignatureException
        | ContractExeException
        | DupTransactionException
        | TaposException
        | TooBigTransactionException
        | TransactionExpirationException
        | ReceiptCheckErrException
        | TooBigTransactionResultException
        | AccountResourceInsufficientException e) {
      throw new P2pException(TypeEnum.CYP_EXE_FAILED, e);
    }
  }

  public void validSignature(BlockCapsule block) throws P2pException {
    try {
      if (!block.validateSignature(dbManager.getDynamicPropertiesStore(),
              dbManager.getAccountStore())) {
        throw new P2pException(TypeEnum.BAD_BLOCK, "valid signature failed.");
      }
    } catch (ValidateSignatureException e) {
      throw new P2pException(TypeEnum.BAD_BLOCK, e);
    }
  }

  public boolean validBlock(BlockCapsule block) throws P2pException {
    long time = System.currentTimeMillis();
    if (block.getTimeStamp() - time > timeout) {
      throw new P2pException(TypeEnum.BAD_BLOCK,
              "time:" + time + ",block time:" + block.getTimeStamp());
    }
    validSignature(block);
    return witnessScheduleStore.getActiveWitnesses().contains(block.getWitnessAddress());
  }

  public PbftSignCapsule getBlockPbftCommitData(long blockNum) {
    return chainBaseManager.getPbftSignDataStore().getBlockSignData(blockNum);
  }

  public PbftSignCapsule getSRLPbftCommitData(long epoch) {
    return chainBaseManager.getPbftSignDataStore().getSrSignData(epoch);
  }

  public boolean allowPBFT() {
    return chainBaseManager.getDynamicPropertiesStore().allowPBFT();
  }
}
