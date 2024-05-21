

package org.cypher.common.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ObjectSizeUtilTest {

  @Test
  public void testGetObjectSize() {

    Person person = new Person();
    assertEquals(48, com.carrotsearch.sizeof.RamUsageEstimator.sizeOf(person));
    Person person1 = new Person(1, "tom", new int[]{});
    assertEquals(112, com.carrotsearch.sizeof.RamUsageEstimator.sizeOf(person1));

    Person person2 = new Person(1, "tom", new int[]{100});
    assertEquals(120, com.carrotsearch.sizeof.RamUsageEstimator.sizeOf(person2));

    Person person3 = new Person(1, "tom", new int[]{100, 100});
    assertEquals(120, com.carrotsearch.sizeof.RamUsageEstimator.sizeOf(person3));
    Person person4 = new Person(1, "tom", new int[]{100, 100, 100});
    assertEquals(128, com.carrotsearch.sizeof.RamUsageEstimator.sizeOf(person4));
    Person person5 = new Person(1, "tom", new int[]{100, 100, 100, 100});
    assertEquals(128, com.carrotsearch.sizeof.RamUsageEstimator.sizeOf(person5));
    Person person6 = new Person(1, "tom", new int[]{100, 100, 100, 100, 100});
    assertEquals(136, com.carrotsearch.sizeof.RamUsageEstimator.sizeOf(person6));

  }

  class Person {

    int age;
    String name;
    int[] scores;

    public Person() {
    }

    public Person(int age, String name, int[] scores) {
      this.age = age;
      this.name = name;
      this.scores = scores;
    }
  }

}
