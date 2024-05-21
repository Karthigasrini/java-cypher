
package org.cypher.core.capsule;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.cypher.common.utils.ByteArray;
import org.cypher.core.store.DynamicPropertiesStore;
import org.cypher.protos.contract.AssetIssueContractOuterClass.AssetIssueContract;
import org.cypher.protos.contract.AssetIssueContractOuterClass.AssetIssueContract.FrozenSupply;

@Slf4j(topic = "capsule")
public class AssetIssueCapsule implements ProtoCapsule<AssetIssueContract> {

  private AssetIssueContract assetIssueContract;

  /**
   * get asset issue contract from bytes data.
   */
  public AssetIssueCapsule(byte[] data) {
    try {
      this.assetIssueContract = AssetIssueContract.parseFrom(data);
    } catch (InvalidProtocolBufferException e) {
      logger.debug(e.getMessage());
    }
  }

  public AssetIssueCapsule(AssetIssueContract assetIssueContract) {
    this.assetIssueContract = assetIssueContract;
  }

  public AssetIssueCapsule(byte[] ownerAddress, String id, String name, String abbr,
                           long totalSupply, int precision) {
    this.assetIssueContract = AssetIssueContract.newBuilder()
            .setOwnerAddress(ByteString.copyFrom(ownerAddress)).setId(id)
            .setName(ByteString.copyFrom(name.getBytes())).setAbbr(ByteString.copyFrom(abbr.getBytes()))
            .setTotalSupply(totalSupply).setPrecision(precision).build();
  }

  public static String createDbKeyString(String name, long order) {
    return name + "_" + order;
  }

  public byte[] getData() {
    return this.assetIssueContract.toByteArray();
  }

  @Override
  public AssetIssueContract getInstance() {
    return this.assetIssueContract;
  }

  @Override
  public String toString() {
    return this.assetIssueContract.toString();
  }

  public ByteString getName() {
    return this.assetIssueContract.getName();
  }

  public String getId() {
    return this.assetIssueContract.getId();
  }

  public void setId(String id) {
    this.assetIssueContract = this.assetIssueContract.toBuilder()
        .setId(id)
        .build();
  }

  public int getPrecision() {
    return this.assetIssueContract.getPrecision();
  }

  public void setPrecision(int precision) {
    this.assetIssueContract = this.assetIssueContract.toBuilder()
        .setPrecision(precision)
        .build();
  }

  public long getOrder() {
    return this.assetIssueContract.getOrder();
  }

  public void setOrder(long order) {
    this.assetIssueContract = this.assetIssueContract.toBuilder()
        .setOrder(order)
        .build();
  }

  public byte[] createDbV2Key() {
    return ByteArray.fromString(this.assetIssueContract.getId());
  }

  public byte[] createDbKey() {
//    long order = getOrder();
//    if (order == 0) {
//      return getName().toByteArray();
//    }
//    String name = new String(getName().toByteArray(), Charset.forName("UTF-8"));
//    String nameKey = createDbKeyString(name, order);
//    return nameKey.getBytes();
    return getName().toByteArray();
  }

  public byte[] createDbKeyFinal(DynamicPropertiesStore dynamicPropertiesStore) {
    if (dynamicPropertiesStore.getAllowSameTokenName() == 0) {
      return createDbKey();
    } else {
      return createDbV2Key();
    }
  }

  public int getNum() {
    return this.assetIssueContract.getNum();
  }

  public int getCypNum() {
    return this.assetIssueContract.getCypNum();
  }

  public long getStartTime() {
    return this.assetIssueContract.getStartTime();
  }

  public long getEndTime() {
    return this.assetIssueContract.getEndTime();
  }

  public ByteString getOwnerAddress() {
    return this.assetIssueContract.getOwnerAddress();
  }

  public int getFrozenSupplyCount() {
    return getInstance().getFrozenSupplyCount();
  }

  public List<FrozenSupply> getFrozenSupplyList() {
    return getInstance().getFrozenSupplyList();
  }

  public long getFrozenSupply() {
    List<FrozenSupply> frozenList = getFrozenSupplyList();
    final long[] frozenBalance = {0};
    frozenList.forEach(frozen -> frozenBalance[0] = Long.sum(frozenBalance[0],
        frozen.getFrozenAmount()));
    return frozenBalance[0];
  }

  public long getFreeAssetNetLimit() {
    return this.assetIssueContract.getFreeAssetNetLimit();
  }

  public void setFreeAssetNetLimit(long newLimit) {
    this.assetIssueContract = this.assetIssueContract.toBuilder()
        .setFreeAssetNetLimit(newLimit).build();
  }

  public long getPublicFreeAssetNetLimit() {
    return this.assetIssueContract.getPublicFreeAssetNetLimit();
  }

  public void setPublicFreeAssetNetLimit(long newPublicLimit) {
    this.assetIssueContract = this.assetIssueContract.toBuilder()
        .setPublicFreeAssetNetLimit(newPublicLimit).build();
  }

  public long getPublicFreeAssetNetUsage() {
    return this.assetIssueContract.getPublicFreeAssetNetUsage();
  }

  public void setPublicFreeAssetNetUsage(long value) {
    this.assetIssueContract = this.assetIssueContract.toBuilder()
        .setPublicFreeAssetNetUsage(value).build();
  }

  public long getPublicLatestFreeNetTime() {
    return this.assetIssueContract.getPublicLatestFreeNetTime();
  }

  public void setPublicLatestFreeNetTime(long time) {
    this.assetIssueContract = this.assetIssueContract.toBuilder()
        .setPublicLatestFreeNetTime(time).build();
  }

  public void setUrl(ByteString newUrl) {
    this.assetIssueContract = this.assetIssueContract.toBuilder()
        .setUrl(newUrl).build();
  }

  public ByteString getUrl() {
    return this.assetIssueContract.getUrl();
  }

  public void setDescription(ByteString description) {
    this.assetIssueContract = this.assetIssueContract.toBuilder()
        .setDescription(description).build();
  }

  public ByteString getDesc() {
    return this.assetIssueContract.getDescription();
  }
}
