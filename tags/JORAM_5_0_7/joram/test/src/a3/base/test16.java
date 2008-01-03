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

import framework.TestCase;
import fr.dyade.aaa.agent.*;

public class test16 extends TestCase {
  short ServerReceiver1 = 1;
  short ServerReceiver2 = 2;

  public test16() {
    super();
  }

  protected void setUp() throws Exception {
    int bounce = Integer.getInteger("bounce", 200).intValue();

    timeout = (long) (10000 * bounce);

    Sender sender = new Sender((short) 0);
    Receiver receiver1 = new Receiver(ServerReceiver1);
    Receiver receiver2 = new Receiver(ServerReceiver2);

    startAgentServer(ServerReceiver1);
    startAgentServer(ServerReceiver2);

    sender.receiver1 = receiver1.getId();
    sender.receiver2 = receiver2.getId();
    sender.bounce = bounce;

    sender.deploy();
    receiver1.deploy();
    receiver2.deploy();

    Channel.sendTo(sender.getId(), new Go());
  }

  protected void tearDown() {
    crashAgentServer(ServerReceiver1);
    crashAgentServer(ServerReceiver2);
  }

  public static void main(String args[]) {
    new test16().runTest(args);
  }

  static class Go extends Notification {
  }

  static class Token extends Notification {
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

  static class Stop extends Notification {
  }

  static class Sender extends Agent {
    int bounce;
    int stop;

    AgentId receiver1;
    AgentId receiver2;

    Sender(short serverId) {
      super(serverId);
    }

    public void react(AgentId from, Notification not) throws Exception {
      try {
        if(not instanceof Go) {
          stop = 2;
          Token tok = new Token(bounce, 10);
          if (tok.bounce >= 0) {
            sendTo(receiver1, tok);
            sendTo(receiver2, tok);
            bounce -= 1;
            sendTo(getId(), new Go());

//             if ((tok.bounce %10) == 9)
            Thread.sleep(100L);
          }
        } else if (not instanceof Token) {
          System.out.println("recv#" + ((Token) not).bounce + " from " + from);
        } else if (not instanceof Stop) {
          stop -= 1;
          if (stop == 0) {
            System.out.println("Stop");
            TestCase.endTest();
            // never reached
          }
        } else {
          super.react(from, not);
        }
      }  catch (Throwable exc) {
        TestCase.error(exc);
        TestCase.endTest();
      }
    }
  }

  static class Receiver extends Agent {
    Receiver(short serverId) {
      super(serverId);
    }

    public void react(AgentId from, Notification not) throws Exception {
      if (not instanceof Token) {
        sendTo(from, not);
        if (((Token) not).bounce == 0) {
          sendTo(from, new Stop());
        }
      } else {
        super.react(from, not);
      }
    }
  }
}
