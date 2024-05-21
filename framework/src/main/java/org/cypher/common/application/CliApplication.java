
package org.cypher.common.application;

import org.cypher.common.parameter.CommonParameter;
import org.cypher.core.ChainBaseManager;
import org.cypher.core.config.args.Args;
import org.cypher.core.db.Manager;

public class CliApplication implements Application {

  @Override
  public void setOptions(Args args) {

  }

  @Override
  public void init(CommonParameter parameter) {

  }

  @Override
  public void initServices(CommonParameter parameter) {

  }

  @Override
  public void startup() {

  }

  @Override
  public void shutdown() {

  }

  @Override
  public void startServices() {

  }

  @Override
  public void shutdownServices() {

  }

  @Override
  public void addService(Service service) {

  }

  @Override
  public Manager getDbManager() {
    return null;
  }

  @Override
  public ChainBaseManager getChainBaseManager() {
    return null;
  }

}
