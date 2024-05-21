package org.cypher.core.net.messagehandler;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.cypher.core.config.args.Args;
import org.cypher.core.exception.P2pException;
import org.cypher.core.exception.P2pException.TypeEnum;
import org.cypher.core.net.CypherNetDelegate;
import org.cypher.core.net.message.TransactionMessage;
import org.cypher.core.net.message.TransactionsMessage;
import org.cypher.core.net.message.CypherMessage;
import org.cypher.core.net.peer.Item;
import org.cypher.core.net.peer.PeerConnection;
import org.cypher.core.net.service.AdvService;
import org.cypher.protos.Protocol.Inventory.InventoryType;
import org.cypher.protos.Protocol.ReasonCode;
import org.cypher.protos.Protocol.Transaction;
import org.cypher.protos.Protocol.Transaction.Contract.ContractType;

@Slf4j(topic = "net")
@Component
public class TransactionsMsgHandler implements CypherMsgHandler {

  private static int MAX_CYP_SIZE = 50_000;
  private static int MAX_SMART_CONTRACT_SUBMIT_SIZE = 100;
  @Autowired
  private CypherNetDelegate tronNetDelegate;
  @Autowired
  private AdvService advService;

  private BlockingQueue<CypEvent> smartContractQueue = new LinkedBlockingQueue(MAX_CYP_SIZE);

  private BlockingQueue<Runnable> queue = new LinkedBlockingQueue();

  private int threadNum = Args.getInstance().getValidateSignThreadNum();
  private ExecutorService cypHandlePool = new ThreadPoolExecutor(threadNum, threadNum, 0L,
      TimeUnit.MILLISECONDS, queue);

  private ScheduledExecutorService smartContractExecutor = Executors
      .newSingleThreadScheduledExecutor();

  public void init() {
    handleSmartContract();
  }

  public void close() {
    smartContractExecutor.shutdown();
  }

  public boolean isBusy() {
    return queue.size() + smartContractQueue.size() > MAX_CYP_SIZE;
  }

  @Override
  public void processMessage(PeerConnection peer, CypherMessage msg) throws P2pException {
    TransactionsMessage transactionsMessage = (TransactionsMessage) msg;
    check(peer, transactionsMessage);
    for (Transaction cyp : transactionsMessage.getTransactions().getTransactionsList()) {
      int type = cyp.getRawData().getContract(0).getType().getNumber();
      if (type == ContractType.TriggerSmartContract_VALUE
          || type == ContractType.CreateSmartContract_VALUE) {
        if (!smartContractQueue.offer(new CypEvent(peer, new TransactionMessage(cyp)))) {
          logger.warn("Add smart contract failed, queueSize {}:{}", smartContractQueue.size(),
              queue.size());
        }
      } else {
        cypHandlePool.submit(() -> handleTransaction(peer, new TransactionMessage(cyp)));
      }
    }
  }

  private void check(PeerConnection peer, TransactionsMessage msg) throws P2pException {
    for (Transaction cyp : msg.getTransactions().getTransactionsList()) {
      Item item = new Item(new TransactionMessage(cyp).getMessageId(), InventoryType.CYP);
      if (!peer.getAdvInvRequest().containsKey(item)) {
        throw new P2pException(TypeEnum.BAD_MESSAGE,
            "cyp: " + msg.getMessageId() + " without request.");
      }
      peer.getAdvInvRequest().remove(item);
    }
  }

  private void handleSmartContract() {
    smartContractExecutor.scheduleWithFixedDelay(() -> {
      try {
        while (queue.size() < MAX_SMART_CONTRACT_SUBMIT_SIZE) {
          CypEvent event = smartContractQueue.take();
          cypHandlePool.submit(() -> handleTransaction(event.getPeer(), event.getMsg()));
        }
      } catch (Exception e) {
        logger.error("Handle smart contract exception.", e);
      }
    }, 1000, 20, TimeUnit.MILLISECONDS);
  }

  private void handleTransaction(PeerConnection peer, TransactionMessage cyp) {
    if (peer.isDisconnect()) {
      logger.warn("Drop cyp {} from {}, peer is disconnect.", cyp.getMessageId(),
          peer.getInetAddress());
      return;
    }

    if (advService.getMessage(new Item(cyp.getMessageId(), InventoryType.CYP)) != null) {
      return;
    }

    try {
      tronNetDelegate.pushTransaction(cyp.getTransactionCapsule());
      advService.broadcast(cyp);
    } catch (P2pException e) {
      logger.warn("Cyp {} from peer {} process failed. type: {}, reason: {}",
          cyp.getMessageId(), peer.getInetAddress(), e.getType(), e.getMessage());
      if (e.getType().equals(TypeEnum.BAD_CYP)) {
        peer.disconnect(ReasonCode.BAD_TX);
      }
    } catch (Exception e) {
      logger.error("Cyp {} from peer {} process failed.", cyp.getMessageId(), peer.getInetAddress(),
          e);
    }
  }

  class CypEvent {

    @Getter
    private PeerConnection peer;
    @Getter
    private TransactionMessage msg;
    @Getter
    private long time;

    public CypEvent(PeerConnection peer, TransactionMessage msg) {
      this.peer = peer;
      this.msg = msg;
      this.time = System.currentTimeMillis();
    }
  }
}