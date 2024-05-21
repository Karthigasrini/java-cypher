package org.cypher.core.services.interfaceOnPBFT.http;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.cypher.core.services.http.ScanShieldedTRC20NotesByIvkServlet;
import org.cypher.core.services.interfaceOnPBFT.WalletOnPBFT;

@Component
@Slf4j(topic = "API")
public class ScanShieldedTRC20NotesByIvkOnPBFTServlet extends ScanShieldedTRC20NotesByIvkServlet {

  @Autowired
  private WalletOnPBFT walletOnPBFT;

  protected void doGet(HttpServletRequest request, HttpServletResponse response) {
    walletOnPBFT.futureGet(() -> super.doGet(request, response));
  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response) {
    walletOnPBFT.futureGet(() -> super.doPost(request, response));
  }
}
