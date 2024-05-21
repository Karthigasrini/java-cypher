package org.cypher.core.services.interfaceOnPBFT;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.cypher.core.db2.core.Chainbase;
import org.cypher.core.services.WalletOnCursor;

@Slf4j(topic = "API")
@Component
public class WalletOnPBFT extends WalletOnCursor {

  public WalletOnPBFT() {
    super.cursor = Chainbase.Cursor.PBFT;
  }
}
