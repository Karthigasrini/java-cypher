
package org.cypher.core.capsule.utils;

import com.google.protobuf.ByteString;
import org.cypher.common.utils.ByteArray;
import org.cypher.protos.Protocol.TXOutput;

public class TxOutputUtil {

  /**
   * new transaction output.
   *
   * @param value int value
   * @param address String address
   * @return {@link TXOutput}
   */
  public static TXOutput newTxOutput(long value, String address) {
    return TXOutput.newBuilder()
        .setValue(value)
        .setPubKeyHash(ByteString.copyFrom(ByteArray.fromHexString(address)))
        .build();
  }
}
