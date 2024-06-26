package org.cypher.core.actuator;

import static org.cypher.core.config.Parameter.ChainConstant.FROZEN_PERIOD;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.cypher.common.utils.Commons;
import org.cypher.common.utils.DecodeUtil;
import org.cypher.core.capsule.AccountCapsule;
import org.cypher.core.capsule.AssetIssueCapsule;
import org.cypher.core.capsule.TransactionResultCapsule;
import org.cypher.core.exception.BalanceInsufficientException;
import org.cypher.core.exception.ContractExeException;
import org.cypher.core.exception.ContractValidateException;
import org.cypher.core.store.AccountStore;
import org.cypher.core.store.AssetIssueStore;
import org.cypher.core.store.AssetIssueV2Store;
import org.cypher.core.store.DynamicPropertiesStore;
import org.cypher.core.utils.TransactionUtil;
import org.cypher.protos.Protocol.Account.Frozen;
import org.cypher.protos.Protocol.Transaction.Contract.ContractType;
import org.cypher.protos.Protocol.Transaction.Result.code;
import org.cypher.protos.contract.AssetIssueContractOuterClass.AssetIssueContract;
import org.cypher.protos.contract.AssetIssueContractOuterClass.AssetIssueContract.FrozenSupply;

@Slf4j(topic = "actuator")
public class AssetIssueActuator extends AbstractActuator {

  public AssetIssueActuator() {
    super(ContractType.AssetIssueContract, AssetIssueContract.class);
  }

  @Override
  public boolean execute(Object result) throws ContractExeException {
    TransactionResultCapsule ret = (TransactionResultCapsule) result;
    if (Objects.isNull(ret)) {
      throw new RuntimeException(ActuatorConstant.TX_RESULT_NULL);
    }

    long fee = calcFee();
    DynamicPropertiesStore dynamicStore = chainBaseManager.getDynamicPropertiesStore();
    AssetIssueStore assetIssueStore = chainBaseManager.getAssetIssueStore();
    AssetIssueV2Store assetIssueV2Store = chainBaseManager.getAssetIssueV2Store();
    AccountStore accountStore = chainBaseManager.getAccountStore();
    try {
      AssetIssueContract assetIssueContract = any.unpack(AssetIssueContract.class);
      byte[] ownerAddress = assetIssueContract.getOwnerAddress().toByteArray();
      AssetIssueCapsule assetIssueCapsule = new AssetIssueCapsule(assetIssueContract);
      AssetIssueCapsule assetIssueCapsuleV2 = new AssetIssueCapsule(assetIssueContract);
      long tokenIdNum = dynamicStore.getTokenIdNum();
      tokenIdNum++;
      assetIssueCapsule.setId(Long.toString(tokenIdNum));
      assetIssueCapsuleV2.setId(Long.toString(tokenIdNum));
      dynamicStore.saveTokenIdNum(tokenIdNum);

      if (dynamicStore.getAllowSameTokenName() == 0) {
        assetIssueCapsuleV2.setPrecision(0);
        assetIssueStore
            .put(assetIssueCapsule.createDbKey(), assetIssueCapsule);
        assetIssueV2Store
            .put(assetIssueCapsuleV2.createDbV2Key(), assetIssueCapsuleV2);
      } else {
        assetIssueV2Store
            .put(assetIssueCapsuleV2.createDbV2Key(), assetIssueCapsuleV2);
      }

      Commons.adjustBalance(accountStore, ownerAddress, -fee);
      if (dynamicStore.supportBlackHoleOptimization()) {
        dynamicStore.burnCyp(fee);
      } else {
        Commons.adjustBalance(accountStore, accountStore.getBlackhole(), fee);//send to blackhole
      }
      AccountCapsule accountCapsule = accountStore.get(ownerAddress);
      List<FrozenSupply> frozenSupplyList = assetIssueContract.getFrozenSupplyList();
      Iterator<FrozenSupply> iterator = frozenSupplyList.iterator();
      long remainSupply = assetIssueContract.getTotalSupply();
      List<Frozen> frozenList = new ArrayList<>();
      long startTime = assetIssueContract.getStartTime();

      while (iterator.hasNext()) {
        FrozenSupply next = iterator.next();
        long expireTime = startTime + next.getFrozenDays() * FROZEN_PERIOD;
        Frozen newFrozen = Frozen.newBuilder()
            .setFrozenBalance(next.getFrozenAmount())
            .setExpireTime(expireTime)
            .build();
        frozenList.add(newFrozen);
        remainSupply -= next.getFrozenAmount();
      }

      if (dynamicStore.getAllowSameTokenName() == 0) {
        accountCapsule.addAsset(assetIssueCapsule.createDbKey(), remainSupply);
      }
      accountCapsule.setAssetIssuedName(assetIssueCapsule.createDbKey());
      accountCapsule.setAssetIssuedID(assetIssueCapsule.createDbV2Key());
      accountCapsule.addAssetV2(assetIssueCapsuleV2.createDbV2Key(), remainSupply);
      accountCapsule.setInstance(accountCapsule.getInstance().toBuilder()
          .addAllFrozenSupply(frozenList).build());

      accountStore.put(ownerAddress, accountCapsule);

      ret.setAssetIssueID(Long.toString(tokenIdNum));
      ret.setStatus(fee, code.SUCESS);
    } catch (InvalidProtocolBufferException | BalanceInsufficientException | ArithmeticException e) {
      logger.debug(e.getMessage(), e);
      ret.setStatus(fee, code.FAILED);
      throw new ContractExeException(e.getMessage());
    }

    return true;
  }

