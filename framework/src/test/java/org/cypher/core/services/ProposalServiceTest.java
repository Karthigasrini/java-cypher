package org.cypher.core.services;

import static org.cypher.core.utils.ProposalUtil.ProposalType.ENERGY_FEE;
import static org.cypher.core.utils.ProposalUtil.ProposalType.WITNESS_127_PAY_PER_BLOCK;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.cypher.common.application.CypherApplicationContext;
import org.cypher.common.utils.FileUtil;
import org.cypher.core.Constant;
import org.cypher.core.capsule.ProposalCapsule;
import org.cypher.core.config.DefaultConfig;
import org.cypher.core.config.args.Args;
import org.cypher.core.consensus.ProposalService;
import org.cypher.core.db.Manager;
import org.cypher.core.utils.ProposalUtil.ProposalType;
import org.cypher.protos.Protocol.Proposal;

@Slf4j
public class ProposalServiceTest {

  private static CypherApplicationContext context;
  private static Manager manager;
  private static String dbPath = "output_proposal_test";

  @BeforeClass
  public static void init() {
    Args.setParam(new String[]{"-d", dbPath}, Constant.TEST_CONF);
    context = new CypherApplicationContext(DefaultConfig.class);
    manager = context.getBean(Manager.class);
    manager.getDynamicPropertiesStore().saveLatestBlockHeaderNumber(5);
  }

  @Test
  public void test() {
    Set<Long> set = new HashSet<>();
    for (ProposalType proposalType : ProposalType.values()) {
      Assert.assertTrue(set.add(proposalType.getCode()));
    }

    Proposal proposal = Proposal.newBuilder().putParameters(1, 1).build();
    ProposalCapsule proposalCapsule = new ProposalCapsule(proposal);
    boolean result = ProposalService.process(manager, proposalCapsule);
    Assert.assertTrue(result);
    //
    proposal = Proposal.newBuilder().putParameters(1000, 1).build();
    proposalCapsule = new ProposalCapsule(proposal);
    result = ProposalService.process(manager, proposalCapsule);
    Assert.assertFalse(result);
    //
    for (ProposalType proposalType : ProposalType.values()) {
      if (proposalType == WITNESS_127_PAY_PER_BLOCK) {
        proposal = Proposal.newBuilder().putParameters(proposalType.getCode(), 16160).build();
      } else {
        proposal = Proposal.newBuilder().putParameters(proposalType.getCode(), 1).build();
      }
      proposalCapsule = new ProposalCapsule(proposal);
      result = ProposalService.process(manager, proposalCapsule);
      Assert.assertTrue(result);
    }
  }

  @Test
  public void testUpdateEnergyFee() {
    String preHistory = manager.getDynamicPropertiesStore().getEnergyPriceHistory();

    long newPrice = 500;
    Proposal proposal = Proposal.newBuilder().putParameters(ENERGY_FEE.getCode(), newPrice).build();
    ProposalCapsule proposalCapsule = new ProposalCapsule(proposal);
    boolean result = ProposalService.process(manager, proposalCapsule);
    Assert.assertTrue(result);

    long currentPrice = manager.getDynamicPropertiesStore().getEnergyFee();
    Assert.assertEquals(currentPrice, newPrice);

    String currentHistory = manager.getDynamicPropertiesStore().getEnergyPriceHistory();
    Assert.assertEquals(preHistory + "," + proposalCapsule.getExpirationTime() + ":" + newPrice,
        currentHistory);
  }

  @AfterClass
  public static void removeDb() {
    Args.clearParam();
    context.destroy();
    if (FileUtil.deleteDir(new File(dbPath))) {
      logger.info("Release resources successful.");
    } else {
      logger.info("Release resources failure.");
    }
  }
}