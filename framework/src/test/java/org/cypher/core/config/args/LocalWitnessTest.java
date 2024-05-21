

package org.cypher.core.config.args;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.cypher.common.utils.LocalWitnesses;

public class LocalWitnessTest {

  private LocalWitnesses localWitness = new LocalWitnesses();

  @Before
  public void setLocalWitness() {
    localWitness
        .setPrivateKeys(
            Lists.newArrayList(
                "f31db24bfbd1a2ef19beddca0a0fa37632eded9ac666a05d3bd925f01dde1f62"));
  }

  @Test
  public void whenSetNullPrivateKey() {
    localWitness.setPrivateKeys(null);
  }

  @Test
  public void whenSetEmptyPrivateKey() {
    localWitness.setPrivateKeys(Lists.newArrayList(""));
  }

  @Test(expected = IllegalArgumentException.class)
  public void whenSetBadFormatPrivateKey() {
    localWitness.setPrivateKeys(Lists.newArrayList("a111"));
  }

  @Test
  public void whenSetPrefixPrivateKey() {
    localWitness
        .setPrivateKeys(Lists
            .newArrayList("0xf31db24bfbd1a2ef19beddca0a0fa37632eded9ac666a05d3bd925f01dde1f62"));
    localWitness
        .setPrivateKeys(Lists
            .newArrayList("0Xf31db24bfbd1a2ef19beddca0a0fa37632eded9ac666a05d3bd925f01dde1f62"));
  }

  @Test
  public void getPrivateKey() {
    Assert.assertEquals(Lists
            .newArrayList("f31db24bfbd1a2ef19beddca0a0fa37632eded9ac666a05d3bd925f01dde1f62"),
        localWitness.getPrivateKeys());
  }
}
