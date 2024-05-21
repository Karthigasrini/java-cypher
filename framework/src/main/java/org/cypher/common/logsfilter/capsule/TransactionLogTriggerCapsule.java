package org.cypher.common.logsfilter.capsule;

import static org.cypher.protos.Protocol.Transaction.Contract.ContractType.CreateSmartContract;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.Hex;
import org.cypher.common.logsfilter.EventPluginLoader;
import org.cypher.common.logsfilter.trigger.InternalTransactionPojo;
import org.cypher.common.logsfilter.trigger.LogPojo;
import org.cypher.common.logsfilter.trigger.TransactionLogTrigger;
import org.cypher.common.runtime.InternalTransaction;
import org.cypher.common.runtime.ProgramResult;
import org.cypher.common.utils.StringUtil;
import org.cypher.core.capsule.BlockCapsule;
import org.cypher.core.capsule.TransactionCapsule;
import org.cypher.core.db.TransactionTrace;
import org.cypher.protos.Protocol;
import org.cypher.protos.Protocol.Transaction;
import org.cypher.protos.Protocol.Transaction.Contract.ContractType;
import org.cypher.protos.Protocol.TransactionInfo;
import org.cypher.protos.contract.AssetIssueContractOuterClass.TransferAssetContract;
import org.cypher.protos.contract.BalanceContract.TransferContract;
import org.cypher.protos.contract.SmartContractOuterClass.CreateSmartContract;
import org.cypher.protos.contract.SmartContractOuterClass.TriggerSmartContract;

@Slf4j
public class TransactionLogTriggerCapsule extends TriggerCapsule {

  @Getter
  @Setter
  private TransactionLogTrigger transactionLogTrigger;

  public TransactionLogTriggerCapsule(TransactionCapsule cypCapsule, BlockCapsule blockCapsule) {
    this(cypCapsule, blockCapsule, 0, 0, 0, null, 0);
  }

