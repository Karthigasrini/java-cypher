package org.cypher.core.net.message;

import org.cypher.common.overlay.message.Message;
import org.cypher.common.utils.Sha256Hash;
import org.cypher.core.capsule.TransactionCapsule;
import org.cypher.protos.Protocol.Transaction;

public class TransactionMessage extends CypherMessage {

  private TransactionCapsule transactionCapsule;

  public TransactionMessage(byte[] data) throws Exception {
    super(data);
    this.transactionCapsule = new TransactionCapsule(getCodedInputStream(data));
    this.type = MessageTypes.CYP.asByte();
    if (Message.isFilter()) {
      compareBytes(data, transactionCapsule.getInstance().toByteArray());
      transactionCapsule
          .validContractProto(transactionCapsule.getInstance().getRawData().getContract(0));
    }
  }

  public TransactionMessage(Transaction cyp) {
    this.transactionCapsule = new TransactionCapsule(cyp);
    this.type = MessageTypes.CYP.asByte();
    this.data = cyp.toByteArray();
  }

  @Override
  public String toString() {
    return new StringBuilder().append(super.toString())
        .append("messageId: ").append(super.getMessageId()).toString();
  }

  @Override
  public Sha256Hash getMessageId() {
    return this.transactionCapsule.getTransactionId();
  }

  @Override
  public Class<?> getAnswerMessage() {
    return null;
  }

  public TransactionCapsule getTransactionCapsule() {
    return this.transactionCapsule;
  }
}
