
package org.cypher.core;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.cypher.common.utils.ByteArray;
import org.cypher.core.capsule.TxOutputCapsule;

@Slf4j
public class TxOutputCapsuleTest {

  @Test
  public void testTxOutputCapsule() {
    long value = 123456L;
    String address = "3450dde5007c67a50ec2e09489fa53ec1ff59c61e7ddea9638645e6e5f62e5f5";
    TxOutputCapsule txOutputCapsule = new TxOutputCapsule(value, address);

    Assert.assertEquals(value, txOutputCapsule.getTxOutput().getValue());
    Assert.assertEquals(address,
        ByteArray.toHexString(txOutputCapsule.getTxOutput().getPubKeyHash().toByteArray()));
    Assert.assertTrue(txOutputCapsule.validate());

    long value3 = 9852448L;
    String address3 = "0xfd1a5decba973b0d31e84e7d8f4a5b10d33ab37ce6533f1ff5a9db2d9db8ef";
    String address4 = "fd1a5decba973b0d31e84e7d8f4a5b10d33ab37ce6533f1ff5a9db2d9db8ef";
    TxOutputCapsule txOutputCapsule2 = new TxOutputCapsule(value3, address3);

    Assert.assertEquals(value3, txOutputCapsule2.getTxOutput().getValue());
    Assert.assertEquals(address4,
        ByteArray.toHexString(txOutputCapsule2.getTxOutput().getPubKeyHash().toByteArray()));
    Assert.assertTrue(txOutputCapsule2.validate());

    long value5 = 67549L;
    String address5 = null;
    TxOutputCapsule txOutputCapsule3 = new TxOutputCapsule(value5, address5);

    Assert.assertEquals(value5, txOutputCapsule3.getTxOutput().getValue());
    Assert.assertEquals("",
        ByteArray.toHexString(txOutputCapsule3.getTxOutput().getPubKeyHash().toByteArray()));
    Assert.assertTrue(txOutputCapsule3.validate());

  }

}

