package org.cypher.common.overlay.discover.node;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedList;
import org.bouncycastle.util.encoders.Hex;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.cypher.common.net.udp.message.UdpMessageTypeEnum;
import org.cypher.common.overlay.discover.node.statistics.MessageStatistics;
import org.cypher.common.overlay.discover.node.statistics.NodeStatistics;
import org.cypher.common.overlay.message.DisconnectMessage;
import org.cypher.common.overlay.message.PongMessage;
import org.cypher.common.utils.Sha256Hash;
import org.cypher.core.capsule.BlockCapsule;
import org.cypher.core.net.message.BlockMessage;
import org.cypher.core.net.message.ChainInventoryMessage;
import org.cypher.core.net.message.FetchInvDataMessage;
import org.cypher.core.net.message.InventoryMessage;
import org.cypher.core.net.message.MessageTypes;
import org.cypher.core.net.message.SyncBlockChainMessage;
import org.cypher.core.net.message.TransactionsMessage;
import org.cypher.protos.Protocol;

public class NodeStatisticsTest {

  private NodeStatistics nodeStatistics;

  @Before
  public void init() {
    this.nodeStatistics = new NodeStatistics();
  }

  @Test
  public void testNode() throws NoSuchFieldException, IllegalAccessException {
    Protocol.ReasonCode reasonCode = this.nodeStatistics.getDisconnectReason();
    Assert.assertEquals(Protocol.ReasonCode.UNKNOWN, reasonCode);

    boolean isReputationPenalized = this.nodeStatistics.isReputationPenalized();
    Assert.assertFalse(isReputationPenalized);

    this.nodeStatistics.setPredefined(true);
    Assert.assertTrue(this.nodeStatistics.isPredefined());

    this.nodeStatistics.setPersistedReputation(10000);
    this.nodeStatistics.nodeDisconnectedRemote(Protocol.ReasonCode.INCOMPATIBLE_VERSION);
    isReputationPenalized = this.nodeStatistics.isReputationPenalized();
    Assert.assertTrue(isReputationPenalized);

    Field field = this.nodeStatistics.getClass().getDeclaredField("firstDisconnectedTime");
    field.setAccessible(true);
    field.set(this.nodeStatistics, System.currentTimeMillis() - 60 * 60 * 1000L - 1);
    isReputationPenalized = this.nodeStatistics.isReputationPenalized();
    Assert.assertFalse(isReputationPenalized);
    reasonCode = this.nodeStatistics.getDisconnectReason();
    Assert.assertEquals(Protocol.ReasonCode.UNKNOWN, reasonCode);

    String str = this.nodeStatistics.toString();
    //System.out.println(str);
    Assert.assertNotNull(str);

    this.nodeStatistics.nodeIsHaveDataTransfer();
    this.nodeStatistics.resetTcpFlow();
    this.nodeStatistics.discoverMessageLatency.add(10);
    this.nodeStatistics.discoverMessageLatency.add(20);
    long avg = this.nodeStatistics.discoverMessageLatency.getAvg();
    Assert.assertEquals(15, avg);

  }