  public TransactionLogTriggerCapsule(TransactionCapsule cypCapsule, BlockCapsule blockCapsule,
      int txIndex, long preCumulativeEnergyUsed, long preCumulativeLogCount,
      TransactionInfo transactionInfo, long energyUnitPrice) {
    transactionLogTrigger = new TransactionLogTrigger();

    String blockHash = "";
    if (Objects.nonNull(blockCapsule)) {
      blockHash = blockCapsule.getBlockId().toString();
      transactionLogTrigger.setBlockHash(blockHash);
    }

    String transactionHash = cypCapsule.getTransactionId().toString();
    transactionLogTrigger.setTransactionId(transactionHash);
    transactionLogTrigger.setTimeStamp(blockCapsule.getTimeStamp());
    transactionLogTrigger.setBlockNumber(cypCapsule.getBlockNum());
    transactionLogTrigger.setData(Hex.toHexString(cypCapsule
        .getInstance().getRawData().getData().toByteArray()));

    TransactionTrace cypTrace = cypCapsule.getCypTrace();

    //result
    if (Objects.nonNull(cypCapsule.getContractRet())) {
      transactionLogTrigger.setResult(cypCapsule.getContractRet().toString());
    }

    Transaction.raw rawData = cypCapsule.getInstance().getRawData();
    ContractType contractType = null;

    if (Objects.nonNull(rawData)) {
      // fee limit
      transactionLogTrigger.setFeeLimit(rawData.getFeeLimit());

      Protocol.Transaction.Contract contract = rawData.getContract(0);
      Any contractParameter = null;

      // contract type
      if (Objects.nonNull(contract)) {
        contractType = contract.getType();
        if (Objects.nonNull(contractType)) {
          transactionLogTrigger.setContractType(contractType.toString());
        }

        contractParameter = contract.getParameter();

        transactionLogTrigger.setContractCallValue(TransactionCapsule.getCallValue(contract));
      }

      if (Objects.nonNull(contractParameter) && Objects.nonNull(contract)) {
        try {
          switch (contractType) {
            case TransferContract:
              TransferContract transferContract = contractParameter.unpack(TransferContract.class);

              if (Objects.nonNull(transferContract)) {
                transactionLogTrigger.setAssetName("cyp");

                if (Objects.nonNull(transferContract.getOwnerAddress())) {
                  transactionLogTrigger.setFromAddress(StringUtil
                      .encode58Check(transferContract.getOwnerAddress().toByteArray()));
                }

                if (Objects.nonNull(transferContract.getToAddress())) {
                  transactionLogTrigger.setToAddress(
                      StringUtil.encode58Check(transferContract.getToAddress().toByteArray()));
                }

                transactionLogTrigger.setAssetAmount(transferContract.getAmount());
              }
              break;
            case TransferAssetContract:
              TransferAssetContract transferAssetContract = contractParameter
                  .unpack(TransferAssetContract.class);

              if (Objects.nonNull(transferAssetContract)) {
                if (Objects.nonNull(transferAssetContract.getAssetName())) {
                  transactionLogTrigger
                      .setAssetName(transferAssetContract.getAssetName().toStringUtf8());
                }

                if (Objects.nonNull(transferAssetContract.getOwnerAddress())) {
                  transactionLogTrigger.setFromAddress(
                      StringUtil
                          .encode58Check(transferAssetContract.getOwnerAddress().toByteArray()));
                }

                if (Objects.nonNull(transferAssetContract.getToAddress())) {
                  transactionLogTrigger.setToAddress(StringUtil
                      .encode58Check(transferAssetContract.getToAddress().toByteArray()));
                }
                transactionLogTrigger.setAssetAmount(transferAssetContract.getAmount());
              }
              break;
            case TriggerSmartContract:
              TriggerSmartContract triggerSmartContract = contractParameter
                  .unpack(TriggerSmartContract.class);

              if (Objects.nonNull(triggerSmartContract.getOwnerAddress())) {
                transactionLogTrigger.setFromAddress(
                    StringUtil.encode58Check(triggerSmartContract.getOwnerAddress().toByteArray()));
              }

              if (Objects.nonNull(triggerSmartContract.getContractAddress())) {
                transactionLogTrigger.setToAddress(StringUtil
                    .encode58Check(triggerSmartContract.getContractAddress().toByteArray()));
              }
              break;
            case CreateSmartContract:
              CreateSmartContract createSmartContract = contractParameter
                  .unpack(CreateSmartContract.class);

              if (Objects.nonNull(createSmartContract.getOwnerAddress())) {
                transactionLogTrigger.setFromAddress(
                    StringUtil.encode58Check(createSmartContract.getOwnerAddress().toByteArray()));
              }
              break;
            default:
              break;
          }
        } catch (Exception e) {
          logger.error("failed to load transferAssetContract, error '{}'", e.getMessage());
        }
      }
    }

    long energyUsageTotal = 0;
    // receipt
    if (Objects.nonNull(cypTrace) && Objects.nonNull(cypTrace.getReceipt())) {
      energyUsageTotal = cypTrace.getReceipt().getEnergyUsageTotal();

      transactionLogTrigger.setEnergyFee(cypTrace.getReceipt().getEnergyFee());
      transactionLogTrigger.setOriginEnergyUsage(cypTrace.getReceipt().getOriginEnergyUsage());
      transactionLogTrigger.setEnergyUsageTotal(energyUsageTotal);
      transactionLogTrigger.setNetUsage(cypTrace.getReceipt().getNetUsage());
      transactionLogTrigger.setNetFee(cypTrace.getReceipt().getNetFee());
      transactionLogTrigger.setEnergyUsage(cypTrace.getReceipt().getEnergyUsage());
    }

    // program result
    if (Objects.nonNull(cypTrace) && Objects.nonNull(cypTrace.getRuntime()) && Objects
        .nonNull(cypTrace.getRuntime().getResult())) {
      ProgramResult programResult = cypTrace.getRuntime().getResult();
      ByteString contractResult = ByteString.copyFrom(programResult.getHReturn());
      ByteString contractAddress = ByteString.copyFrom(programResult.getContractAddress());

      if (Objects.nonNull(contractResult) && contractResult.size() > 0) {
        transactionLogTrigger.setContractResult(Hex.toHexString(contractResult.toByteArray()));
      }

      if (Objects.nonNull(contractAddress) && contractAddress.size() > 0) {
        if (Objects.nonNull(transactionInfo)
            && contractType != null && contractType != CreateSmartContract) {
          transactionLogTrigger.setContractAddress(null);
        } else {
          transactionLogTrigger
              .setContractAddress(StringUtil.encode58Check((contractAddress.toByteArray())));
        }
      }

      // internal transaction
      transactionLogTrigger.setInternalTransactionList(
          getInternalTransactionList(programResult.getInternalTransactions()));
    }

    // process transactionInfo list, only enabled when ethCompatible is true
    if (Objects.nonNull(transactionInfo)) {
      transactionLogTrigger.setTransactionIndex(txIndex);
      transactionLogTrigger.setCumulativeEnergyUsed(preCumulativeEnergyUsed + energyUsageTotal);
      transactionLogTrigger.setPreCumulativeLogCount(preCumulativeLogCount);
      transactionLogTrigger.setEnergyUnitPrice(energyUnitPrice);

      List<LogPojo> logPojoList = new ArrayList<>();
      for (int index = 0; index < transactionInfo.getLogCount(); index++) {
        TransactionInfo.Log log = transactionInfo.getLogList().get(index);
        LogPojo logPojo = new LogPojo();

        logPojo.setAddress((log.getAddress() != null)
            ? Hex.toHexString(log.getAddress().toByteArray()) : "");
        logPojo.setBlockHash(blockHash);
        logPojo.setBlockNumber(cypCapsule.getBlockNum());
        logPojo.setData(Hex.toHexString(log.getData().toByteArray()));
        logPojo.setLogIndex(preCumulativeLogCount + index);

        List<String> topics = new ArrayList<>();
        for (int i = 0; i < log.getTopicsCount(); i++) {
          topics.add(Hex.toHexString(log.getTopics(i).toByteArray()));
        }
        logPojo.setTopicList(topics);

        logPojo.setTransactionHash(transactionHash);
        logPojo.setTransactionIndex(txIndex);

        logPojoList.add(logPojo);
      }
      transactionLogTrigger.setLogList(logPojoList);
    }
  }

