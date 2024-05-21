package org.cypher.core.services.jsonrpc.filters;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import lombok.Getter;
import org.cypher.core.Wallet;
import org.cypher.core.exception.JsonRpcInvalidParamsException;
import org.cypher.core.services.jsonrpc.CypherJsonRpc.FilterRequest;
import org.cypher.core.services.jsonrpc.CypherJsonRpc.LogFilterElement;

public class LogFilterAndResult extends FilterResult<LogFilterElement> {

  @Getter
  private LogFilterWrapper logFilterWrapper;

  public LogFilterAndResult(FilterRequest fr, long currentMaxBlockNum, Wallet wallet)
      throws JsonRpcInvalidParamsException {
    this.logFilterWrapper = new LogFilterWrapper(fr, currentMaxBlockNum, wallet);
    result = new LinkedBlockingQueue<>();
    this.updateExpireTime();
  }

  @Override
  public void add(LogFilterElement logFilterElement) {
    result.add(logFilterElement);
  }

  @Override
  public List<LogFilterElement> popAll() {
    List<LogFilterElement> elements = new ArrayList<>();
    result.drainTo(elements);
    return elements;
  }
}
