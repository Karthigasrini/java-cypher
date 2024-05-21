
package org.cypher.core.actuator;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.Arrays;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.cypher.common.utils.ByteArray;
import org.cypher.common.utils.Commons;
import org.cypher.common.utils.DecodeUtil;
import org.cypher.core.capsule.AccountCapsule;
import org.cypher.core.capsule.AssetIssueCapsule;
import org.cypher.core.capsule.TransactionResultCapsule;
import org.cypher.core.exception.ContractExeException;
import org.cypher.core.exception.ContractValidateException;
import org.cypher.core.store.AccountStore;
import org.cypher.core.store.AssetIssueStore;
import org.cypher.core.store.AssetIssueV2Store;
import org.cypher.core.store.DynamicPropertiesStore;
import org.cypher.protos.Protocol;
import org.cypher.protos.Protocol.Transaction.Contract.ContractType;
import org.cypher.protos.Protocol.Transaction.Result.code;
import org.cypher.protos.contract.AssetIssueContractOuterClass.ParticipateAssetIssueContract;

@Slf4j(topic = "actuator")
public class ParticipateAssetIssueActuator extends AbstractActuator {

  public ParticipateAssetIssueActuator() {
    super(ContractType.ParticipateAssetIssueContract, ParticipateAssetIssueContract.class);
  }

