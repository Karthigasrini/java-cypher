package org.cypher.core.capsule;

public interface ProtoCapsule<T> {

  byte[] getData();

  T getInstance();
}
