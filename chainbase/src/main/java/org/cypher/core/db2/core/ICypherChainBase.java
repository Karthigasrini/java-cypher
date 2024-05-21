package org.cypher.core.db2.core;

import com.google.protobuf.InvalidProtocolBufferException;
import java.util.Map.Entry;
import org.cypher.common.utils.Quitable;
import org.cypher.core.exception.BadItemException;
import org.cypher.core.exception.ItemNotFoundException;

public interface ICypherChainBase<T> extends Iterable<Entry<byte[], T>>, Quitable {

  /**
   * reset the database.
   */
  void reset();

  /**
   * close the database.
   */
  void close();

  void put(byte[] key, T item);

  void delete(byte[] key);

  T get(byte[] key) throws InvalidProtocolBufferException, ItemNotFoundException, BadItemException;

  T getUnchecked(byte[] key);

  boolean has(byte[] key);

  boolean isNotEmpty();

  String getName();

  String getDbName();

}
