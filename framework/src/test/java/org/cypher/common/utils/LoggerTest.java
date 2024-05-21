
package org.cypher.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

@Slf4j
public class LoggerTest {

  @Test
  public void testLogger() {
    logger.debug("test debug: {}", "success");
    logger.info("test info: {}", "success");
    logger.warn("test warn: {}", "success");
    logger.error("test error: {}", "success");
  }
}
