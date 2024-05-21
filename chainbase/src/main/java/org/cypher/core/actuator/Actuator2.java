package org.cypher.core.actuator;

import org.cypher.core.exception.ContractExeException;
import org.cypher.core.exception.ContractValidateException;

public interface Actuator2 {

  void execute(Object object) throws ContractExeException;

  void validate(Object object) throws ContractValidateException;
}