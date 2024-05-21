package org.cypher.common.utils;

import com.google.common.collect.Lists;
import java.util.List;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.cypher.common.crypto.ECKey;
import org.cypher.common.crypto.SignInterface;
import org.cypher.common.crypto.SignUtils;
import org.cypher.core.config.Parameter.ChainConstant;

@Slf4j(topic = "app")
public class LocalWitnesses {

  @Getter
  private List<String> privateKeys = Lists.newArrayList();

  private byte[] witnessAccountAddress;

  public LocalWitnesses() {
  }

  public LocalWitnesses(String privateKey) {
    addPrivateKeys(privateKey);
  }

  public LocalWitnesses(List<String> privateKeys) {
    setPrivateKeys(privateKeys);
  }

  public byte[] getWitnessAccountAddress(boolean isECKeyCryptoEngine) {
    if (witnessAccountAddress == null) {
      byte[] privateKey = ByteArray.fromHexString(getPrivateKey());
      final SignInterface cryptoEngine = SignUtils.fromPrivate(privateKey, isECKeyCryptoEngine);
      this.witnessAccountAddress = cryptoEngine.getAddress();
    }
    return witnessAccountAddress;
  }

  public void setWitnessAccountAddress(final byte[] localWitnessAccountAddress) {
    this.witnessAccountAddress = localWitnessAccountAddress;
  }

  public void initWitnessAccountAddress(boolean isECKeyCryptoEngine) {
    if (witnessAccountAddress == null) {
      byte[] privateKey = ByteArray.fromHexString(getPrivateKey());
      final SignInterface ecKey = SignUtils.fromPrivate(privateKey,
          isECKeyCryptoEngine);
      this.witnessAccountAddress = ecKey.getAddress();
    }
  }

  /**
   * Private key of ECKey.
   */
  public void setPrivateKeys(final List<String> privateKeys) {
    if (CollectionUtils.isEmpty(privateKeys)) {
      return;
    }
    for (String privateKey : privateKeys) {
      validate(privateKey);
    }
    this.privateKeys = privateKeys;
  }

  private void validate(String privateKey) {
    if (StringUtils.startsWithIgnoreCase(privateKey, "0X")) {
      privateKey = privateKey.substring(2);
    }

    if (StringUtils.isNotBlank(privateKey)
        && privateKey.length() != ChainConstant.PRIVATE_KEY_LENGTH) {
      throw new IllegalArgumentException(
          "Private key(" + privateKey + ") must be " + ChainConstant.PRIVATE_KEY_LENGTH
              + "-bits hex string.");
    }
  }

  public void addPrivateKeys(String privateKey) {
    validate(privateKey);
    this.privateKeys.add(privateKey);
  }

  //get the first one recently
  public String getPrivateKey() {
    if (CollectionUtils.isEmpty(privateKeys)) {
      logger.warn("privateKey is null");
      return null;
    }
    return privateKeys.get(0);
  }

  public byte[] getPublicKey() {
    if (CollectionUtils.isEmpty(privateKeys)) {
      logger.warn("privateKey is null");
      return null;
    }
    byte[] privateKey = ByteArray.fromHexString(getPrivateKey());
    final ECKey ecKey = ECKey.fromPrivate(privateKey);
    return ecKey.getAddress();
  }

}
