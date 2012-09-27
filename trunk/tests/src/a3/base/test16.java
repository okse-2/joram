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

    receiver1.sender = sender.getId();
    receiver2.sender = sender.getId();

    sender.deploy();
    receiver1.deploy();
    receiver2.deploy();

    Channel.sendTo(sender.getId(), new Go(bounce));
  }

  protected void tearDown() {
    killAgentServer(ServerReceiver1);
    killAgentServer(ServerReceiver2);
  }

  public static void main(String args[]) {
    new test16().runTest(args);
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

  static class Sender extends Agent {
    int bounce = -999;
    int stop;

    AgentId receiver1;
    int bounce1;
    AgentId receiver2;
    int bounce2;

    Sender(short serverId) {
      super(serverId);
    }

    public void react(AgentId from, Notification not) throws Exception {
      try {
        if(not instanceof Go) {
          if (bounce == -999) {
            // firts time..
            stop = 2;

            bounce = ((Go) not).bounce;
            bounce1 = bounce;
            bounce2 = bounce;
          }

          Token tok = new Token(bounce, 1000);
          if (tok.bounce >= 0) {
            // Sends token to receivers
            sendTo(receiver1, tok);
            sendTo(receiver2, tok);
            // Sends a Go notification to loop one more time
            bounce -= 1;
            sendTo(getId(), not);
          }

//           if ((bounce %50) == 49)
//             System.out.println("sends " + bounce);

//           if (((bounce - bounce1) > 50) || ((bounce - bounce2) > 50)) {
//             System.out.println("sleeping..");
//             Thread.sleep(100L);
//           }
        } else if (not instanceof Token) {
          int tbounce = ((Token) not).bounce;
          TestCase.assertTrue((from.equals(receiver1)) || (from.equals(receiver2)));
          if (from.equals(receiver1)) {
            TestCase.assertEquals(((Token) not).bounce, bounce1);
            bounce1 -= 1;
          } else if (from.equals(receiver2)) {
            TestCase.assertEquals(((Token) not).bounce, bounce2);
            bounce2 -= 1;
          }

//           if ((((Token) not).bounce %10) == 9)
//             System.out.println("recv#" + ((Token) not).bounce + " from " + from);
        } else if (not instanceof Stop) {
//           System.out.println("Stop from " + from);

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
    AgentId sender;

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
