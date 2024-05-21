
package org.cypher.common.utils;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.cypher.common.utils.TypeConversion.bytesToHexString;
import static org.cypher.common.utils.TypeConversion.bytesToLong;
import static org.cypher.common.utils.TypeConversion.hexStringToBytes;
import static org.cypher.common.utils.TypeConversion.longToBytes;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

@Slf4j
public class TypeConversionTest {

  @Test
  public void testLongToBytes() {
    byte[] result = longToBytes(123L);
    //logger.info("long 123 to bytes is: {}", result);
    byte[] expected = new byte[]{0, 0, 0, 0, 0, 0, 0, 123};
    assertArrayEquals(expected, result);

  }

  @Test
  public void testBytesToLong() {
    long result = bytesToLong(new byte[]{0, 0, 0, 0, 0, 0, 0, 124});
    //logger.info("bytes 124 to long is: {}", result);
    assertEquals(124L, result);

  }

  @Test
  public void testBytesToHexString() {
    String result = bytesToHexString(new byte[]{0, 0, 0, 0, 0, 0, 0, 125});
    //logger.info("bytes 125 to hex string is: {}", result);
    assertEquals("000000000000007d", result);
  }

  @Test
  public void testHexStringToBytes() {
    byte[] result = hexStringToBytes("7f");
    //logger.info("hex string 7f to bytes is: {}", result);
    byte[] expected = new byte[]{127};
    assertArrayEquals(expected, result);

  }
}
