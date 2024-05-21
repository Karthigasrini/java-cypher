

package org.cypher.common.runtime.vm;

import static org.junit.Assert.assertTrue;

import lombok.extern.slf4j.Slf4j;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.cypher.common.parameter.CommonParameter;
import org.cypher.common.runtime.InternalTransaction;
import org.cypher.common.runtime.InternalTransaction.CypType;
import org.cypher.core.config.args.Args;
import org.cypher.core.exception.ContractValidateException;
import org.cypher.core.vm.Op;
import org.cypher.core.vm.Operation;
import org.cypher.core.vm.OperationRegistry;
import org.cypher.core.vm.program.Program;
import org.cypher.core.vm.program.invoke.ProgramInvokeMockImpl;
import org.cypher.protos.Protocol.Transaction;

@Slf4j
public class InterpreterTest {

  private ProgramInvokeMockImpl invoke;
  private Program program;

  @BeforeClass
  public static void init() {
    OperationRegistry.newBaseOperation();
    CommonParameter.getInstance().setDebug(true);
  }

  @AfterClass
  public static void destroy() {
    Args.clearParam();
  }

  @Test
  public void testVMException() throws ContractValidateException {
    invoke = new ProgramInvokeMockImpl();
    byte[] op = {0x5b, 0x60, 0x00, 0x56};
    // 0x5b      - JUMPTEST
    // 0x60 0x00 - PUSH 0x00
    // 0x56      - JUMP to 0
    Transaction cyp = Transaction.getDefaultInstance();
    InternalTransaction interCyp = new InternalTransaction(cyp, CypType.CYP_UNKNOWN_TYPE);
    program = new Program(op, invoke, interCyp);

    boolean result = false;

    try {
      while (!program.isStopped()) {
        Operation operation = OperationRegistry.get(program.getCurrentOpIntValue());
        if (operation == null) {
          throw Program.Exception.invalidOpCode(program.getCurrentOp());
        }
        program.setLastOp((byte) operation.getOpcode());
        program.verifyStackSize(operation.getRequire());
        //Check not exceeding stack limits
        program.verifyStackOverflow(operation.getRequire(), operation.getRet());

        program.spendEnergy(operation.getEnergyCost(program),
            Op.getNameOf(operation.getOpcode()));
        program.checkCPUTimeLimit(Op.getNameOf(operation.getOpcode()));
        operation.execute(program);
        program.setPreviouslyExecutedOp((byte) operation.getOpcode());
      }
    } catch (Program.OutOfEnergyException e) {
      result = true;
    }

    assertTrue(result);
  }

  @Test
  public void JumpSingleOperation() throws ContractValidateException {
    invoke = new ProgramInvokeMockImpl();
    byte[] op = {0x56};
    // 0x56      - JUMP
    Transaction cyp = Transaction.getDefaultInstance();
    InternalTransaction interCyp = new InternalTransaction(cyp, CypType.CYP_UNKNOWN_TYPE);
    program = new Program(op, invoke, interCyp);

    boolean result = false;

    try {
      while (!program.isStopped()) {
        Operation operation = OperationRegistry.get(program.getCurrentOpIntValue());
        if (operation == null) {
          throw Program.Exception.invalidOpCode(program.getCurrentOp());
        }
        program.setLastOp((byte) operation.getOpcode());
        program.verifyStackSize(operation.getRequire());
        //Check not exceeding stack limits
        program.verifyStackOverflow(operation.getRequire(), operation.getRet());

        program.spendEnergy(operation.getEnergyCost(program),
            Op.getNameOf(operation.getOpcode()));
        program.checkCPUTimeLimit(Op.getNameOf(operation.getOpcode()));
        operation.execute(program);
        program.setPreviouslyExecutedOp((byte) operation.getOpcode());
      }
    } catch (Program.StackTooSmallException e) {
      // except to get stack too small exception for Jump
      result = true;
    }

    assertTrue(result);
  }

  @Test
  public void JumpToInvalidDestination() throws ContractValidateException {
    invoke = new ProgramInvokeMockImpl();
    byte[] op = {0x60, 0x20, 0x56};
    // 0x60      - PUSH1
    // 0x20      - 20
    // 0x56      - JUMP
    Transaction cyp = Transaction.getDefaultInstance();
    InternalTransaction interCyp = new InternalTransaction(cyp, CypType.CYP_UNKNOWN_TYPE);
    program = new Program(op, invoke, interCyp);

    boolean result = false;

    try {
      while (!program.isStopped()) {
        Operation operation = OperationRegistry.get(program.getCurrentOpIntValue());
        if (operation == null) {
          throw Program.Exception.invalidOpCode(program.getCurrentOp());
        }
        program.setLastOp((byte) operation.getOpcode());
        program.verifyStackSize(operation.getRequire());
        //Check not exceeding stack limits
        program.verifyStackOverflow(operation.getRequire(), operation.getRet());

        program.spendEnergy(operation.getEnergyCost(program),
            Op.getNameOf(operation.getOpcode()));
        program.checkCPUTimeLimit(Op.getNameOf(operation.getOpcode()));
        operation.execute(program);
        program.setPreviouslyExecutedOp((byte) operation.getOpcode());
      }
    } catch (Program.BadJumpDestinationException e) {
      // except to get BadJumpDestinationException for Jump
      Assert.assertTrue(e.getMessage().contains("Operation with pc isn't 'JUMPDEST': PC[32];"));
      result = true;
    }

    assertTrue(result);
  }

  @Test
  public void JumpToLargeNumberDestination() throws ContractValidateException {
    invoke = new ProgramInvokeMockImpl();
    byte[] op = {0x64, 0x7f, 0x7f, 0x7f, 0x7f, 0x7f, 0x56};
    // 0x60              - PUSH5
    // 0x7F7F7F7F7F      - 547599908735
    // 0x56              - JUMP
    Transaction cyp = Transaction.getDefaultInstance();
    InternalTransaction interCyp = new InternalTransaction(cyp, CypType.CYP_UNKNOWN_TYPE);
    program = new Program(op, invoke, interCyp);

    boolean result = false;

    try {
      while (!program.isStopped()) {
        Operation operation = OperationRegistry.get(program.getCurrentOpIntValue());
        if (operation == null) {
          throw Program.Exception.invalidOpCode(program.getCurrentOp());
        }
        program.setLastOp((byte) operation.getOpcode());
        program.verifyStackSize(operation.getRequire());
        //Check not exceeding stack limits
        program.verifyStackOverflow(operation.getRequire(), operation.getRet());

        program.spendEnergy(operation.getEnergyCost(program),
            Op.getNameOf(operation.getOpcode()));
        program.checkCPUTimeLimit(Op.getNameOf(operation.getOpcode()));
        operation.execute(program);
        program.setPreviouslyExecutedOp((byte) operation.getOpcode());
      }
    } catch (Program.BadJumpDestinationException e) {
      // except to get BadJumpDestinationException for Jump
      Assert.assertTrue(e.getMessage().contains("Operation with pc isn't 'JUMPDEST': PC[-1];"));
      result = true;
    }

    assertTrue(result);
  }
}
