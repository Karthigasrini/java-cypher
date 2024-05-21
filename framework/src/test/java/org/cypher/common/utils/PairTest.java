
package org.cypher.common.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PairTest {

  @Test
  public void testPairObject() {
    Pair<String, String> aPair = new Pair<>("key", "value");
    assertEquals("key", aPair.getKey());
    assertEquals("value", aPair.getValue());
    assertEquals("key=value", aPair.toString());      
  }

  @Test
  public void testPairObjectEquality() {
    Pair<String, String> aPair = new Pair<>("key", "value");
    Pair<String, String> aPair2 = aPair;
    Pair<String, String> anotherPair = new Pair<>("key", "value");
    // reference equality checks
    assertTrue(aPair == aPair2);
    assertFalse(aPair == anotherPair);
    // value equality checks
    assertTrue(aPair.equals(aPair2));
    assertTrue(aPair.equals(anotherPair));
  }  
}
