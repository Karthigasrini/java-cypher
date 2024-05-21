package org.cypher.core.consensus;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.cypher.common.backup.BackupManager;
import org.cypher.common.backup.BackupManager.BackupStatusEnum;
import org.cypher.consensus.Consensus;
import org.cypher.consensus.base.BlockHandle;
import org.cypher.consensus.base.Param.Miner;
import org.cypher.consensus.base.State;
import org.cypher.core.capsule.BlockCapsule;
import org.cypher.core.db.Manager;
import org.cypher.core.net.CypherNetService;
import org.cypher.core.net.message.BlockMessage;

@Slf4j(topic = "consensus")
@Component
public class BlockHandleImpl implements BlockHandle {

  @Autowired
  private Manager manager;

  @Autowired
  private BackupManager backupManager;

  @Autowired
  private CypherNetService cypherNetService;

  @Autowired
  private Consensus consensus;

  @Override
  public State getState() {
    if (!backupManager.getStatus().equals(BackupStatusEnum.MASTER)) {
      return State.BACKUP_IS_NOT_MASTER;
    }
    return State.OK;
  }

  public Object getLock() {
    return manager;
  }

  public BlockCapsule produce(Miner miner, long blockTime, long timeout) {
    BlockCapsule blockCapsule = manager.generateBlock(miner, blockTime, timeout);
    if (blockCapsule == null) {
      return null;
    }
    try {
      consensus.receiveBlock(blockCapsule);
      BlockMessage blockMessage = new BlockMessage(blockCapsule);
      cypherNetService.fastForward(blockMessage);
      cypherNetService.broadcast(blockMessage);
      manager.pushBlock(blockCapsule);
    } catch (Exception e) {
      logger.error("Handle block {} failed.", blockCapsule.getBlockId().getString(), e);
      return null;
    }
    return blockCapsule;
  }
}
