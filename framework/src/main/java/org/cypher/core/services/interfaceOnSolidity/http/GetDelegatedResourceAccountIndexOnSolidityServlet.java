package org.cypher.core.services.interfaceOnSolidity.http;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.cypher.core.services.http.GetDelegatedResourceAccountIndexServlet;
import org.cypher.core.services.interfaceOnSolidity.WalletOnSolidity;

@Component
@Slf4j(topic = "API")
public class GetDelegatedResourceAccountIndexOnSolidityServlet extends
    GetDelegatedResourceAccountIndexServlet {

  @Autowired
  private WalletOnSolidity walletOnSolidity;

  protected void doGet(HttpServletRequest request, HttpServletResponse response) {
    walletOnSolidity.futureGet(() -> super.doGet(request, response));
  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response) {
    walletOnSolidity.futureGet(() -> super.doPost(request, response));
  }
}
