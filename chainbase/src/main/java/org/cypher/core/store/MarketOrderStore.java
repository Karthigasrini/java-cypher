package org.cypher.core.store;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.cypher.core.capsule.MarketOrderCapsule;
import org.cypher.core.db.CypherStoreWithRevoking;
import org.cypher.core.exception.ItemNotFoundException;

@Component
public class MarketOrderStore extends CypherStoreWithRevoking<MarketOrderCapsule> {

  @Autowired
  protected MarketOrderStore(@Value("market_order") String dbName) {
    super(dbName);
  }

  @Override
  public MarketOrderCapsule get(byte[] key) throws ItemNotFoundException {
    byte[] value = revokingDB.get(key);
    return new MarketOrderCapsule(value);
  }

}