/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C)  2001-2003 ScalAgent Distributed Technologies
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


package a3.perf;

import fr.dyade.aaa.agent.*;
import framework.TestCase;

public class test1 extends TestCase {
  short ServerPing;
  short ServerPong;

  public test1() {
    super();
  }

  protected void setUp() throws Exception {
    ServerPing = Integer.getInteger("Ping", 0).shortValue();
    ServerPong = Integer.getInteger("Pong", 0).shortValue();
    
    int bounce = Integer.getInteger("bounce", 499).intValue();

    timeout = (long) (50000 * bounce);
    timeout = Long.getLong("timeout", timeout).longValue();

    Ping ping = new Ping(ServerPing);
    Pong pong = new Pong(ServerPong);

    if (ServerPing != 0)
      startAgentServer(ServerPing);
    if ((ServerPong != 0) && (ServerPong != ServerPing))
      startAgentServer(ServerPong);

    ping.pong = pong.getId();
    ping.bounce = bounce;

    ping.deploy();
    pong.deploy();
    
    Channel.sendTo(ping.getId(), new Start());
  }

  protected void tearDown() {
    if (ServerPing != 0)
      crashAgentServer(ServerPing);
    if ((ServerPong != 0) && (ServerPong != ServerPing))
      crashAgentServer(ServerPong);
  }

  public static void main(String args[]) {
    new test1().runTest(args);
  }
}

class Start extends Notification {
}

class Ball extends Notification {
  public int bounce;
  public int[] ballast;

  public Ball(int bounce, int size) {
    this.bounce = bounce;
    this.ballast = new int[size];
  }
}

class Ping extends Agent {
  int bounce;
  int test;
  long start;
  long total;

  final static int X = 10000;

  public AgentId pong;

  public Ping(short to) {
    super(to);
    test = 5;
  }

  public void react(AgentId from, Notification not) throws Exception {
    if (not instanceof Start) {
      start = System.currentTimeMillis();
      sendTo(pong, new Ball(bounce, test * X));
      test -= 1;
    } else if (not instanceof Ball) {
      if (((Ball) not).bounce > 0) {
        ((Ball) not).bounce -= 1;
	sendTo(from, not);
      } else {
	long stop = System.currentTimeMillis();
	System.out.println("dT[" + (((test+1)*X*4)/1000) + "Kb] = " +
                           ((1000*(stop - start))/(2*(bounce+1))) + "us");
	total += (stop - start);
	if (test >= 0) {
	  sendTo(getId(), new Start());
	} else {
	  System.out.println("total = " + total + "s");
          TestCase.endTest();
        }
      }
    }
  }
}

class Pong extends Agent {
  public Pong(short to) {
    super(to);
  }

  public void react(AgentId from, Notification not) throws Exception {
    sendTo(from, not);
  }
}
