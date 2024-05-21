package org.cypher.core.exception;

public class AccountResourceInsufficientException extends CypherException {

  public AccountResourceInsufficientException() {
    super("Insufficient bandwidth and balance to create new account");
  }

  public AccountResourceInsufficientException(String message) {
    super(message);
  }
}

