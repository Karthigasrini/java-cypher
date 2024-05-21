package org.cypher.core.jsonrpc;

import com.google.protobuf.ByteString;
import java.io.File;
import lombok.extern.slf4j.Slf4j;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.cypher.common.application.CypherApplicationContext;
import org.cypher.common.utils.ByteArray;
import org.cypher.common.utils.FileUtil;
import org.cypher.core.Constant;
import org.cypher.core.Wallet;
import org.cypher.core.capsule.AccountCapsule;
import org.cypher.core.config.DefaultConfig;
import org.cypher.core.config.args.Args;
import org.cypher.core.db.Manager;
import org.cypher.core.db2.core.Chainbase.Cursor;
import org.cypher.core.services.NodeInfoService;
import org.cypher.core.services.jsonrpc.CypherJsonRpcImpl;
import org.cypher.core.services.jsonrpc.CypherJsonRpcImpl.RequestSource;
import org.cypher.core.services.jsonrpc.types.BuildArguments;
import org.cypher.protos.Protocol;

@Slf4j
public class WalletCursorTest {
  private static String dbPath = "output_wallet_cursor_test";
  private static final String OWNER_ADDRESS;
  private static final String OWNER_ADDRESS_ACCOUNT_NAME = "first";

  private static CypherApplicationContext context;
  private static Manager dbManager;
  private static Wallet wallet;
  private static NodeInfoService nodeInfoService;

  static {
    Args.setParam(new String[]{"--output-directory", dbPath}, Constant.TEST_CONF);
    context = new CypherApplicationContext(DefaultConfig.class);

    OWNER_ADDRESS =
        Wallet.getAddressPreFixString() + "abd4b9367799eaa3197fecb144eb71de1e049abc";
    nodeInfoService = context.getBean("nodeInfoService", NodeInfoService.class);
  }

  @BeforeClass
  public static void init() {
    dbManager = context.getBean(Manager.class);
    wallet = context.getBean(Wallet.class);

    AccountCapsule accountCapsule =
        new AccountCapsule(
            ByteString.copyFromUtf8(OWNER_ADDRESS_ACCOUNT_NAME),
            ByteString.copyFrom(ByteArray.fromHexString(OWNER_ADDRESS)),
            Protocol.AccountType.Normal,
            10000_000_000L);
    dbManager.getAccountStore().put(accountCapsule.getAddress().toByteArray(), accountCapsule);
  }

  @AfterClass
  public static void removeDb() {
    Args.clearParam();
    context.destroy();
    if (FileUtil.deleteDir(new File(dbPath))) {
      logger.info("Release resources successful.");
    } else {
      logger.info("Release resources failure.");
    }
  }

  @Test
  public void testSource() {
    CypherJsonRpcImpl tronJsonRpc = new CypherJsonRpcImpl(nodeInfoService, wallet, dbManager);

    Assert.assertEquals(Cursor.HEAD, wallet.getCursor());
    Assert.assertEquals(RequestSource.FULLNODE, tronJsonRpc.getSource());

    dbManager.setCursor(Cursor.HEAD);
    Assert.assertEquals(Cursor.HEAD, wallet.getCursor());
    Assert.assertEquals(RequestSource.FULLNODE, tronJsonRpc.getSource());
    dbManager.resetCursor();

    dbManager.setCursor(Cursor.SOLIDITY);
    Assert.assertEquals(Cursor.SOLIDITY, wallet.getCursor());
    Assert.assertEquals(RequestSource.SOLIDITY, tronJsonRpc.getSource());
    dbManager.resetCursor();

    dbManager.setCursor(Cursor.PBFT);
    Assert.assertEquals(Cursor.PBFT, wallet.getCursor());
    Assert.assertEquals(RequestSource.PBFT, tronJsonRpc.getSource());
    dbManager.resetCursor();
  }

  @Test
  public void testDisableInSolidity() {
    BuildArguments buildArguments = new BuildArguments();
    buildArguments.from = "0xabd4b9367799eaa3197fecb144eb71de1e049abc";
    buildArguments.to = "0x548794500882809695a8a687866e76d4271a1abc";
    buildArguments.tokenId = 1000016L;
    buildArguments.tokenValue = 20L;

    dbManager.setCursor(Cursor.SOLIDITY);

    CypherJsonRpcImpl tronJsonRpc = new CypherJsonRpcImpl(nodeInfoService, wallet, dbManager);
    try {
      tronJsonRpc.buildTransaction(buildArguments);
    } catch (Exception e) {
      Assert.assertEquals("the method buildTransaction does not exist/is not available in "
          + "SOLIDITY", e.getMessage());
    }

    dbManager.resetCursor();
  }

  @Test
  public void testDisableInPBFT() {
    BuildArguments buildArguments = new BuildArguments();
    buildArguments.from = "0xabd4b9367799eaa3197fecb144eb71de1e049abc";
    buildArguments.to = "0x548794500882809695a8a687866e76d4271a1abc";
    buildArguments.tokenId = 1000016L;
    buildArguments.tokenValue = 20L;

    dbManager.setCursor(Cursor.PBFT);

    CypherJsonRpcImpl tronJsonRpc = new CypherJsonRpcImpl(nodeInfoService, wallet, dbManager);
    try {
      tronJsonRpc.buildTransaction(buildArguments);
    } catch (Exception e) {
      Assert.assertEquals("the method buildTransaction does not exist/is not available in "
          + "PBFT", e.getMessage());
    }

    dbManager.resetCursor();
  }

  @Test
  public void testEnableInFullNode() {
    BuildArguments buildArguments = new BuildArguments();
    buildArguments.from = "0xabd4b9367799eaa3197fecb144eb71de1e049abc";
    buildArguments.to = "0x548794500882809695a8a687866e76d4271a1abc";
    buildArguments.value = "0x1f4";

    CypherJsonRpcImpl tronJsonRpc = new CypherJsonRpcImpl(nodeInfoService, wallet, dbManager);

    try {
      tronJsonRpc.buildTransaction(buildArguments);
    } catch (Exception e) {
      Assert.fail();
    }
  }

}