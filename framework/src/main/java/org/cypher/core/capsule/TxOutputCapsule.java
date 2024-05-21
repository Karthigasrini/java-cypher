

package org.cypher.core.capsule;

import com.google.protobuf.ByteString;
import org.cypher.common.utils.ByteArray;
import org.cypher.protos.Protocol.TXOutput;

public class TxOutputCapsule implements ProtoCapsule<TXOutput> {

  private TXOutput txOutput;

  /**
   * constructor TxOutputCapsule.
   *
   * @param value int value
   * @param address String address
   */
  public TxOutputCapsule(long value, String address) {
    this.txOutput = TXOutput.newBuilder()
        .setValue(value)
        .setPubKeyHash(ByteString.copyFrom(ByteArray.fromHexString(address)))
        .build();
  }

  public TXOutput getTxOutput() {
    return txOutput;
  }

  /**
   * validateSignature.
   */
  public boolean validate() {
    return true;
  }

  @Override
  public byte[] getData() {
    return new byte[0];
  }

  @Override
  public TXOutput getInstance() {
    return this.txOutput;
  }
}
