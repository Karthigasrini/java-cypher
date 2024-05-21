package org.cypher.core.store;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.cypher.core.capsule.MarketAccountOrderCapsule;
import org.cypher.core.db.CypherStoreWithRevoking;
import org.cypher.core.exception.ItemNotFoundException;

@Component
public class MarketAccountStore extends CypherStoreWithRevoking<MarketAccountOrderCapsule> {

  @Autowired
  protected MarketAccountStore(@Value("market_account") String dbName) {
    super(dbName);
  }

  @Override
  public MarketAccountOrderCapsule get(byte[] key) throws ItemNotFoundException {
    byte[] value = revokingDB.get(key);
    return new MarketAccountOrderCapsule(value);
  }

}