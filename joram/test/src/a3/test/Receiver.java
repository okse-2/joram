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

package a3.test;

import java.util.Vector;
import fr.dyade.aaa.agent.*;

public class Receiver extends Agent implements ReceiverMBean {

  public static AgentId id;

  public static void main(String args[]) throws Exception {
    AgentServer.init(new String[] {"0", "./s0"});

    Receiver receiver = new Receiver((short) 0);
    id = receiver.getId();
    receiver.bounce = Integer.getInteger("bounce", 10).intValue();
    receiver.payload = Integer.getInteger("payload", 10000).intValue();
    receiver.deploy();

    AgentServer.start();
  }

  boolean running;

  public Receiver(short serverId) {
    super(serverId);
    running = true;
    ids = new Vector();
  }

  int bounce;

  public int getBounce() {
    return bounce;
  }

  public void setBounce(int bounce) {
    this.bounce = bounce;
  }

  int payload;

  public int getPayload() {
    return payload;
  }

  public void setPayload(int payload) {
    this .payload = payload;
  }

  int tokens;

  public int getTokens() {
    return tokens;
  }

  public void reset() {
    tokens = 0;
  }

  public void start() {
    running = true;
    while (! ids.isEmpty()) {
      sendTo((AgentId) ids.remove(0), new Token(bounce, payload));
    }
  }

  public void stop() {
    running = false;
  }

  Vector ids;

  public void react(AgentId from, Notification not) {
    try {
      if (not instanceof Token) {
        Token token = (Token) not;

        System.out.println("recv#" + token.bounce + " from " + from);

        if (token.bounce == 0) {
          if (running)
            sendTo(from, new Token(bounce, payload));
          else
            ids.addElement(from);
        }
      } else {
        super.react(from, not);
      }
    }  catch (Throwable exc) {
      exc.printStackTrace();
    }
  }
}
