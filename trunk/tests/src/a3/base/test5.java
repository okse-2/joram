/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C)  2001 ScalAgent Distributed Technologies
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

package a3.base;

import fr.dyade.aaa.agent.*;
import framework.TestCase;

public class test5 extends TestCase {
  public test5() {
    super();
  }

  short ServerPing = (short) 0;
  short ServerPong = (short) 1;
  boolean router = false;

  protected void setUp() throws Exception {
    // Ping should always be deployed on server#0 !!
    ServerPong = Integer.getInteger("Pong", ServerPong).shortValue();
    router = new Boolean(System.getProperty("Router",
                                            "false")).booleanValue();

    if (router) startAgentServer((short) 2);
    if (ServerPong != ServerPing) startAgentServer(ServerPong);

    int bounce = Integer.getInteger("bounce", 100).intValue();
    timeout = 1200000;
    

    Ping ping = new Ping(ServerPing);
    Pong pong = new Pong(ServerPong);

    ping.pong = pong.getId();
    ping.bounce = bounce;

    ping.deploy();
    pong.deploy();
  }

  protected void tearDown() {
    if (ServerPong != ServerPing) crashAgentServer(ServerPong);
    if (router) crashAgentServer((short) 2);
  }


  public static void main(String args[]) {
    new test5().runTest(args);
  }

  static class Ball extends Notification {
    public int bounce;

    public Ball(int bounce) {
      this.bounce = bounce;
    }

    public StringBuffer toString(StringBuffer output) {
      output.append('(');
      super.toString(output);
      output.append(",bounce=").append(bounce);
      output.append(')');

      return output;
    }
  }

  static class Ping extends Agent {
    AgentId pong;
    int bounce;

    public Ping(short to) {
      super(to);
    }

    protected void agentInitialize(boolean firstime) throws Exception {
      if (firstime)
        sendTo(pong, new Ball(bounce));
    }

    public void react(AgentId from, Notification not) {
      try {
        assertTrue(from.equals(pong));
        assertEquals(not.getClass().getName(),
                     "a3.base.test5$Ball");
        if (not instanceof Ball) {
          Ball ball = (Ball) not;
          assertEquals(ball.bounce, bounce);
          if (ball.bounce == 0) {
            endTest();
            // never reached
          } else {
            bounce -= 1;
            sendTo(pong, new Ball(bounce));
          }
        }
      } catch (Exception exc) {
        error(exc);
        endTest();
      }
    }
  }

  static class Pong extends Agent {
    public Pong(short to) {
      super(to);
    }

    public void react(AgentId from, Notification not) throws Exception {
      if (not instanceof Ball) {
        sendTo(from, new Ball(((Ball) not).bounce));
      }
    }
  }
}
