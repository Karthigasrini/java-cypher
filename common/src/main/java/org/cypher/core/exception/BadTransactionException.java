package org.cypher.core.exception;

public class BadTransactionException extends CypherException {

  public BadTransactionException() {
    super();
  }

  public BadTransactionException(String message) {
    super(message);
  }

  public BadTransactionException(String message, Throwable cause) {
    super(message, cause);
  }
}
