

package org.cypher.core;

import com.google.protobuf.ByteString;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.cypher.common.utils.ByteArray;
import org.cypher.common.utils.Sha256Hash;
import org.cypher.core.capsule.BlockCapsule;
import org.cypher.core.capsule.utils.BlockUtil;
import org.cypher.core.config.args.Args;
import org.cypher.protos.Protocol.Block;
import org.cypher.protos.Protocol.BlockHeader;
import org.cypher.protos.Protocol.BlockHeader.raw;

@Slf4j
public class BlockUtilTest {

  @Before
  public void initConfiguration() {
    Args.setParam(new String[]{}, Constant.TEST_CONF);
  }

  @After
  public void destroy() {
    Args.clearParam();
  }

  @Test
  public void testBlockUtil() {
    //test create GenesisBlockCapsule
    BlockCapsule blockCapsule1 = BlockUtil.newGenesisBlockCapsule();
    Sha256Hash sha256Hash = Sha256Hash.wrap(ByteArray
        .fromHexString("0x0000000000000000000000000000000000000000000000000000000000000000"));

    Assert.assertEquals(0, blockCapsule1.getTimeStamp());
    Assert.assertEquals(sha256Hash,
        blockCapsule1.getParentHash());
    Assert.assertEquals(0, blockCapsule1.getNum());

    //test isParentOf method: create blockCapsule2 and blockCapsule3
    // blockCapsule3.setParentHash() equals blockCapsule2.getBlockId
    BlockCapsule blockCapsule2 = new BlockCapsule(Block.newBuilder().setBlockHeader(
        BlockHeader.newBuilder().setRawData(raw.newBuilder().setParentHash(ByteString.copyFrom(
            ByteArray
                .fromHexString("0304f784e4e7bae517bcab94c3e0c9214fb4ac7ff9d7d5a937d1f40031f87b81")))
        )).build());

    BlockCapsule blockCapsule3 = new BlockCapsule(Block.newBuilder().setBlockHeader(
        BlockHeader.newBuilder().setRawData(raw.newBuilder().setParentHash(ByteString.copyFrom(
            ByteArray
                .fromHexString(blockCapsule2.getBlockId().toString())))
        )).build());

    Assert.assertEquals(false, BlockUtil.isParentOf(blockCapsule1, blockCapsule2));
    Assert.assertFalse(BlockUtil.isParentOf(blockCapsule1, blockCapsule2));
    Assert.assertEquals(true, BlockUtil.isParentOf(blockCapsule2, blockCapsule3));
    Assert.assertTrue(BlockUtil.isParentOf(blockCapsule2, blockCapsule3));
  }
}
