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

public class test12 extends TestCase {
  static short s0 = (short) 0;
  static short s1 = (short) 1;
  static short s2 = (short) 2;

  public test12() {
    super();
  }

  protected void setUp() throws Exception {
    timeout = 1200000L;
    
    Controller controller = new Controller(s0);

    startAgentServer(s1);
    startAgentServer(s2);

    controller.deploy();
    Channel.sendTo(controller.getId(), new Activate());
  }

  protected void tearDown() {
    crashAgentServer(s1);
    crashAgentServer(s2);
  }

  public static void main(String args[]) {
    new test12().runTest(args);
  }

  static class Activate extends Notification {
  }

  static class Token extends Notification {
  }

  static class Controller extends Agent {
    AgentId sender;
    AgentId receiver;

    int senderStatus, receiverStatus;

    int step = 0;

    Controller(short serverId) {
      super(serverId);
    }

    public void react(AgentId from, Notification not) throws Exception {
      System.out.println("step#" + step + ", " + not + " from " + from);

      try {
        if (step == 0) {
          if(not instanceof Activate) {
            Sender s = new Sender(s1);
            Receiver r = new Receiver(s2);

            sender = s.getId();
            receiver = r.getId();

            s.receiver = receiver;

            s.deploy(getId());
            r.deploy(getId());

            senderStatus = 1;
            receiverStatus = 1;
          } else {
            throw new Exception("Step#" + step +
                                ", bad notification:" + not +
                                " from " + from);
          }
        } else if ((step == 1) || (step == 2)) {
          if (not instanceof AgentCreateReply) {
            if (sender.equals(((AgentCreateReply) not).agent) &&
                (senderStatus == 1)) {
              senderStatus += 1;
              sendTo(sender, new Activate());
            } else if (receiver.equals(((AgentCreateReply) not).agent) &&
                       (receiverStatus == 1)) {
              receiverStatus += 1;
              sendTo(receiver, new Activate());
            } else {
              throw new Exception("Step#" + step +
                                  ", bad create reply from " + from);
            }
          } else {
            throw new Exception("Step#" + step +
                                ", bad notification:" + not +
                                " from " + from);
          }
        } else if (step == 3) {
          if ((not instanceof Token) && from.equals(receiver)) {
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
      step += 1;
    }
  }

  static class Sender extends Agent {
    AgentId receiver;

    Sender(short serverId) {
      super(serverId);
    }

    public void react(AgentId from, Notification not) throws Exception {
      if(not instanceof Activate) {
        sendTo(receiver, new Token());
      } else {
        super.react(from, not);
      }
    }
  }

  static class Receiver extends Agent {
    AgentId controller;
    boolean token = false;

    Receiver(short serverId) {
      super(serverId);
    }

    public void react(AgentId from, Notification not) throws Exception {
      if(not instanceof Activate) {
        controller = from;
      } else if (not instanceof Token) {
        token = true;
      } else {
        super.react(from, not);
      }
      if (token && (controller != null))
        sendTo(controller, new Token());
    }
  }
}
