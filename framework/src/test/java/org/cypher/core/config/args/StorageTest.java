
package org.cypher.core.config.args;

import java.io.File;
import org.iq80.leveldb.CompressionType;
import org.iq80.leveldb.Options;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.cypher.common.utils.FileUtil;
import org.cypher.common.utils.StorageUtils;

public class StorageTest {

  private static Storage storage;

  static {
    Args.setParam(new String[]{}, "config-test-storagetest.conf");
    storage = Args.getInstance().getStorage();
  }

  @AfterClass
  public static void cleanup() {
    Args.clearParam();
    FileUtil.deleteDir(new File("test_path"));
  }

  @Test
  public void getDirectory() {
    Assert.assertEquals("database", storage.getDbDirectory());
    Assert.assertEquals("index", storage.getIndexDirectory());
  }

  @Test
  public void getPath() {
    Assert.assertEquals("storage_directory_test", StorageUtils.getPathByDbName("account"));
    Assert.assertEquals("test_path", StorageUtils.getPathByDbName("test_name"));
    Assert.assertNull(StorageUtils.getPathByDbName("some_name_not_exists"));
  }

  @Test
  public void getOptions() {
    Options options = StorageUtils.getOptionsByDbName("account");
    Assert.assertTrue(options.createIfMissing());
    Assert.assertTrue(options.paranoidChecks());
    Assert.assertTrue(options.verifyChecksums());
    Assert.assertEquals(CompressionType.SNAPPY, options.compressionType());
    Assert.assertEquals(4096, options.blockSize());
    Assert.assertEquals(10485760, options.writeBufferSize());
    Assert.assertEquals(10485760L, options.cacheSize());
    Assert.assertEquals(100, options.maxOpenFiles());

    options = StorageUtils.getOptionsByDbName("test_name");
    Assert.assertFalse(options.createIfMissing());
    Assert.assertFalse(options.paranoidChecks());
    Assert.assertFalse(options.verifyChecksums());
    Assert.assertEquals(CompressionType.SNAPPY, options.compressionType());
    Assert.assertEquals(2, options.blockSize());
    Assert.assertEquals(3, options.writeBufferSize());
    Assert.assertEquals(4L, options.cacheSize());
    Assert.assertEquals(5, options.maxOpenFiles());

    options = StorageUtils.getOptionsByDbName("some_name_not_exists");
    Assert.assertTrue(options.createIfMissing());
    Assert.assertTrue(options.paranoidChecks());
    Assert.assertTrue(options.verifyChecksums());
    Assert.assertEquals(CompressionType.SNAPPY, options.compressionType());
    Assert.assertEquals(4 * 1024, options.blockSize());
    Assert.assertEquals(16 * 1024 * 1024, options.writeBufferSize());
    Assert.assertEquals(32 * 1024 * 1024L, options.cacheSize());
    Assert.assertEquals(50, options.maxOpenFiles());

    options = StorageUtils.getOptionsByDbName("code");
    Assert.assertEquals(64 * 1024 * 1024, options.writeBufferSize());
    Assert.assertEquals(500, options.maxOpenFiles());

    options = StorageUtils.getOptionsByDbName("delegation");
    Assert.assertEquals(64 * 1024 * 1024, options.writeBufferSize());
    Assert.assertEquals(1000, options.maxOpenFiles());

    options = StorageUtils.getOptionsByDbName("trans");
    Assert.assertEquals(256 * 1024 * 1024, options.writeBufferSize());
    Assert.assertEquals(50, options.maxOpenFiles());
  }

}
