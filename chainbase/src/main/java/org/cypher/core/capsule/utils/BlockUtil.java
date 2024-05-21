
package org.cypher.core.capsule.utils;

import com.google.protobuf.ByteString;
import java.util.List;
import java.util.stream.Collectors;
import org.cypher.common.args.GenesisBlock;
import org.cypher.common.parameter.CommonParameter;
import org.cypher.common.utils.ByteArray;
import org.cypher.core.capsule.BlockCapsule;
import org.cypher.protos.Protocol.Transaction;

public class BlockUtil {

  /**
   * create genesis block from transactions.
   */
  public static BlockCapsule newGenesisBlockCapsule() {
    GenesisBlock genesisBlockArg = CommonParameter.getInstance()
        .getGenesisBlock();
    List<Transaction> transactionList =
        genesisBlockArg.getAssets().stream()
            .map(key -> TransactionUtil.newGenesisTransaction(key.getAddress(), key.getBalance()))
            .collect(Collectors.toList());

    long timestamp = Long.parseLong(genesisBlockArg.getTimestamp());
    ByteString parentHash =
        ByteString.copyFrom(ByteArray.fromHexString(genesisBlockArg.getParentHash()));
    long number = Long.parseLong(genesisBlockArg.getNumber());

    BlockCapsule blockCapsule = new BlockCapsule(timestamp, parentHash, number, transactionList);

    blockCapsule.setMerkleRoot();
    blockCapsule.setWitness(
        "A new system must allow existing systems to be linked together without "
            + "requiring any central control or coordination");
    blockCapsule.generatedByMyself = true;

    return blockCapsule;
  }

  /**
   * Whether the hash of the judge block is equal to the hash of the parent block.
   */
  public static boolean isParentOf(BlockCapsule blockCapsule1, BlockCapsule blockCapsule2) {
    return blockCapsule1.getBlockId().equals(blockCapsule2.getParentHash());
  }
}
