package org.cypher.common.runtime;

import org.cypher.core.db.TransactionContext;
import org.cypher.core.exception.ContractExeException;
import org.cypher.core.exception.ContractValidateException;


public interface Runtime {

  void execute(TransactionContext context)
      throws ContractValidateException, ContractExeException;

  ProgramResult getResult();

  String getRuntimeError();

}
