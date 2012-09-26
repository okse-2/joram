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

public class test2 extends TestCase {
  short ServerSender;
  short ServerReceiver;

  public test2() {
    super();
  }

  protected void setUp() throws Exception {
    ServerSender = Integer.getInteger("Send", 0).shortValue();
    ServerReceiver = Integer.getInteger("Recv", 0).shortValue();

    int bounce = Integer.getInteger("bounce", 99).intValue();

    timeout = (long) (50000 * bounce);
    timeout = Long.getLong("timeout", timeout).longValue();

    Sender sender = new Sender(ServerSender);
    Receiver receiver = new Receiver(ServerReceiver);

    if (ServerSender != 0)
      startAgentServer(ServerSender);
    if ((ServerReceiver != 0) && (ServerReceiver != ServerSender))
      startAgentServer(ServerReceiver);

    sender.receiver = receiver.getId();
    sender.bounce = bounce;

    sender.deploy();
    receiver.deploy();

    Channel.sendTo(sender.getId(), new Go(true));
  }

  protected void tearDown() {
    if (ServerSender != 0)
      crashAgentServer(ServerSender);
    if ((ServerReceiver != 0) && (ServerReceiver != ServerSender))
      crashAgentServer(ServerReceiver);
  }

  public static void main(String args[]) {
    new test2().runTest(args);
  }
}

class Go extends Notification {
  boolean firsttime;

  Go(boolean firsttime) {
    this.firsttime = firsttime;
  }
}

class Token extends Notification {
  public int bounce;
  public int[] ballast;

  public Token(int bounce, int size) {
    this.bounce = bounce;
    this.ballast = new int[size];
  }


  public StringBuffer toString(StringBuffer output) {
    output.append('(');
    super.toString(output);
    output.append(",bounce=").append(bounce);
    output.append(')');

    return output;
  }
}

class Stop extends Notification {
}

class Sender extends Agent {
  Token tok;
  int bounce;
  int test;

  final static int X = 10000;

  AgentId receiver;

  long start;
  long total;

  Sender(short serverId) {
    super(serverId);
    test = 6;
  }

  
  public void react(AgentId from, Notification not) throws Exception {
    if(not instanceof Go) {
      if (((Go) not).firsttime) {
        start = System.currentTimeMillis();
        tok = new Token(bounce, (test -1) * X);
        ((Go) not).firsttime = false;
      }
      
      if (tok.bounce >= 0) {
	sendTo(receiver, tok);
        tok.bounce -= 1;
        sendTo(getId(), not);
      }
    } else if (not instanceof Stop) {
      long end = System.currentTimeMillis();
      System.out.println("dT[" + (((test -1)*X*4)/1000) + "Kb] = " +
                         ((1000*(end - start))/(bounce+1)) + "us");
      total += (end - start);
      test = test -1;
      if (test > 0) {
	sendTo(getId(), new Go(true));
      } else {
        System.out.println("total = " + total + "s");
        TestCase.endTest();
      }
    } else {
      super.react(from, not);
    }
  }
}

class Receiver extends Agent {
  Receiver(short serverId) {
    super(serverId);
  }

  public void react(AgentId from, Notification not) throws Exception {
    if (not instanceof Token) {
      if (((Token) not).bounce == 0) {
	sendTo(from, new Stop());
      }
    } else {
      super.react(from, not);
    }
  }
}
