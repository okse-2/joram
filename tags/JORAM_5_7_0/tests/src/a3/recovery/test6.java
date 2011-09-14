/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C)  2001 - 2009 ScalAgent Distributed Technologies
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
package a3.recovery;

import java.util.Random;

import fr.dyade.aaa.agent.Agent;
import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.Notification;
import framework.TestCase;

public class test6 extends TestCase {
  public test6() {
    super();
  }

  static short ServerPing = (short) 1;
  static short ServerPong = (short) 1;
  static int  nbStopTask =0;

  protected void setUp() throws Exception {
    ServerPing = Integer.getInteger("Ping", ServerPing).shortValue();
    ServerPong = Integer.getInteger("Pong", ServerPong).shortValue();

    startAgentServer(ServerPing, new String[] { "-DNTNoLockFile=true" });
    if (ServerPong != ServerPing)
      startAgentServer(ServerPong, new String[] { "-DNTNoLockFile=true" });

    // int bounce = Integer.getInteger("bounce", 325).intValue();
    int bounce = 5000;
    timeout = 1200000;
    // timeout = Long.getLong("timeout", timeout).longValue();
    
    Test6Agent test = new Test6Agent((short) 0);

    Ping ping = new Ping(ServerPing);
    Pong pong = new Pong(ServerPong);

    ping.pong = pong.getId();
    ping.test = test.getId();
    ping.bounce = bounce;

    test.ping = ping.getId();

    test.deploy();

    pong.deploy();
    ping.deploy();
  }

  protected void tearDown() {
    crashAgentServer(ServerPing);
    if (ServerPong != ServerPing) crashAgentServer(ServerPong);
  }


  public static void main(String args[]) {
    new test6().runTest(args);
  }

  static class Test6Agent extends Agent {
    AgentId ping;
    Random rand = null;

    public Test6Agent(short to) {
      super(to);
      rand = new Random(0x1234L);
    }

    public void react(AgentId from, Notification not) {
      try {
        assertTrue(from.equals(ping));
        assertEquals(not.getClass().getName(), "a3.recovery.test6$Ball");

        Ball ball = (Ball) not;
        System.out.println("bounce: " + ball.bounce);
        assertTrue(ball.bounce >= 0);

        if (ball.bounce > 0) {
          if (rand.nextBoolean()) {
            System.out.println("stop");
            TestCase.stopAgentServer(test6.ServerPing);
          } else {
            System.out.println("crash");
            TestCase.crashAgentServer(test6.ServerPing);
          }
          // Start server#1
          TestCase.startAgentServer(test6.ServerPing, new String[] { "-DNTNoLockFile=true" });
          Thread.sleep(2000L);
          nbStopTask++;
          if (nbStopTask > 20)
            endTest();
        } else {
          endTest();
          // never reached
        }
      } catch (Exception exc) {
        error(exc);
        endTest();
      }
    }
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
    AgentId test, pong;
    int bounce;

    public Ping(short to) {
      super(to);
    }

    protected void agentInitialize(boolean firstime) throws Exception {
      if (firstime) {
        sendTo(pong, new Ball(bounce));
      }
    }

    public void react(AgentId from, Notification not) {
      if (from.equals(pong) && (not instanceof Ball)) {
        if ((bounce %50) == 0)
          sendTo(test, new Ball(bounce));
          
        Ball ball = (Ball) not;
        if (ball.bounce != bounce)
          sendTo(test, new Ball(-1));
          
        if (ball.bounce != 0) {
          bounce -= 1;
          sendTo(pong, new Ball(bounce));
        }
      } else {
        sendTo(test, new Ball(-2));
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