  @Override
  public boolean execute(Object object) throws ContractExeException {
    TransactionResultCapsule ret = (TransactionResultCapsule) object;
    if (Objects.isNull(ret)) {
      throw new RuntimeException(ActuatorConstant.TX_RESULT_NULL);
    }

    long fee = calcFee();
    AccountStore accountStore = chainBaseManager.getAccountStore();
    DynamicPropertiesStore dynamicStore = chainBaseManager.getDynamicPropertiesStore();
    AssetIssueStore assetIssueStore = chainBaseManager.getAssetIssueStore();
    AssetIssueV2Store assetIssueV2Store = chainBaseManager.getAssetIssueV2Store();
    try {
      final ParticipateAssetIssueContract participateAssetIssueContract =
          any.unpack(ParticipateAssetIssueContract.class);
      long cost = participateAssetIssueContract.getAmount();

      //subtract from owner address
      byte[] ownerAddress = participateAssetIssueContract.getOwnerAddress().toByteArray();
      AccountCapsule ownerAccount = accountStore.get(ownerAddress);
      long balance = Math.subtractExact(ownerAccount.getBalance(), cost);
      balance = Math.subtractExact(balance, fee);
      ownerAccount.setBalance(balance);
      byte[] key = participateAssetIssueContract.getAssetName().toByteArray();

      //calculate the exchange amount
      AssetIssueCapsule assetIssueCapsule;
      assetIssueCapsule = Commons
          .getAssetIssueStoreFinal(dynamicStore, assetIssueStore, assetIssueV2Store).get(key);

      long exchangeAmount = Math.multiplyExact(cost, assetIssueCapsule.getNum());
      exchangeAmount = Math.floorDiv(exchangeAmount, assetIssueCapsule.getCypNum());
      ownerAccount.addAssetAmountV2(key, exchangeAmount, dynamicStore, assetIssueStore);

      //add to to_address
      byte[] toAddress = participateAssetIssueContract.getToAddress().toByteArray();
      AccountCapsule toAccount = accountStore.get(toAddress);
      toAccount.setBalance(Math.addExact(toAccount.getBalance(), cost));
      if (!toAccount.reduceAssetAmountV2(key, exchangeAmount, dynamicStore, assetIssueStore)) {
        throw new ContractExeException("reduceAssetAmount failed !");
      }

      //write to db
      accountStore.put(ownerAddress, ownerAccount);
      accountStore.put(toAddress, toAccount);
      ret.setStatus(fee, Protocol.Transaction.Result.code.SUCESS);
    } catch (InvalidProtocolBufferException | ArithmeticException e) {
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
    AccountStore accountStore = chainBaseManager.getAccountStore();
    DynamicPropertiesStore dynamicStore = chainBaseManager.getDynamicPropertiesStore();
    AssetIssueStore assetIssueStore = chainBaseManager.getAssetIssueStore();
    AssetIssueV2Store assetIssueV2Store = chainBaseManager.getAssetIssueV2Store();
    if (!this.any.is(ParticipateAssetIssueContract.class)) {
      throw new ContractValidateException(
          "contract type error,expected type [ParticipateAssetIssueContract],real type[" + any
              .getClass() + "]");
    }

    final ParticipateAssetIssueContract participateAssetIssueContract;
    try {
      participateAssetIssueContract =
          this.any.unpack(ParticipateAssetIssueContract.class);
    } catch (InvalidProtocolBufferException e) {
      logger.debug(e.getMessage(), e);
      throw new ContractValidateException(e.getMessage());
    }
    //Parameters check
    byte[] ownerAddress = participateAssetIssueContract.getOwnerAddress().toByteArray();
    byte[] toAddress = participateAssetIssueContract.getToAddress().toByteArray();
    byte[] assetName = participateAssetIssueContract.getAssetName().toByteArray();
    long amount = participateAssetIssueContract.getAmount();

    if (!DecodeUtil.addressValid(ownerAddress)) {
      throw new ContractValidateException("Invalid ownerAddress");
    }
    if (!DecodeUtil.addressValid(toAddress)) {
      throw new ContractValidateException("Invalid toAddress");
    }
//    if (!TransactionUtil.validAssetName(assetName)) {
//      throw new ContractValidateException("Invalid assetName");
//    }
    if (amount <= 0) {
      throw new ContractValidateException("Amount must greater than 0!");
    }

    if (Arrays.equals(ownerAddress, toAddress)) {
      throw new ContractValidateException("Cannot participate asset Issue yourself !");
    }

    //Whether the accountStore exist
    AccountCapsule ownerAccount = accountStore.get(ownerAddress);
    if (ownerAccount == null) {
      throw new ContractValidateException("Account does not exist!");
    }
    try {
      //Whether the balance is enough
      long fee = calcFee();
      if (ownerAccount.getBalance() < Math.addExact(amount, fee)) {
        throw new ContractValidateException("No enough balance !");
      }

      //Whether have the mapping
      AssetIssueCapsule assetIssueCapsule;
      assetIssueCapsule = Commons
          .getAssetIssueStoreFinal(dynamicStore, assetIssueStore, assetIssueV2Store).get(assetName);
      if (assetIssueCapsule == null) {
        throw new ContractValidateException("No asset named " + ByteArray.toStr(assetName));
      }

      if (!Arrays.equals(toAddress, assetIssueCapsule.getOwnerAddress().toByteArray())) {
        throw new ContractValidateException(
            "The asset is not issued by " + ByteArray.toHexString(toAddress));
      }
      //Whether the exchange can be processed: to see if the exchange can be the exact int
      long now = dynamicStore.getLatestBlockHeaderTimestamp();
      if (now >= assetIssueCapsule.getEndTime() || now < assetIssueCapsule
          .getStartTime()) {
        throw new ContractValidateException("No longer valid period!");
      }

      int cypNum = assetIssueCapsule.getCypNum();
      int num = assetIssueCapsule.getNum();
      long exchangeAmount = Math.multiplyExact(amount, num);
      exchangeAmount = Math.floorDiv(exchangeAmount, cypNum);
      if (exchangeAmount <= 0) {
        throw new ContractValidateException("Can not process the exchange!");
      }

      AccountCapsule toAccount = accountStore.get(toAddress);
      if (toAccount == null) {
        throw new ContractValidateException("To account does not exist!");
      }

      if (!toAccount.assetBalanceEnoughV2(assetName, exchangeAmount,
          dynamicStore)) {
        throw new ContractValidateException("Asset balance is not enough !");
      }
    } catch (ArithmeticException e) {
      logger.debug(e.getMessage(), e);
      throw new ContractValidateException(e.getMessage());
    }

    return true;
  }

  @Override
  public ByteString getOwnerAddress() throws InvalidProtocolBufferException {
    return this.any.unpack(ParticipateAssetIssueContract.class).getOwnerAddress();
  }

  @Override
  public long calcFee() {
    return 0;
  }
}