  @Test
  public void testMessage() {
    MessageStatistics statistics = this.nodeStatistics.messageStatistics;
    statistics.addUdpInMessage(UdpMessageTypeEnum.DISCOVER_FIND_NODE);
    statistics.addUdpOutMessage(UdpMessageTypeEnum.DISCOVER_NEIGHBORS);
    statistics.addUdpInMessage(UdpMessageTypeEnum.DISCOVER_NEIGHBORS);
    statistics.addUdpOutMessage(UdpMessageTypeEnum.DISCOVER_FIND_NODE);
    Assert.assertEquals(1, statistics.discoverInFindNode.getTotalCount());
    long inFindNodeCount = statistics.discoverInFindNode.getTotalCount();
    long outNeighbours = statistics.discoverOutNeighbours.getTotalCount();
    Assert.assertEquals(inFindNodeCount, outNeighbours);

    PongMessage pongMessage = new PongMessage(MessageTypes.P2P_PONG.asByte(), Hex.decode("C0"));
    pongMessage.getData();
    String pongStr = pongMessage.toString();
    Assert.assertNotNull(pongStr);
    statistics.addTcpInMessage(pongMessage);
    statistics.addTcpOutMessage(pongMessage);
    Assert.assertEquals(1, statistics.p2pInPong.getTotalCount());

    DisconnectMessage disconnectMessage = new DisconnectMessage(Protocol.ReasonCode.TOO_MANY_PEERS);
    Assert.assertEquals(Protocol.ReasonCode.TOO_MANY_PEERS, disconnectMessage.getReasonCode());
    statistics.addTcpInMessage(disconnectMessage);
    statistics.addTcpOutMessage(disconnectMessage);
    Assert.assertEquals(1, statistics.p2pOutDisconnect.getTotalCount());

    SyncBlockChainMessage syncBlockChainMessage = new SyncBlockChainMessage(new ArrayList<>());
    String syncBlockChainStr = syncBlockChainMessage.toString();
    Assert.assertNotNull(syncBlockChainStr);
    statistics.addTcpInMessage(syncBlockChainMessage);
    statistics.addTcpOutMessage(syncBlockChainMessage);
    Assert.assertEquals(1, statistics.tronInSyncBlockChain.getTotalCount());

    ChainInventoryMessage chainInventoryMessage = new ChainInventoryMessage(new ArrayList<>(), 0L);
    String chainInventoryMessageStr = chainInventoryMessage.toString();
    Assert.assertNotNull(chainInventoryMessageStr);
    statistics.addTcpInMessage(chainInventoryMessage);
    statistics.addTcpOutMessage(chainInventoryMessage);
    Assert.assertEquals(1, statistics.tronOutBlockChainInventory.getTotalCount());

    InventoryMessage invMsgCyp =
        new InventoryMessage(new ArrayList<>(), Protocol.Inventory.InventoryType.CYP);
    String inventoryMessageStr = invMsgCyp.toString();
    Assert.assertNotNull(inventoryMessageStr);
    statistics.addTcpInMessage(invMsgCyp);
    statistics.addTcpOutMessage(invMsgCyp);
    InventoryMessage invMsgBlock =
        new InventoryMessage(new ArrayList<>(), Protocol.Inventory.InventoryType.BLOCK);
    MessageTypes invType = invMsgBlock.getInvMessageType();
    Assert.assertEquals(MessageTypes.BLOCK, invType);
    statistics.addTcpInMessage(invMsgBlock);
    statistics.addTcpOutMessage(invMsgBlock);
    Assert.assertEquals(1, statistics.tronInBlockInventory.getTotalCount());

    FetchInvDataMessage fetchInvDataCyp =
        new FetchInvDataMessage(new ArrayList<>(), Protocol.Inventory.InventoryType.CYP);
    statistics.addTcpInMessage(fetchInvDataCyp);
    statistics.addTcpOutMessage(fetchInvDataCyp);
    FetchInvDataMessage fetchInvDataBlock =
        new FetchInvDataMessage(new ArrayList<>(), Protocol.Inventory.InventoryType.BLOCK);
    statistics.addTcpInMessage(fetchInvDataBlock);
    statistics.addTcpOutMessage(fetchInvDataBlock);
    Assert.assertEquals(1, statistics.tronInCypFetchInvData.getTotalCount());

    TransactionsMessage transactionsMessage =
        new TransactionsMessage(new LinkedList<>());
    statistics.addTcpInMessage(transactionsMessage);
    statistics.addTcpOutMessage(transactionsMessage);
    Assert.assertEquals(1, statistics.tronInCyps.getTotalCount());

    BlockCapsule blockCapsule = new BlockCapsule(1, Sha256Hash.ZERO_HASH,
        System.currentTimeMillis(), Sha256Hash.ZERO_HASH.getByteString());
    BlockMessage blockMessage = new BlockMessage(blockCapsule);
    statistics.addTcpInMessage(blockMessage);
    statistics.addTcpOutMessage(blockMessage);
    long inBlockCount = statistics.tronInBlock.getTotalCount();
    Assert.assertEquals(1, inBlockCount);
  }
}
