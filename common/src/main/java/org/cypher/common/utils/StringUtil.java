
package org.cypher.common.utils;

import com.google.protobuf.ByteString;
import org.cypher.common.parameter.CommonParameter;

public class StringUtil {

  public static byte[] createDbKey(ByteString string) {
    return string.toByteArray();
  }

  public static String createReadableString(byte[] bytes) {
    return ByteArray.toHexString(bytes);
  }

  public static String encode58Check(byte[] input) {
    byte[] hash0 = Sha256Hash.hash(CommonParameter.getInstance().isECKeyCryptoEngine(), input);
    byte[] hash1 = Sha256Hash.hash(CommonParameter.getInstance().isECKeyCryptoEngine(), hash0);
    byte[] inputCheck = new byte[input.length + 4];
    System.arraycopy(input, 0, inputCheck, 0, input.length);
    System.arraycopy(hash1, 0, inputCheck, input.length, 4);
    return Base58.encode(inputCheck);
  }

  public static String createReadableString(ByteString string) {
    return createReadableString(string.toByteArray());
  }

  public static ByteString hexString2ByteString(String hexString) {
    return ByteString.copyFrom(ByteArray.fromHexString(hexString));
  }
}