  @Override
  public boolean validate() throws ContractValidateException {
    if (this.any == null) {
      throw new ContractValidateException(ActuatorConstant.CONTRACT_NOT_EXIST);
    }
    if (chainBaseManager == null) {
      throw new ContractValidateException(ActuatorConstant.STORE_NOT_EXIST);
    }
    DynamicPropertiesStore dynamicStore = chainBaseManager.getDynamicPropertiesStore();
    AssetIssueStore assetIssueStore = chainBaseManager.getAssetIssueStore();
    AccountStore accountStore = chainBaseManager.getAccountStore();
    if (!this.any.is(AssetIssueContract.class)) {
      throw new ContractValidateException(
          "contract type error,expected type [AssetIssueContract],real type[" + any
              .getClass() + "]");
    }

    final AssetIssueContract assetIssueContract;
    try {
      assetIssueContract = this.any.unpack(AssetIssueContract.class);
    } catch (InvalidProtocolBufferException e) {
      logger.debug(e.getMessage(), e);
      throw new ContractValidateException(e.getMessage());
    }

    byte[] ownerAddress = assetIssueContract.getOwnerAddress().toByteArray();
    if (!DecodeUtil.addressValid(ownerAddress)) {
      throw new ContractValidateException("Invalid ownerAddress");
    }

    if (!TransactionUtil.validAssetName(assetIssueContract.getName().toByteArray())) {
      throw new ContractValidateException("Invalid assetName");
    }

    if (dynamicStore.getAllowSameTokenName() != 0) {
      String name = assetIssueContract.getName().toStringUtf8().toLowerCase();
      if (("cyp").equals(name)) {
        throw new ContractValidateException("assetName can't be cyp");
      }
    }

    int precision = assetIssueContract.getPrecision();
    if (precision != 0
        && dynamicStore.getAllowSameTokenName() != 0
        && (precision < 0 || precision > ActuatorConstant.PRECISION_DECIMAL)) {
      throw new ContractValidateException("precision cannot exceed 6");
    }

    if ((!assetIssueContract.getAbbr().isEmpty()) && !TransactionUtil
        .validAssetName(assetIssueContract.getAbbr().toByteArray())) {
      throw new ContractValidateException("Invalid abbreviation for token");
    }

    if (!TransactionUtil.validUrl(assetIssueContract.getUrl().toByteArray())) {
      throw new ContractValidateException("Invalid url");
    }

    if (!TransactionUtil
        .validAssetDescription(assetIssueContract.getDescription().toByteArray())) {
      throw new ContractValidateException("Invalid description");
    }

    if (assetIssueContract.getStartTime() == 0) {
      throw new ContractValidateException("Start time should be not empty");
    }
    if (assetIssueContract.getEndTime() == 0) {
      throw new ContractValidateException("End time should be not empty");
    }
    if (assetIssueContract.getEndTime() <= assetIssueContract.getStartTime()) {
      throw new ContractValidateException("End time should be greater than start time");
    }
    if (assetIssueContract.getStartTime() <= dynamicStore.getLatestBlockHeaderTimestamp()) {
      throw new ContractValidateException("Start time should be greater than HeadBlockTime");
    }

    if (dynamicStore.getAllowSameTokenName() == 0
        && assetIssueStore.get(assetIssueContract.getName().toByteArray())
        != null) {
      throw new ContractValidateException("Token exists");
    }

    if (assetIssueContract.getTotalSupply() <= 0) {
      throw new ContractValidateException("TotalSupply must greater than 0!");
    }

    if (assetIssueContract.getCypNum() <= 0) {
      throw new ContractValidateException("CypNum must greater than 0!");
    }

    if (assetIssueContract.getNum() <= 0) {
      throw new ContractValidateException("Num must greater than 0!");
    }

    if (assetIssueContract.getPublicFreeAssetNetUsage() != 0) {
      throw new ContractValidateException("PublicFreeAssetNetUsage must be 0!");
    }

    if (assetIssueContract.getFrozenSupplyCount()
        > dynamicStore.getMaxFrozenSupplyNumber()) {
      throw new ContractValidateException("Frozen supply list length is too long");
    }

    if (assetIssueContract.getFreeAssetNetLimit() < 0
        || assetIssueContract.getFreeAssetNetLimit() >=
        dynamicStore.getOneDayNetLimit()) {
      throw new ContractValidateException("Invalid FreeAssetNetLimit");
    }

    if (assetIssueContract.getPublicFreeAssetNetLimit() < 0
        || assetIssueContract.getPublicFreeAssetNetLimit() >=
        dynamicStore.getOneDayNetLimit()) {
      throw new ContractValidateException("Invalid PublicFreeAssetNetLimit");
    }

    long remainSupply = assetIssueContract.getTotalSupply();
    long minFrozenSupplyTime = dynamicStore.getMinFrozenSupplyTime();
    long maxFrozenSupplyTime = dynamicStore.getMaxFrozenSupplyTime();
    List<FrozenSupply> frozenList = assetIssueContract.getFrozenSupplyList();
    Iterator<FrozenSupply> iterator = frozenList.iterator();

    while (iterator.hasNext()) {
      FrozenSupply next = iterator.next();
      if (next.getFrozenAmount() <= 0) {
        throw new ContractValidateException("Frozen supply must be greater than 0!");
      }
      if (next.getFrozenAmount() > remainSupply) {
        throw new ContractValidateException("Frozen supply cannot exceed total supply");
      }
      if (!(next.getFrozenDays() >= minFrozenSupplyTime
          && next.getFrozenDays() <= maxFrozenSupplyTime)) {
        throw new ContractValidateException(
            "frozenDuration must be less than " + maxFrozenSupplyTime + " days "
                + "and more than " + minFrozenSupplyTime + " days");
      }
      remainSupply -= next.getFrozenAmount();
    }

    AccountCapsule accountCapsule = accountStore.get(ownerAddress);
    if (accountCapsule == null) {
      throw new ContractValidateException("Account not exists");
    }

    if (!accountCapsule.getAssetIssuedName().isEmpty()) {
      throw new ContractValidateException("An account can only issue one asset");
    }

    if (accountCapsule.getBalance() < calcFee()) {
      throw new ContractValidateException("No enough balance for fee!");
    }
    return true;
  }

  @Override
  public ByteString getOwnerAddress() throws InvalidProtocolBufferException {
    return any.unpack(AssetIssueContract.class).getOwnerAddress();
  }

  @Override
  public long calcFee() {
    return chainBaseManager.getDynamicPropertiesStore().getAssetIssueFee();
  }

}
