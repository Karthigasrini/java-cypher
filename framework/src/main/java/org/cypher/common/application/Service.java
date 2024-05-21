

package org.cypher.common.application;

import org.cypher.common.parameter.CommonParameter;

public interface Service {

  void init();

  void init(CommonParameter parameter);

  void start();

  void stop();
}
