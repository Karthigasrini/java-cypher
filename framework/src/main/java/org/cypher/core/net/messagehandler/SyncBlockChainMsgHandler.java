package org.cypher.core.net.messagehandler;

import java.util.LinkedList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.cypher.core.capsule.BlockCapsule.BlockId;
import org.cypher.core.config.Parameter.NetConstants;
import org.cypher.core.exception.P2pException;
import org.cypher.core.exception.P2pException.TypeEnum;
import org.cypher.core.net.CypherNetDelegate;
import org.cypher.core.net.message.ChainInventoryMessage;
import org.cypher.core.net.message.SyncBlockChainMessage;
import org.cypher.core.net.message.CypherMessage;
import org.cypher.core.net.peer.PeerConnection;
import org.cypher.protos.Protocol;

@Slf4j(topic = "net")
@Component
public class SyncBlockChainMsgHandler implements CypherMsgHandler {

  @Autowired
  private CypherNetDelegate tronNetDelegate;

  @Override
  public void processMessage(PeerConnection peer, CypherMessage msg) throws P2pException {

    SyncBlockChainMessage syncBlockChainMessage = (SyncBlockChainMessage) msg;

    check(peer, syncBlockChainMessage);

    long remainNum = 0;

    List<BlockId> summaryChainIds = syncBlockChainMessage.getBlockIds();

    LinkedList<BlockId> blockIds = getLostBlockIds(summaryChainIds);

    if (blockIds.size() == 0) {
      logger.error("Can't get lost block Ids.");
      peer.disconnect(Protocol.ReasonCode.INCOMPATIBLE_CHAIN);
      return;
    } else if (blockIds.size() == 1) {
      peer.setNeedSyncFromUs(false);
    } else {
      peer.setNeedSyncFromUs(true);
      remainNum = tronNetDelegate.getHeadBlockId().getNum() - blockIds.peekLast().getNum();
    }

    peer.setLastSyncBlockId(blockIds.peekLast());
    peer.setRemainNum(remainNum);
    peer.sendMessage(new ChainInventoryMessage(blockIds, remainNum));
  }

  private void check(PeerConnection peer, SyncBlockChainMessage msg) throws P2pException {
    List<BlockId> blockIds = msg.getBlockIds();
    if (CollectionUtils.isEmpty(blockIds)) {
      throw new P2pException(TypeEnum.BAD_MESSAGE, "SyncBlockChain blockIds is empty");
    }

    BlockId firstId = blockIds.get(0);
    if (!tronNetDelegate.containBlockInMainChain(firstId)) {
      throw new P2pException(TypeEnum.BAD_MESSAGE, "No first block:" + firstId.getString());
    }

    long headNum = tronNetDelegate.getHeadBlockId().getNum();
    if (firstId.getNum() > headNum) {
      throw new P2pException(TypeEnum.BAD_MESSAGE,
          "First blockNum:" + firstId.getNum() + " gt my head BlockNum:" + headNum);
    }

    BlockId lastSyncBlockId = peer.getLastSyncBlockId();
    long lastNum = blockIds.get(blockIds.size() - 1).getNum();
    if (lastSyncBlockId != null && lastSyncBlockId.getNum() > lastNum) {
      throw new P2pException(TypeEnum.BAD_MESSAGE,
          "lastSyncNum:" + lastSyncBlockId.getNum() + " gt lastNum:" + lastNum);
    }
  }

  private LinkedList<BlockId> getLostBlockIds(List<BlockId> blockIds) throws P2pException {

    BlockId unForkId = null;
    for (int i = blockIds.size() - 1; i >= 0; i--) {
      if (tronNetDelegate.containBlockInMainChain(blockIds.get(i))) {
        unForkId = blockIds.get(i);
        break;
      }
    }

    if (unForkId == null) {
      throw new P2pException(TypeEnum.SYNC_FAILED, "unForkId is null");
    }

    long len = Math.min(tronNetDelegate.getHeadBlockId().getNum(),
        unForkId.getNum() + NetConstants.SYNC_FETCH_BATCH_NUM);

    LinkedList<BlockId> ids = new LinkedList<>();
    for (long i = unForkId.getNum(); i <= len; i++) {
      BlockId id = tronNetDelegate.getBlockIdByNum(i);
      ids.add(id);
    }
    return ids;
  }

}