  public void setLatestSolidifiedBlockNumber(long latestSolidifiedBlockNumber) {
    transactionLogTrigger.setLatestSolidifiedBlockNumber(latestSolidifiedBlockNumber);
  }

  private List<InternalTransactionPojo> getInternalTransactionList(
      List<InternalTransaction> internalTransactionList) {
    List<InternalTransactionPojo> pojoList = new ArrayList<>();

    internalTransactionList.forEach(internalTransaction -> {
      InternalTransactionPojo item = new InternalTransactionPojo();

      item.setHash(Hex.toHexString(internalTransaction.getHash()));
      item.setCallValue(internalTransaction.getValue());
      item.setTokenInfo(internalTransaction.getTokenInfo());
      item.setCaller_address(Hex.toHexString(internalTransaction.getSender()));
      item.setTransferTo_address(Hex.toHexString(internalTransaction.getTransferToAddress()));
      item.setData(Hex.toHexString(internalTransaction.getData()));
      item.setRejected(internalTransaction.isRejected());
      item.setNote(internalTransaction.getNote());
      item.setExtra(internalTransaction.getExtra());

      pojoList.add(item);
    });

    return pojoList;
  }

  @Override
  public void processTrigger() {
    EventPluginLoader.getInstance().postTransactionTrigger(transactionLogTrigger);
  }
}
