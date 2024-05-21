package org.cypher.consensus.base;

import org.cypher.consensus.pbft.message.PbftBaseMessage;
import org.cypher.core.capsule.BlockCapsule;

public interface PbftInterface {

  boolean isSyncing();

  void forwardMessage(PbftBaseMessage message);

  BlockCapsule getBlock(long blockNum) throws Exception;

}