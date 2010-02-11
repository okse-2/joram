/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C)  2007 ScalAgent Distributed Technologies
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA.
 *
 * Initial developer(s):ScalAgent D.T.
 * Contributor(s): 
 */
package a3.cssi;

import fr.dyade.aaa.agent.*;

public class Sender extends Agent implements SenderMBean {

  public static void main(String args[]) throws Exception {
    AgentServer.init(args);

    Thread.sleep(100L);

    Sender sender = new Sender(AgentServer.getServerId());

    sender.payload = Integer.getInteger("payload", 10000).intValue();
    sender.pause = Integer.getInteger("pause", 5000).intValue();

    Receiver receiver1 = new Receiver((short) 1);
    Receiver receiver2 = new Receiver((short) 2);

    sender.receiver1 = receiver1.getId();
    sender.receiver2 = receiver2.getId();

    receiver1.deploy();
    receiver2.deploy();
    sender.deploy();

    // Start the ping-pong
    Channel.sendTo(sender.getId(), new Token(1, sender.payload, sender.pause));

    AgentServer.start();
  }
  
  AgentId receiver1;
  AgentId receiver2;

  public Sender(short serverId) {
    super(serverId);
  }

  int bounce;

  public int getBounce() {
    return bounce;
  }

  int payload;

  public int getPayload() {
    return payload;
  }

  public void setPayload(int payload) {
    this.payload = payload;
  }

  int pause;

  public int getPause() {
    return pause;
  }

  public void setPause(int pause) {
    this.pause = pause;
  }

  public void reset() {
    bounce = 0;
  }

  public void start() {
    running = true;
    sendTo(getId(), new Token(bounce, payload, pause));
  }

  public void stop() {
    running = false;
  }

  boolean running = true;

  public void react(AgentId from, Notification not) {
    try {
      if (not instanceof Token) {
        Token token = (Token) not;

        if (running) {
          Thread.sleep(token.pause);
          
          bounce = token.bounce +1;

          Token token2 = new Token(bounce, payload, pause);

          sendTo(receiver1, token2);
          sendTo(receiver2, token2);
          sendTo(getId(), token2);
        }
      } else {
        super.react(from, not);
      }
    }  catch (Throwable exc) {
      exc.printStackTrace();
    }
  }
}
