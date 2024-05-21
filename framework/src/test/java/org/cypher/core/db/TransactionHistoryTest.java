package org.cypher.core.db;

import java.io.File;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.cypher.common.application.CypherApplicationContext;
import org.cypher.common.utils.ByteArray;
import org.cypher.common.utils.FileUtil;
import org.cypher.core.Constant;
import org.cypher.core.capsule.TransactionInfoCapsule;
import org.cypher.core.config.DefaultConfig;
import org.cypher.core.config.args.Args;
import org.cypher.core.exception.BadItemException;
import org.cypher.core.store.TransactionHistoryStore;

public class TransactionHistoryTest {

  private static final byte[] transactionId = TransactionStoreTest.randomBytes(32);
  private static String dbPath = "output_TransactionHistoryStore_test";
  private static String dbDirectory = "db_TransactionHistoryStore_test";
  private static String indexDirectory = "index_TransactionHistoryStore_test";
  private static CypherApplicationContext context;
  private static TransactionHistoryStore transactionHistoryStore;

  static {
    Args.setParam(
        new String[]{
            "--output-directory", dbPath,
            "--storage-db-directory", dbDirectory,
            "--storage-index-directory", indexDirectory
        },
        Constant.TEST_CONF
    );
    context = new CypherApplicationContext(DefaultConfig.class);
  }

  @AfterClass
  public static void destroy() {
    Args.clearParam();
    context.destroy();
    FileUtil.deleteDir(new File(dbPath));
  }

  @BeforeClass
  public static void init() {
    transactionHistoryStore = context.getBean(TransactionHistoryStore.class);
    TransactionInfoCapsule transactionInfoCapsule = new TransactionInfoCapsule();

    transactionInfoCapsule.setId(transactionId);
    transactionInfoCapsule.setFee(1000L);
    transactionInfoCapsule.setBlockNumber(100L);
    transactionInfoCapsule.setBlockTimeStamp(200L);
    transactionHistoryStore.put(transactionId, transactionInfoCapsule);
  }

  @Test
  public void get() throws BadItemException {
    //test get and has Method
    TransactionInfoCapsule resultCapsule = transactionHistoryStore.get(transactionId);
    Assert.assertEquals(1000L, resultCapsule.getFee());
    Assert.assertEquals(100L, resultCapsule.getBlockNumber());
    Assert.assertEquals(200L, resultCapsule.getBlockTimeStamp());
    Assert.assertEquals(ByteArray.toHexString(transactionId),
        ByteArray.toHexString(resultCapsule.getId()));
  }
}