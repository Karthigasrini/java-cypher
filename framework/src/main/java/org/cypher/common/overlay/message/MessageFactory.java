

package org.cypher.common.overlay.message;

public abstract class MessageFactory {

  protected abstract Message create(byte[] data) throws Exception;

}
