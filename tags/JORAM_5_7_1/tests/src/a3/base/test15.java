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

import fr.dyade.aaa.agent.Agent;
import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.Channel;
import fr.dyade.aaa.agent.Notification;
import framework.TestCase;

public class test15 extends TestCase {
  public test15() {
    super();
  }

  short ServerEcho = (short) 0;

  protected void setUp() throws Exception {
    timeout = 60000L;

    ServerEcho = Integer.getInteger("Echo", ServerEcho).shortValue();
    if (ServerEcho != 0)
      startAgentServer((short) ServerEcho);

    Echo echo = new Echo((short) ServerEcho);
    echo.deploy();

    Test agent = new Test(echo.getId());
    agent.deploy();

    // this notification should be destroyed at post (Engine or Network).
    Ball not = new Ball(0);
    not.setExpiration(System.currentTimeMillis()-1000L);
    Channel.sendTo(agent.getId(), not);
    // this notification should be handled.
    not = new Ball(1);
    not.setExpiration(System.currentTimeMillis()+1000L);
    Channel.sendTo(agent.getId(), not);
    // this notification should be destroyed by Engine.
    not = new Ball(2);
    not.setExpiration(System.currentTimeMillis()+1000L);
    Channel.sendTo(agent.getId(), not);
    // this notification should be handled.
    not = new Ball(3);
    not.setExpiration(System.currentTimeMillis()+2000L);
    Channel.sendTo(agent.getId(), not);
  }

  protected void tearDown() {
    if (ServerEcho != 0)
      crashAgentServer((short) ServerEcho);
  }

  public static void main(String args[]) {
    new test15().runTest(args);
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

//   static class Echo extends Agent {
//     public Echo(short to) {
//       super(to, "Echo#" + to);
//     }

//     public void react(AgentId from, Notification not) {
//       not.setExpiration(-1L);
//       sendTo(from, not);
//       try {
//         Thread.sleep(1000L);
//       } catch (InterruptedException exc) {}
//     }
//   }

  static class Test extends Agent {
    int state = 0;
    AgentId echo;

    public Test(AgentId echo) {
      super();
      this.echo = echo;
    }

    public void react(AgentId from, Notification not) {
      try {
        System.out.println("" + state + " -> " + not);
        System.out.println(not.getExpiration());

        switch (state) {
        case 0:
          // Should receive the Ball #1
          assertTrue(from.isNullId());
          assertEquals("a3.base.test15$Ball", not.getClass().getName());
          assertEquals(((Ball) not).bounce, 1);
          try {
            Thread.sleep(1200L);
          } catch (InterruptedException exc) {}
          break;
        case 1:
          // Should receive the Ball #3
          assertTrue(from.isNullId());
          assertEquals("a3.base.test15$Ball", not.getClass().getName());
          assertEquals(((Ball) not).bounce, 3);

          // this notification should be destroyed.
          Ball ball = new Ball(4);
          ball.setExpiration(System.currentTimeMillis()-10L);
          sendTo(echo, ball);
          // this notification should be handled.
          ball = new Ball(5);
          ball.setExpiration(System.currentTimeMillis()+1000L);
          sendTo(echo, ball);
          break;
        case 2:
          // Should receive the Ball #5
          assertTrue(echo.equals(from));
          assertEquals("a3.base.test15$Ball", not.getClass().getName());
          assertEquals(((Ball) not).bounce, 5);

          // this notification should be handled.
          ball = new Ball(6);
          ball.setExpiration(System.currentTimeMillis()+1000L);
          sendTo(getId(), ball);
          // this notification should be destroyed.
          ball = new Ball(7);
          ball.setExpiration(System.currentTimeMillis()+1000L);
          sendTo(echo, ball);
          break;
        case 3:
          // Should receive the Ball #6
          assertTrue(getId().equals(from));
          assertEquals("a3.base.test15$Ball", not.getClass().getName());
          assertEquals(((Ball) not).bounce, 6);
          try {
            Thread.sleep(2000L);
          } catch (InterruptedException exc) {}
          // this notification should be handled.
          ball = new Ball(8);
          ball.setExpiration(System.currentTimeMillis()+1000L);
          sendTo(echo, ball);
          break;
        case 4:
          // Should receive the Ball #8
          assertTrue(echo.equals(from));
          assertEquals("a3.base.test15$Ball", not.getClass().getName());
          assertEquals(((Ball) not).bounce, 8);

          endTest();
          // never reached
        }
        state += 1;
      } catch (Exception exc) {
        error(exc);
        endTest();
      }
    }
  }
}
