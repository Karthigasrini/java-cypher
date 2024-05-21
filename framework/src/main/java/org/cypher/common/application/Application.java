

package org.cypher.common.application;

import org.cypher.common.parameter.CommonParameter;
import org.cypher.core.ChainBaseManager;
import org.cypher.core.config.args.Args;
import org.cypher.core.db.Manager;

public interface Application {

  void setOptions(Args args);

  void init(CommonParameter parameter);

  void initServices(CommonParameter parameter);

  void startup();

  void shutdown();

  void startServices();

  void shutdownServices();

  void addService(Service service);

  Manager getDbManager();

  ChainBaseManager getChainBaseManager();

}
