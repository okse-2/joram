/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C)  2012 ScalAgent Distributed Technologies
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
 * Initial developer(s):ScalAgent Distributed Technologies
 * Contributor(s): 
 */
package a3.pool;

import fr.dyade.aaa.agent.Agent;
import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.agent.Notification;
import framework.TestCase;

public class Receiver extends TestCase {

  public static void main(String args[]) throws Exception {
    new Receiver().runTest(args);
  }
  
  protected void setUp() throws Exception {
    ReceiverAg receiver = new ReceiverAg((short) 0);
    receiver.bounce = Integer.getInteger("bounce", 5).intValue();
    receiver.payload = Integer.getInteger("payload", 1000000).intValue();
    receiver.pause = Integer.getInteger("pause", 500).intValue();
    receiver.deploy();
  }
  
  public static void stop() {
    AgentServer.stop(false);
  }

  static class ReceiverAg extends Agent {

    short serverId;
    int bounce;
    int payload;
    int pause;

    public ReceiverAg(short serverId) {
      super(serverId);
      this.serverId = serverId;
    }

    public void react(AgentId from, Notification not) {
      try {
        if (not instanceof Token) {
          Token token = (Token) not;

          if (token.bounce != 0)
            System.out.println("Receiver: receive bounce = " + token.bounce + ", from = " + from);
          
          if (token.bounce == 0) {
            //System.out.println("Receiver: send bounce = " + bounce + " payload = " + payload + " pause = " + pause);
            sendTo(from, new Token(bounce, payload, pause));
            System.out.println("Receiver: receive the init bounce from = " + from);
            System.out.println("Receiver: bounce = " + bounce + " sended to " + from);
          } else if (token.bounce == 1) {
            System.out.println("End test: stop server " + serverId);
            stop();
          }
        } else {
          super.react(from, not);
        }
      } catch (Throwable exc) {
        error(exc);
        exc.printStackTrace();
      }
    }
  }
}
