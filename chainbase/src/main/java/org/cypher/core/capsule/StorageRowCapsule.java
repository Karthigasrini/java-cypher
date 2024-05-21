
package org.cypher.core.capsule;

import java.util.Arrays;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.cypher.common.parameter.CommonParameter;
import org.cypher.common.utils.Sha256Hash;


@Slf4j(topic = "capsule")
public class StorageRowCapsule implements ProtoCapsule<byte[]> {

  @Getter
  private byte[] rowValue;
  @Setter
  @Getter
  private byte[] rowKey;

  @Getter
  private boolean dirty = false;

  public StorageRowCapsule(StorageRowCapsule rowCapsule) {
    this.rowKey = rowCapsule.getRowKey().clone();
    this.rowValue = rowCapsule.getRowValue().clone();
    this.dirty = rowCapsule.isDirty();
  }

  public StorageRowCapsule(byte[] rowKey, byte[] rowValue) {
    this.rowKey = rowKey;
    this.rowValue = rowValue;
    markDirty();
  }

  public StorageRowCapsule(byte[] rowValue) {
    this.rowValue = rowValue;
  }

  private void markDirty() {
    dirty = true;
  }

  public Sha256Hash getHash() {
    return Sha256Hash.of(CommonParameter.getInstance().isECKeyCryptoEngine(),
        this.rowValue);
  }

  public byte[] getValue() {
    return this.rowValue;
  }

  public void setValue(byte[] value) {
    this.rowValue = value;
    markDirty();
  }

  @Override
  public byte[] getData() {
    return this.rowValue;
  }

  @Override
  public byte[] getInstance() {
    return this.rowValue;
  }

  @Override
  public String toString() {
    return Arrays.toString(rowValue);
  }
}
