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

import a3.base.Echo;

public class test10 extends TestCase {
  public test10() {
    super();
  }

  static short ServerEcho = (short) 1;

  protected void setUp() throws Exception {
    timeout = 1200000L;

    Echo echo = new Echo((short) ServerEcho);
    echo.deploy();

    Test agent = new Test(echo.getId());
    agent.deploy();

    // this notification should be handled.
    Notification not = new Ball(0);
    not.setExpiration(System.currentTimeMillis()+1000L);
    Channel.sendTo(agent.getId(), not);
  }

  protected void tearDown() {
    crashAgentServer(ServerEcho);
  }


  public static void main(String args[]) {
    new test10().runTest(args);
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
          // Should receive the Ball #0
          assertTrue(from.isNullId());
          assertEquals("a3.recovery.test10$Ball", not.getClass().getName());
          assertEquals(((Ball) not).bounce, 0);

          // this notification should be handled.
          Ball ball = new Ball(1);
          ball.setExpiration(System.currentTimeMillis()+15000L);
          sendTo(echo, ball);
          // this notification should be destroyed.
          ball = new Ball(2);
          ball.setExpiration(System.currentTimeMillis()+1000L);
          sendTo(echo, ball);
          // this notification should be handled.
          ball = new Ball(3);
          ball.setExpiration(System.currentTimeMillis()+15000L);
          sendTo(echo, ball);
          // this notification should be handled.
          ball = new Ball(4);
          ball.setExpiration(System.currentTimeMillis()+1000L);
          sendTo(getId(), ball);

          break;
        case 1:
          // Should receive the Ball #4
          assertTrue(getId().equals(from));
          assertEquals("a3.recovery.test10$Ball", not.getClass().getName());
          assertEquals(((Ball) not).bounce, 4);

          try {
            Thread.sleep(1500L);
          } catch (InterruptedException exc) {}

          // Start the server #1
          startAgentServer((short) test10.ServerEcho);
          break;
        case 2:
          // Should receive the Ball #1
          assertTrue(echo.equals(from));
          assertEquals("a3.recovery.test10$Ball", not.getClass().getName());
          assertEquals(((Ball) not).bounce, 1);
          break;
        case 3:
          // Should receive the Ball #3
          assertTrue(echo.equals(from));
          assertEquals("a3.recovery.test10$Ball", not.getClass().getName());
          assertEquals(((Ball) not).bounce, 3);

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
