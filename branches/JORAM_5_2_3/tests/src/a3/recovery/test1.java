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

package a3.recovery;

import java.util.*;



import fr.dyade.aaa.agent.*;
import framework.TestCase;

public class test1 extends TestCase {
  public test1() {
    super();
  }

  static short ServerPing = (short) 0;
  static short ServerPong = (short) 1;
  static int  nbStopTask =0;

  protected void setUp() throws Exception {
    startAgentServer(ServerPong);

    int bounce = Integer.getInteger("bounce", 1500).intValue();
    timeout = 250L * bounce;
    timeout = Long.getLong("timeout", timeout).longValue();
    
    Timer timer = new Timer(true);

    Ping ping = new Ping(ServerPing);
    Pong pong = new Pong(ServerPong);

    ping.pong = pong.getId();
    ping.bounce = bounce;

    ping.deploy();
    pong.deploy();

    timer.schedule(new StopTask(ping.getId(), new StopNot()), 5000L, 15000L);
  }

  protected void tearDown() {
    crashAgentServer(ServerPong);
  }


  public static void main(String args[]) {
    new test1().runTest(args);
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

  static class StopNot extends Notification {}

  static class StopTask extends TimerTask {
    AgentId to;
    Notification not;

    StopTask(AgentId to, Notification not) {
      this.to = to;
      this.not = not;
    }

    public void run() {
      Channel.sendTo(to, not);
    }
  }

  static class Ping extends Agent {
    AgentId pong;
    int bounce;
    Random rand = null;

    public Ping(short to) {
      super(to);
      rand = new Random(0x1234L);
    }

    protected void agentInitialize(boolean firstime) throws Exception {
      if (firstime) {
        sendTo(pong, new Ball(bounce));
      }
    }

    public void react(AgentId from, Notification not) {
      try {
        if (not instanceof StopNot) {
          if (rand.nextBoolean()) {
	    System.out.println("stop");
            TestCase.stopAgentServer(test1.ServerPong);
          } else {
	    System.out.println("crash");
            TestCase.crashAgentServer(test1.ServerPong);
	  }
	  // Wait in order to prevent WAIT status on TCP connection
	  Thread.currentThread().sleep(500L);
	  // Start server#1
	  TestCase.startAgentServer(test1.ServerPong);
	  nbStopTask++;
	  if(nbStopTask > 20 ) endTest();
        } else {
	  if ((bounce %50) == 0) System.out.println("bounce: " + bounce);
          assertTrue(from.equals(pong));
          assertEquals(not.getClass().getName(),
                       "a3.recovery.test1$Ball");
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
