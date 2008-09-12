/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C)  2007 - 2008 ScalAgent Distributed Technologies
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
 * Initial developer(s): ScalAgent Distributed Technologies
 * Contributor(s): 
 */
package a3.test;

import fr.dyade.aaa.agent.*;

public class Sender  extends Agent {
  public static void main(String args[]) throws Exception {
    AgentServer.init(args);

    Thread.sleep(100L);

    Sender sender = new Sender(AgentServer.getServerId());
    sender.receiver = AgentId.fromString("#0.0.1025");
    sender.deploy();
    // Start the ping-pong
    Channel.sendTo(sender.getId(), new Token(1, 10, 1000));

    AgentServer.start();
  }
  
  AgentId receiver;

  public Sender(short serverId) {
    super(serverId);
  }

  public void react(AgentId from, Notification not) {
    try {
      if (not instanceof Token) {
        Token token = (Token) not;

        Thread.sleep(token.pause);
      
        token.bounce -= 1;
        sendTo(receiver, token);

        if (token.bounce > 0)
          sendTo(getId(), not);
      } else {
        super.react(from, not);
      }
    }  catch (Throwable exc) {
      exc.printStackTrace();
    }
  }
}
