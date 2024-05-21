package org.cypher.core.net.services;

import com.google.common.collect.Lists;
import java.util.List;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.cypher.common.application.CypherApplicationContext;
import org.cypher.common.overlay.server.SyncPool;
import org.cypher.common.parameter.CommonParameter;
import org.cypher.common.utils.ReflectUtils;
import org.cypher.common.utils.Sha256Hash;
import org.cypher.core.Constant;
import org.cypher.core.capsule.BlockCapsule;
import org.cypher.core.config.DefaultConfig;
import org.cypher.core.config.args.Args;
import org.cypher.core.net.message.BlockMessage;
import org.cypher.core.net.message.TransactionMessage;
import org.cypher.core.net.peer.Item;
import org.cypher.core.net.peer.PeerConnection;
import org.cypher.core.net.service.AdvService;
import org.cypher.protos.Protocol;
import org.cypher.protos.Protocol.Inventory.InventoryType;

//@Ignore
public class AdvServiceTest {

  protected CypherApplicationContext context;
  private AdvService service;
  private PeerConnection peer;
  private SyncPool syncPool;

  /**
   * init context.
   */
  @Before
  public void init() {
    Args.setParam(new String[]{"--output-directory", "output-directory", "--debug"},
        Constant.TEST_CONF);
    context = new CypherApplicationContext(DefaultConfig.class);
    service = context.getBean(AdvService.class);
  }

  /**
   * destroy.
   */
  @After
  public void destroy() {
    Args.clearParam();
    context.destroy();
  }

  @Test
  public void test() {
    testAddInv();
    testBroadcast();
    testFastSend();
    testCypBroadcast();
  }

  private void testAddInv() {
    boolean flag;
    Item itemCyp = new Item(Sha256Hash.ZERO_HASH, InventoryType.CYP);
    flag = service.addInv(itemCyp);
    Assert.assertTrue(flag);
    flag = service.addInv(itemCyp);
    Assert.assertFalse(flag);

    Item itemBlock = new Item(Sha256Hash.ZERO_HASH, InventoryType.BLOCK);
    flag = service.addInv(itemBlock);
    Assert.assertTrue(flag);
    flag = service.addInv(itemBlock);
    Assert.assertFalse(flag);

    service.addInvToCache(itemBlock);
    flag = service.addInv(itemBlock);
    Assert.assertFalse(flag);
  }

  private void testBroadcast() {

    try {
      peer = context.getBean(PeerConnection.class);
      syncPool = context.getBean(SyncPool.class);

      List<PeerConnection> peers = Lists.newArrayList();
      peers.add(peer);
      ReflectUtils.setFieldValue(syncPool, "activePeers", peers);
      BlockCapsule blockCapsule = new BlockCapsule(1, Sha256Hash.ZERO_HASH,
          System.currentTimeMillis(), Sha256Hash.ZERO_HASH.getByteString());
      BlockMessage msg = new BlockMessage(blockCapsule);
      service.broadcast(msg);
      Item item = new Item(blockCapsule.getBlockId(), InventoryType.BLOCK);
      Assert.assertNotNull(service.getMessage(item));

      peer.close();
      syncPool.close();
    } catch (NullPointerException e) {
      System.out.println(e);
    }
  }

  private void testFastSend() {

    try {
      peer = context.getBean(PeerConnection.class);
      syncPool = context.getBean(SyncPool.class);

      List<PeerConnection> peers = Lists.newArrayList();
      peers.add(peer);
      ReflectUtils.setFieldValue(syncPool, "activePeers", peers);
      BlockCapsule blockCapsule = new BlockCapsule(1, Sha256Hash.ZERO_HASH,
          System.currentTimeMillis(), Sha256Hash.ZERO_HASH.getByteString());
      BlockMessage msg = new BlockMessage(blockCapsule);
      service.fastForward(msg);
      Item item = new Item(blockCapsule.getBlockId(), InventoryType.BLOCK);
      //Assert.assertNull(service.getMessage(item));

      peer.getAdvInvRequest().put(item, System.currentTimeMillis());
      service.onDisconnect(peer);

      peer.close();
      syncPool.close();
    } catch (NullPointerException e) {
      System.out.println(e);
    }
  }

  private void testCypBroadcast() {
    Protocol.Transaction cyp = Protocol.Transaction.newBuilder().build();
    CommonParameter.getInstance().setValidContractProtoThreadNum(1);
    TransactionMessage msg = new TransactionMessage(cyp);
    service.broadcast(msg);
    Item item = new Item(msg.getMessageId(), InventoryType.CYP);
    Assert.assertNotNull(service.getMessage(item));
  }

}
