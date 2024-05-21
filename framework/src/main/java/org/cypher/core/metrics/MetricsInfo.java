package org.cypher.core.metrics;

import lombok.Data;
import org.cypher.core.metrics.blockchain.BlockChainInfo;
import org.cypher.core.metrics.net.NetInfo;
import org.cypher.core.metrics.node.NodeInfo;

@Data
public class MetricsInfo {
  private long interval;
  private NodeInfo node;
  private BlockChainInfo blockchain;
  private NetInfo net;
}
