package org.cypher.core.net.messagehandler;

import org.cypher.core.exception.P2pException;
import org.cypher.core.net.message.CypherMessage;
import org.cypher.core.net.peer.PeerConnection;

public interface CypherMsgHandler {

  void processMessage(PeerConnection peer, CypherMessage msg) throws P2pException;

}
