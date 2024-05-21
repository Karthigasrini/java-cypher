package org.cypher.core.exception;

public class TooBigTransactionException extends CypherException {

  public TooBigTransactionException() {
    super();
  }

  public TooBigTransactionException(String message) {
    super(message);
  }
}
