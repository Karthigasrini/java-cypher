package org.cypher.consensus.base;

import org.cypher.consensus.base.Param.Miner;
import org.cypher.core.capsule.BlockCapsule;

public interface BlockHandle {

  State getState();

  Object getLock();

  BlockCapsule produce(Miner miner, long blockTime, long timeout);

}