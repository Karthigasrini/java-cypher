
package org.cypher.common.application;

import java.util.ArrayList;
import lombok.extern.slf4j.Slf4j;
import org.cypher.common.parameter.CommonParameter;

@Slf4j(topic = "app")
public class ServiceContainer {

  private ArrayList<Service> services;

  public ServiceContainer() {
    this.services = new ArrayList<>();
  }

  public void add(Service service) {
    this.services.add(service);
  }


  public void init() {
    for (Service service : this.services) {
      logger.debug("Initing " + service.getClass().getSimpleName());
      service.init();
    }
  }

  public void init(CommonParameter parameter) {
    for (Service service : this.services) {
      logger.debug("Initing " + service.getClass().getSimpleName());
      service.init(parameter);
    }
  }

  public void start() {
    logger.debug("Starting services");
    for (Service service : this.services) {
      logger.debug("Starting " + service.getClass().getSimpleName());
      service.start();
    }
  }

  public void stop() {
    for (Service service : this.services) {
      logger.debug("Stopping " + service.getClass().getSimpleName());
      service.stop();
    }
  }
}
