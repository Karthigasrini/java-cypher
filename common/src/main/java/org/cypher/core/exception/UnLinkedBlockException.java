package org.cypher.core.exception;

public class UnLinkedBlockException extends CypherException {

  public UnLinkedBlockException() {
    super();
  }

  public UnLinkedBlockException(String message) {
    super(message);
  }

  public UnLinkedBlockException(String message, Throwable cause) {
    super(message, cause);
  }
}
