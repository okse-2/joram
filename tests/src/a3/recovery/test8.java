/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C)  2001 - 2011 ScalAgent Distributed Technologies
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
package a3.recovery;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import fr.dyade.aaa.agent.Agent;
import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.Channel;
import fr.dyade.aaa.agent.Notification;
import framework.TestCase;

public class test8 extends TestCase {
  public test8() {
    super();
  }

  static Timer timer = null;

  static short ServerPing = (short) 0;
  static short ServerPong1 = (short) 1;
  static short ServerPong2 = (short) 2;
  static int  nbStopTask0 =0;
  static int  nbStopTask1 =0;
  static int ended = 2;

  protected void setUp() throws Exception {
    int bounce = Integer.getInteger("bounce", 1000).intValue();
    timeout = 100L * bounce;
    timeout = Long.getLong("timeout", timeout).longValue();
    
    timer = new Timer(true);

    Ping ping1 = new Ping(ServerPing);
    Ping ping2 = new Ping(ServerPing);

    ping1.remote = ServerPong1;
    ping1.bounce = bounce;
    ping1.deploy();

    ping2.remote = ServerPong2;
    ping2.bounce = bounce;
    ping2.deploy();

    timer.schedule(new PingTask(ping1.getId(), new StartNot()), 2500L, 10000L);
    timer.schedule(new PingTask(ping1.getId(), new StopNot()), 11000L, 10000L);

    timer.schedule(new PingTask(ping2.getId(), new StartNot()), 500L, 5000L);
    timer.schedule(new PingTask(ping2.getId(), new StopNot()), 4000L, 5000L);
  }

  protected void tearDown() {
    crashAgentServer(ServerPong1);
    crashAgentServer(ServerPong2);
    timer.cancel();
  }


  public static void main(String args[]) throws InterruptedException {
    new test8().runTest(args);
  }

  static class Ball extends Notification {
    public int bounce;

    public Ball(int bounce) {
      this.bounce = bounce;
    }
  }

  static class StartNot extends Notification {}
  static class StopNot extends Notification {}

  static class PingTask extends TimerTask {
    AgentId to;
    Notification not;

    PingTask(AgentId to, Notification not) {
      this.to = to;
      this.not = not;
    }

    public void run() {
      Channel.sendTo(to, not);
    }
  }

  static class Ping extends Agent {
    short remote;
    AgentId pong;
    int bounce;
    Random rand = null;

    public Ping(short to) {
      super(to);
      rand = new Random(0x1234L);
    }

    protected void agentInitialize(boolean firstime) throws Exception {
      if (firstime) {
        Pong agent = new Pong(remote);
        pong = agent.getId();
        agent.deploy();
        sendTo(pong, new Ball(bounce));
      }
    }

    public void react(AgentId from, Notification not) {
      try {
        if (not instanceof StartNot) {
          String[] jvmargs = { "-DTransaction.UseLockFile=false", "-Dcom.sun.management.jmxremote" };
          System.out.println("start " + remote + " - " + bounce);
          startAgentServer(remote, jvmargs);
        } else if (not instanceof StopNot) {
          if (rand.nextBoolean()) {
            System.out.println("stop " + remote + " - " + bounce);
            TestCase.stopAgentServer(remote);
          } else {
            System.out.println("crash " + remote + " - " + bounce);
            TestCase.crashAgentServer(remote);
          }
          if( remote == 1 ){
            nbStopTask0++;
          }else if( remote == 2){
            nbStopTask1++;
          }

          if(nbStopTask0 > 2 && nbStopTask1 >2 ) endTest();
        } else {
          if ((bounce %50) == 0)
            System.out.println("bounce[" + remote + "]: " + bounce);
          assertTrue(from.equals(pong));
          assertEquals(not.getClass().getName(), "a3.recovery.test8$Ball");
          if (not instanceof Ball) {
            Ball ball = (Ball) not;
            assertEquals(ball.bounce, bounce);
            if (ball.bounce == 0) {
              test8.ended -= 1;

              if (test8.ended == 0)  endTest();
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
