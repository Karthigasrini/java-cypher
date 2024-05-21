package org.cypher.core.db;

import lombok.Data;
import org.cypher.common.runtime.ProgramResult;
import org.cypher.core.capsule.BlockCapsule;
import org.cypher.core.capsule.TransactionCapsule;
import org.cypher.core.store.StoreFactory;

@Data
public class TransactionContext {

  private BlockCapsule blockCap;
  private TransactionCapsule cypCap;
  private StoreFactory storeFactory;
  private ProgramResult programResult = new ProgramResult();
  private boolean isStatic;
  private boolean eventPluginLoaded;

  public TransactionContext(BlockCapsule blockCap, TransactionCapsule cypCap,
      StoreFactory storeFactory,
      boolean isStatic,
      boolean eventPluginLoaded) {
    this.blockCap = blockCap;
    this.cypCap = cypCap;
    this.storeFactory = storeFactory;
    this.isStatic = isStatic;
    this.eventPluginLoaded = eventPluginLoaded;
  }
}
