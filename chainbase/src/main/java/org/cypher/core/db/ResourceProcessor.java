package org.cypher.core.db;

import static org.cypher.core.config.Parameter.ChainConstant.BLOCK_PRODUCED_INTERVAL;

import org.cypher.common.utils.Commons;
import org.cypher.core.capsule.AccountCapsule;
import org.cypher.core.capsule.TransactionCapsule;
import org.cypher.core.config.Parameter.AdaptiveResourceLimitConstants;
import org.cypher.core.config.Parameter.ChainConstant;
import org.cypher.core.exception.AccountResourceInsufficientException;
import org.cypher.core.exception.BalanceInsufficientException;
import org.cypher.core.exception.ContractValidateException;
import org.cypher.core.exception.TooBigTransactionResultException;
import org.cypher.core.store.AccountStore;
import org.cypher.core.store.DynamicPropertiesStore;

abstract class ResourceProcessor {

  protected DynamicPropertiesStore dynamicPropertiesStore;
  protected AccountStore accountStore;
  protected long precision;
  protected long windowSize;
  protected long averageWindowSize;

  public ResourceProcessor(DynamicPropertiesStore dynamicPropertiesStore,
      AccountStore accountStore) {
    this.dynamicPropertiesStore = dynamicPropertiesStore;
    this.accountStore = accountStore;
    this.precision = ChainConstant.PRECISION;
    this.windowSize = ChainConstant.WINDOW_SIZE_MS / BLOCK_PRODUCED_INTERVAL;
    this.averageWindowSize =
        AdaptiveResourceLimitConstants.PERIODS_MS / BLOCK_PRODUCED_INTERVAL;
  }

  abstract void updateUsage(AccountCapsule accountCapsule);

  abstract void consume(TransactionCapsule cyp, TransactionTrace trace)
      throws ContractValidateException, AccountResourceInsufficientException, TooBigTransactionResultException;

  protected long increase(long lastUsage, long usage, long lastTime, long now) {
    return increase(lastUsage, usage, lastTime, now, windowSize);
  }

  protected long increase(long lastUsage, long usage, long lastTime, long now, long windowSize) {
    long averageLastUsage = divideCeil(lastUsage * precision, windowSize);
    long averageUsage = divideCeil(usage * precision, windowSize);

    if (lastTime != now) {
      assert now > lastTime;
      if (lastTime + windowSize > now) {
        long delta = now - lastTime;
        double decay = (windowSize - delta) / (double) windowSize;
        averageLastUsage = Math.round(averageLastUsage * decay);
      } else {
        averageLastUsage = 0;
      }
    }
    averageLastUsage += averageUsage;
    return getUsage(averageLastUsage, windowSize);
  }

  private long divideCeil(long numerator, long denominator) {
    return (numerator / denominator) + ((numerator % denominator) > 0 ? 1 : 0);
  }

  private long getUsage(long usage, long windowSize) {
    return usage * windowSize / precision;
  }

  protected boolean consumeFeeForBandwidth(AccountCapsule accountCapsule, long fee) {
    try {
      long latestOperationTime = dynamicPropertiesStore.getLatestBlockHeaderTimestamp();
      accountCapsule.setLatestOperationTime(latestOperationTime);
      Commons.adjustBalance(accountStore, accountCapsule, -fee);
      if (dynamicPropertiesStore.supportTransactionFeePool()) {
        dynamicPropertiesStore.addTransactionFeePool(fee);
      } else if (dynamicPropertiesStore.supportBlackHoleOptimization()) {
        dynamicPropertiesStore.burnCyp(fee);
      } else {
        Commons.adjustBalance(accountStore, accountStore.getBlackhole().createDbKey(), +fee);
      }

      return true;
    } catch (BalanceInsufficientException e) {
      return false;
    }
  }


  protected boolean consumeFeeForNewAccount(AccountCapsule accountCapsule, long fee) {
    try {
      long latestOperationTime = dynamicPropertiesStore.getLatestBlockHeaderTimestamp();
      accountCapsule.setLatestOperationTime(latestOperationTime);
      Commons.adjustBalance(accountStore, accountCapsule, -fee);
      if (dynamicPropertiesStore.supportBlackHoleOptimization()) {
        dynamicPropertiesStore.burnCyp(fee);
      } else {
        Commons.adjustBalance(accountStore, accountStore.getBlackhole().createDbKey(), +fee);
      }

      return true;
    } catch (BalanceInsufficientException e) {
      return false;
    }
  }
}
