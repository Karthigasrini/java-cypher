package org.cypher.core.net.message;

import java.util.List;
import org.cypher.core.capsule.TransactionCapsule;
import org.cypher.protos.Protocol;
import org.cypher.protos.Protocol.Transaction;

public class TransactionsMessage extends CypherMessage {

  private Protocol.Transactions transactions;

  public TransactionsMessage(List<Transaction> cyps) {
    Protocol.Transactions.Builder builder = Protocol.Transactions.newBuilder();
    cyps.forEach(cyp -> builder.addTransactions(cyp));
    this.transactions = builder.build();
    this.type = MessageTypes.CYPS.asByte();
    this.data = this.transactions.toByteArray();
  }

  public TransactionsMessage(byte[] data) throws Exception {
    super(data);
    this.type = MessageTypes.CYPS.asByte();
    this.transactions = Protocol.Transactions.parseFrom(getCodedInputStream(data));
    if (isFilter()) {
      compareBytes(data, transactions.toByteArray());
      TransactionCapsule.validContractProto(transactions.getTransactionsList());
    }
  }

  public Protocol.Transactions getTransactions() {
    return transactions;
  }

  @Override
  public String toString() {
    return new StringBuilder().append(super.toString()).append("cyp size: ")
        .append(this.transactions.getTransactionsList().size()).toString();
  }

  @Override
  public Class<?> getAnswerMessage() {
    return null;
  }

}
