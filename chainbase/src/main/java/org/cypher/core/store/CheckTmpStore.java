package org.cypher.core.store;

import com.google.protobuf.InvalidProtocolBufferException;
import java.util.Spliterator;
import java.util.function.Consumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.cypher.core.db.CypherDatabase;
import org.cypher.core.exception.BadItemException;
import org.cypher.core.exception.ItemNotFoundException;

@Component
public class CheckTmpStore extends CypherDatabase<byte[]> {

  @Autowired
  public CheckTmpStore(ApplicationContext ctx) {
    super("tmp");
  }

  @Override
  public void put(byte[] key, byte[] item) {
  }

  @Override
  public void delete(byte[] key) {

  }

  @Override
  public byte[] get(byte[] key)
      throws InvalidProtocolBufferException, ItemNotFoundException, BadItemException {
    return null;
  }

  @Override
  public boolean has(byte[] key) {
    return false;
  }

  @Override
  public void forEach(Consumer action) {

  }

  @Override
  public Spliterator spliterator() {
    return null;
  }
}