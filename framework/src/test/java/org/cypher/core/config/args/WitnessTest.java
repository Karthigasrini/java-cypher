

package org.cypher.core.config.args;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.cypher.common.args.Witness;
import org.cypher.common.utils.ByteArray;
import org.cypher.core.Wallet;

public class WitnessTest {

  private Witness witness = new Witness();

  /**
   * init witness.
   */
  @Before
  public void setWitness() {
    witness
        .setAddress(ByteArray.fromHexString(
            Wallet.getAddressPreFixString() + "448d53b2df0cd78158f6f0aecdf60c1c10b15413"));
    witness.setUrl("http://Uranus.org");
    witness.setVoteCount(1000L);
  }

  @Test(expected = IllegalArgumentException.class)
  public void whenSetNullAddressShouldThrowIllegalArgumentException() {
    witness.setAddress(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void whenSetEmptyAddressShouldThrowIllegalArgumentException() {
    witness.setAddress(new byte[0]);
  }

  @Test(expected = IllegalArgumentException.class)
  public void whenSetBadFormatAddressShouldThrowIllegalArgumentException() {
    witness
        .setAddress(ByteArray.fromHexString("558d53b2df0cd78158f6f0aecdf60c1c10b15413"));
  }

  @Test
  public void setAddressRight() {
    witness
        .setAddress(ByteArray.fromHexString(
            Wallet.getAddressPreFixString() + "558d53b2df0cd78158f6f0aecdf60c1c10b15413"));
    Assert.assertEquals(
        Wallet.getAddressPreFixString() + "558d53b2df0cd78158f6f0aecdf60c1c10b15413",
        ByteArray.toHexString(witness.getAddress()));
  }

  @Test(expected = IllegalArgumentException.class)
  public void whenSetNullUrlShouldThrowIllegalArgumentException() {
    witness.setUrl(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void whenSetEmptyUrlShouldThrowIllegalArgumentException() {
    witness.setUrl("");
  }

  @Test
  public void setUrlRight() {
    witness.setUrl("afwe");
  }

  @Test
  public void setVoteCountRight() {
    witness.setVoteCount(Long.MAX_VALUE);
    Assert.assertEquals(Long.MAX_VALUE, witness.getVoteCount());

    witness.setVoteCount(Long.MIN_VALUE);
    Assert.assertEquals(Long.MIN_VALUE, witness.getVoteCount());

    witness.setVoteCount(1000L);
    Assert.assertEquals(1000L, witness.getVoteCount());
  }

  @Test
  public void getVoteCountRight() {
    Assert.assertEquals(1000L, witness.getVoteCount());
  }
}
