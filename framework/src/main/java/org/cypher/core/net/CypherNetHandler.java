package org.cypher.core.net;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.cypher.common.overlay.server.Channel;
import org.cypher.common.overlay.server.MessageQueue;
import org.cypher.core.net.message.CypherMessage;
import org.cypher.core.net.peer.PeerConnection;

@Component
@Scope("prototype")
public class CypherNetHandler extends SimpleChannelInboundHandler<CypherMessage> {

  protected PeerConnection peer;

  private MessageQueue msgQueue;

  @Autowired
  private CypherNetService cypherNetService;

  @Override
  public void channelRead0(final ChannelHandlerContext ctx, CypherMessage msg) throws Exception {
    msgQueue.receivedMessage(msg);
    cypherNetService.onMessage(peer, msg);
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    peer.processException(cause);
  }

  public void setMsgQueue(MessageQueue msgQueue) {
    this.msgQueue = msgQueue;
  }

  public void setChannel(Channel channel) {
    this.peer = (PeerConnection) channel;
  }

}