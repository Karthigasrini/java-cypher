

package org.cypher.core.capsule.utils;

import com.google.protobuf.ByteString;
import org.cypher.protos.Protocol.TXInput;

public class TxInputUtil {

  /**
   * new transaction input.
   *
   * @param txId byte[] txId
   * @param vout int vout
   * @param signature byte[] signature
   * @param pubKey byte[] pubKey
   * @return {@link TXInput}
   */
  public static TXInput newTxInput(byte[] txId, long vout, byte[]
      signature, byte[] pubKey) {

    TXInput.raw.Builder rawBuilder = TXInput.raw.newBuilder();

    TXInput.raw rawData = rawBuilder
        .setTxID(ByteString.copyFrom(txId))
        .setVout(vout)
        .setPubKey(ByteString.copyFrom(pubKey)).build();

    return TXInput.newBuilder()
        .setSignature(ByteString.copyFrom(signature))
        .setRawData(rawData).build();
  }
}
