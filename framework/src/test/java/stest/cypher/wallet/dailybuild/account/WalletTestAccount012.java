package stest.cypher.wallet.dailybuild.account;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.cypher.api.GrpcAPI.AccountResourceMessage;
import org.cypher.api.WalletGrpc;
import org.cypher.common.crypto.ECKey;
import org.cypher.common.utils.ByteArray;
import org.cypher.common.utils.Utils;
import org.cypher.core.Wallet;
import org.cypher.protos.Protocol.Account;
import stest.cypher.wallet.common.client.Configuration;
import stest.cypher.wallet.common.client.Parameter.CommonConstant;
import stest.cypher.wallet.common.client.utils.PublicMethed;

@Slf4j
public class WalletTestAccount012 {
  private static final long sendAmount = 10000000000L;
  private static final long frozenAmountForCypherPower = 3456789L;
  private static final long frozenAmountForNet = 7000000L;
  private final String foundationKey = Configuration.getByPath("testng.conf")
      .getString("foundationAccount.key1");
  private final byte[] foundationAddress = PublicMethed.getFinalAddress(foundationKey);

  private final String witnessKey = Configuration.getByPath("testng.conf")
      .getString("witness.key1");
  private final byte[] witnessAddress = PublicMethed.getFinalAddress(witnessKey);

  ECKey ecKey1 = new ECKey(Utils.getRandom());
  byte[] frozenAddress = ecKey1.getAddress();
  String frozenKey = ByteArray.toHexString(ecKey1.getPrivKeyBytes());

  private ManagedChannel channelFull = null;
  private WalletGrpc.WalletBlockingStub blockingStubFull = null;
  private String fullnode = Configuration.getByPath("testng.conf").getStringList("fullnode.ip.list")
      .get(0);

  /**
   * constructor.
   */
  @BeforeClass(enabled = true)
  public void beforeClass() {
    PublicMethed.printAddress(frozenKey);
    channelFull = ManagedChannelBuilder.forTarget(fullnode)
        .usePlaintext(true)
        .build();
    blockingStubFull = WalletGrpc.newBlockingStub(channelFull);

  }

  @Test(enabled = true, description = "Freeze balance to get cypher power")
  public void test01FreezeBalanceGetCypherPower() {


    final Long beforeFrozenTime = System.currentTimeMillis();
    Assert.assertTrue(PublicMethed.sendcoin(frozenAddress, sendAmount,
        foundationAddress, foundationKey, blockingStubFull));
    PublicMethed.waitProduceNextBlock(blockingStubFull);


    AccountResourceMessage accountResource = PublicMethed
        .getAccountResource(frozenAddress, blockingStubFull);
    final Long beforeTotalCypherPowerWeight = accountResource.getTotalCypherPowerWeight();
    final Long beforeCypherPowerLimit = accountResource.getCypherPowerLimit();


    Assert.assertTrue(PublicMethed.freezeBalanceGetCypherPower(frozenAddress,frozenAmountForCypherPower,
        0,2,null,frozenKey,blockingStubFull));
    Assert.assertTrue(PublicMethed.freezeBalanceGetCypherPower(frozenAddress,frozenAmountForNet,
        0,0,null,frozenKey,blockingStubFull));
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    Long afterFrozenTime = System.currentTimeMillis();
    Account account = PublicMethed.queryAccount(frozenAddress,blockingStubFull);
    Assert.assertEquals(account.getCypherPower().getFrozenBalance(),frozenAmountForCypherPower);
    Assert.assertTrue(account.getCypherPower().getExpireTime() > beforeFrozenTime
        && account.getCypherPower().getExpireTime() < afterFrozenTime);

    accountResource = PublicMethed
        .getAccountResource(frozenAddress, blockingStubFull);
    Long afterTotalCypherPowerWeight = accountResource.getTotalCypherPowerWeight();
    Long afterCypherPowerLimit = accountResource.getCypherPowerLimit();
    Long afterCypherPowerUsed = accountResource.getCypherPowerUsed();
    Assert.assertEquals(afterTotalCypherPowerWeight - beforeTotalCypherPowerWeight,
        frozenAmountForCypherPower / 1000000L);

    Assert.assertEquals(afterCypherPowerLimit - beforeCypherPowerLimit,
        frozenAmountForCypherPower / 1000000L);



    Assert.assertTrue(PublicMethed.freezeBalanceGetCypherPower(frozenAddress,
        6000000 - frozenAmountForCypherPower,
        0,2,null,frozenKey,blockingStubFull));
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    accountResource = PublicMethed
        .getAccountResource(frozenAddress, blockingStubFull);
    afterCypherPowerLimit = accountResource.getCypherPowerLimit();

    Assert.assertEquals(afterCypherPowerLimit - beforeCypherPowerLimit,
        6);


  }


