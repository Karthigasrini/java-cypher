package org.cypher.core.vm;

import java.util.function.Consumer;
import java.util.function.Function;
import org.cypher.core.vm.program.Program;

public class Operation {

  private final int opcode;
  private final int require;
  private final int ret;
  private final Function<Program, Long> cost;
  private final Consumer<Program> action;

  public Operation(int opcode, int require, int ret,
                      Function<Program, Long> cost, Consumer<Program> action) {
    this.opcode = opcode;
    this.require = require;
    this.ret = ret;
    this.cost = cost;
    this.action = action;
  }

  public int getOpcode() {
    return opcode;
  }

  public int getRequire() {
    return require;
  }

  public int getRet() {
    return ret;
  }

  public long getEnergyCost(Program program) {
    return this.cost.apply(program);
  }

  public void execute(Program program) {
    this.action.accept(program);
  }
}
