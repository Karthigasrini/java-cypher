

package org.cypher.core.config.args;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class OverlayTest {

  private Overlay overlay = new Overlay();

  @Before
  public void setOverlay() {
    overlay.setPort(8080);
  }

  @Test(expected = IllegalArgumentException.class)
  public void whenSetOutOfBoundsPort() {
    overlay.setPort(-1);
  }

  @Test
  public void getOverlay() {
    Assert.assertEquals(8080, overlay.getPort());
  }
}
