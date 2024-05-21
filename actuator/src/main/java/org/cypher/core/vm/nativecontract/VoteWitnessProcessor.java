package org.cypher.core.vm.nativecontract;

import static org.cypher.core.actuator.ActuatorConstant.NOT_EXIST_STR;
import static org.cypher.core.actuator.ActuatorConstant.STORE_NOT_EXIST;
import static org.cypher.core.actuator.ActuatorConstant.WITNESS_EXCEPTION_STR;
import static org.cypher.core.config.Parameter.ChainConstant.MAX_VOTE_NUMBER;
import static org.cypher.core.config.Parameter.ChainConstant.CYP_PRECISION;

import com.google.common.math.LongMath;
import com.google.protobuf.ByteString;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.cypher.common.utils.StringUtil;
import org.cypher.core.capsule.AccountCapsule;
import org.cypher.core.capsule.VotesCapsule;
import org.cypher.core.exception.ContractExeException;
import org.cypher.core.exception.ContractValidateException;
import org.cypher.core.vm.nativecontract.param.VoteWitnessParam;
import org.cypher.core.vm.repository.Repository;
import org.cypher.core.vm.utils.VoteRewardUtil;
import org.cypher.protos.Protocol;

@Slf4j(topic = "VMProcessor")
public class VoteWitnessProcessor {

  public void validate(VoteWitnessParam param, Repository repo) throws ContractValidateException {
    if (repo == null) {
      throw new ContractValidateException(STORE_NOT_EXIST);
    }

    if (param.getVotes().size() > MAX_VOTE_NUMBER) {
      throw new ContractValidateException(
          "VoteNumber more than maxVoteNumber " + MAX_VOTE_NUMBER);
    }
  }

  public void execute(VoteWitnessParam param, Repository repo) throws ContractExeException {
    byte[] ownerAddress = param.getVoterAddress();
    VoteRewardUtil.withdrawReward(ownerAddress, repo);

    AccountCapsule accountCapsule = repo.getAccount(ownerAddress);

    VotesCapsule votesCapsule = repo.getVotes(ownerAddress);
    if (votesCapsule == null) {
      votesCapsule = new VotesCapsule(ByteString.copyFrom(ownerAddress),
          accountCapsule.getVotesList());
    }

    accountCapsule.clearVotes();
    votesCapsule.clearNewVotes();

    Map<ByteString, Long> voteMap = new HashMap<>();
    Iterator<Protocol.Vote> iterator = param.getVotes().iterator();
    try {
      long sum = 0;
      while (iterator.hasNext()) {
        Protocol.Vote vote = iterator.next();

        byte[] witnessAddress = vote.getVoteAddress().toByteArray();
        /*
          Already covered while doing maintenance in MaintenanceManager.java, for tvm performance,
          we remove the account check
         */
//        if (repo.getAccount(witnessAddress) == null) {
//          throw new ContractValidateException(
//              ACCOUNT_EXCEPTION_STR + StringUtil.encode58Check(witnessAddress) + NOT_EXIST_STR);
//        }
        if (repo.getWitness(witnessAddress) == null) {
          throw new ContractExeException(
              WITNESS_EXCEPTION_STR + StringUtil.encode58Check(witnessAddress) + NOT_EXIST_STR);
        }

        long voteCount = vote.getVoteCount();
        if (voteCount < 0) {
          throw new ContractExeException("Vote count must not be less than 0");
        } else if (voteCount == 0) {
          iterator.remove();
        } else {
          sum = LongMath.checkedAdd(sum, voteCount);
          // merge vote for same witness
          voteMap.put(vote.getVoteAddress(),
              LongMath.checkedAdd(voteMap.getOrDefault(vote.getVoteAddress(), 0L), voteCount));
        }
      }

      long cypherPower = accountCapsule.getCypherPower();
      sum =  LongMath.checkedMultiply(sum, CYP_PRECISION);
      if (sum > cypherPower) {
        throw new ContractExeException(
            "The total number of votes[" + sum + "] is greater than the cypherPower[" + cypherPower
                + "]");
      }
    } catch (ArithmeticException e) {
      throw new ContractExeException(e.getMessage());
    }

    for (Map.Entry<ByteString, Long> entry : voteMap.entrySet()) {
      accountCapsule.addVotes(entry.getKey(), entry.getValue());
      votesCapsule.addNewVotes(entry.getKey(), entry.getValue());
    }
    repo.updateAccount(accountCapsule.createDbKey(), accountCapsule);
    repo.updateVotes(ownerAddress, votesCapsule);
  }
}
