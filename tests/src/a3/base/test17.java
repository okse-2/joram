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

public class test17 extends TestCase {
  short ServerSender1 = 1;
  short ServerSender2 = 2;

  public test17() {
    super();
  }

  protected void setUp() throws Exception {
    int bounce = Integer.getInteger("bounce", 200).intValue();

    timeout = (long) (10000 * bounce);

    Receiver receiver = new Receiver((short) 0);
    Sender sender1 = new Sender(ServerSender1);
    Sender sender2 = new Sender(ServerSender2);

    startAgentServer(ServerSender1);
    startAgentServer(ServerSender2);

    receiver.sender1 = sender1.getId();
    receiver.sender2 = sender2.getId();

    sender1.receiver = receiver.getId();
    sender2.receiver = receiver.getId();

    receiver.deploy();
    sender1.deploy();
    sender2.deploy();

    Channel.sendTo(receiver.getId(), new Go(bounce));
  }

  protected void tearDown() {
    crashAgentServer(ServerSender1);
    crashAgentServer(ServerSender2);
  }

  public static void main(String args[]) {
    new test17().runTest(args);
  }

  static class Go extends Notification {
    public int bounce;

    public Go(int bounce) {
      this.bounce = bounce;
    }
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

  static class Receiver extends Agent {
    int stop;

    AgentId sender1;
    int bounce1;
    AgentId sender2;
    int bounce2;

    Receiver(short serverId) {
      super(serverId);
    }

    public void react(AgentId from, Notification not) throws Exception {
      try {
        if(not instanceof Go) {
          stop = 2;

          bounce1 = ((Go) not).bounce;
          bounce2 = bounce1;

          sendTo(sender1, not);
          sendTo(sender2, not);
        } else if (not instanceof Token) {
          int tbounce = ((Token) not).bounce;
          TestCase.assertTrue((from.equals(sender1)) || (from.equals(sender2)));
          if (from.equals(sender1)) {
            TestCase.assertEquals(((Token) not).bounce, bounce1);
            bounce1 -= 1;
          } else if (from.equals(sender2)) {
            TestCase.assertEquals(((Token) not).bounce, bounce2);
            bounce2 -= 1;
          }

          if ((((Token) not).bounce %10) == 9)
            System.out.println("recv#" + ((Token) not).bounce + " from " + from);

          if (((Token) not).bounce == 0) {
            stop -= 1;
            if (stop == 0) {
              System.out.println("Stop");
              TestCase.endTest();
              // never reached
            }
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

  static class Sender extends Agent {
    AgentId receiver;

    Sender(short serverId) {
      super(serverId);
    }

    public void react(AgentId from, Notification not) throws Exception {
      if(not instanceof Go) {
        if (((Go) not).bounce >= 0) {
          Token token = new Token(((Go) not).bounce, 1000);
          sendTo(receiver, token);

          ((Go) not).bounce -= 1;
          sendTo(getId(), not);
        }
      } else {
        super.react(from, not);
      }
    }
  }
}