  @Test(enabled = true,description = "Vote witness by cypher power")
  public void test02VotePowerOnlyComeFromCypherPower() {
    AccountResourceMessage accountResource = PublicMethed
        .getAccountResource(frozenAddress, blockingStubFull);
    final Long beforeCypherPowerUsed = accountResource.getCypherPowerUsed();


    HashMap<byte[],Long> witnessMap = new HashMap<>();
    witnessMap.put(witnessAddress,frozenAmountForNet / 1000000L);
    Assert.assertFalse(PublicMethed.voteWitness(frozenAddress,frozenKey,witnessMap,
        blockingStubFull));
    witnessMap.put(witnessAddress,frozenAmountForCypherPower / 1000000L);
    Assert.assertTrue(PublicMethed.voteWitness(frozenAddress,frozenKey,witnessMap,
        blockingStubFull));
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    accountResource = PublicMethed
        .getAccountResource(frozenAddress, blockingStubFull);
    Long afterCypherPowerUsed = accountResource.getCypherPowerUsed();
    Assert.assertEquals(afterCypherPowerUsed - beforeCypherPowerUsed,
        frozenAmountForCypherPower / 1000000L);

    final Long secondBeforeCypherPowerUsed = afterCypherPowerUsed;
    witnessMap.put(witnessAddress,(frozenAmountForCypherPower / 1000000L) - 1);
    Assert.assertTrue(PublicMethed.voteWitness(frozenAddress,frozenKey,witnessMap,
        blockingStubFull));
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    accountResource = PublicMethed
        .getAccountResource(frozenAddress, blockingStubFull);
    afterCypherPowerUsed = accountResource.getCypherPowerUsed();
    Assert.assertEquals(secondBeforeCypherPowerUsed - afterCypherPowerUsed,
        1);


  }

  @Test(enabled = true,description = "Cypher power is not allow to others")
  public void test03CypherPowerIsNotAllowToOthers() {
    Assert.assertFalse(PublicMethed.freezeBalanceGetCypherPower(frozenAddress,
        frozenAmountForCypherPower, 0,2,
        ByteString.copyFrom(foundationAddress),frozenKey,blockingStubFull));
  }


  @Test(enabled = true,description = "Unfreeze balance for cypher power")
  public void test04UnfreezeBalanceForCypherPower() {
    AccountResourceMessage accountResource = PublicMethed
        .getAccountResource(foundationAddress, blockingStubFull);
    final Long beforeTotalCypherPowerWeight = accountResource.getTotalCypherPowerWeight();


    Assert.assertTrue(PublicMethed.unFreezeBalance(frozenAddress,frozenKey,2,
        null,blockingStubFull));
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    accountResource = PublicMethed
        .getAccountResource(frozenAddress, blockingStubFull);
    Long afterTotalCypherPowerWeight = accountResource.getTotalCypherPowerWeight();
    Assert.assertEquals(beforeTotalCypherPowerWeight - afterTotalCypherPowerWeight,
        6);

    Assert.assertEquals(accountResource.getCypherPowerLimit(),0L);
    Assert.assertEquals(accountResource.getCypherPowerUsed(),0L);

    Account account = PublicMethed.queryAccount(frozenAddress,blockingStubFull);
    Assert.assertEquals(account.getCypherPower().getFrozenBalance(),0);


  }
  

  /**
   * constructor.
   */

  @AfterClass(enabled = true)
  public void shutdown() throws InterruptedException {
    PublicMethed.unFreezeBalance(frozenAddress, frozenKey, 2, null,
        blockingStubFull);
    PublicMethed.unFreezeBalance(frozenAddress, frozenKey, 0, null,
        blockingStubFull);
    PublicMethed.freedResource(frozenAddress, frozenKey, foundationAddress, blockingStubFull);
    if (channelFull != null) {
      channelFull.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
  }
}


