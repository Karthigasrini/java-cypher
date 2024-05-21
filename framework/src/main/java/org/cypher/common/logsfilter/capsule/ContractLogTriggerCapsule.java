package org.cypher.common.logsfilter.capsule;

import static org.cypher.common.logsfilter.EventPluginLoader.matchFilter;

import lombok.Getter;
import lombok.Setter;
import org.cypher.common.logsfilter.EventPluginLoader;
import org.cypher.common.logsfilter.trigger.ContractLogTrigger;

public class ContractLogTriggerCapsule extends TriggerCapsule {

  @Getter
  @Setter
  private ContractLogTrigger contractLogTrigger;

  public ContractLogTriggerCapsule(ContractLogTrigger contractLogTrigger) {
    this.contractLogTrigger = contractLogTrigger;
  }

  public void setLatestSolidifiedBlockNumber(long latestSolidifiedBlockNumber) {
    contractLogTrigger.setLatestSolidifiedBlockNumber(latestSolidifiedBlockNumber);
  }

  @Override
  public void processTrigger() {
    if (matchFilter(contractLogTrigger)) {
      EventPluginLoader.getInstance().postContractLogTrigger(contractLogTrigger);
    }
  }
}
