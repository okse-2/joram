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

public class test11 extends TestCase {
  short ServerSender;
  short ServerReceiver;

  public test11() {
    super();
  }

  protected void setUp() throws Exception {
    ServerSender = Integer.getInteger("Send", 0).shortValue();
    ServerReceiver = Integer.getInteger("Recv", 0).shortValue();

    int bounce = Integer.getInteger("bounce", 200).intValue();

    timeout = (long) (100000 * bounce);

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

    Channel.sendTo(sender.getId(), new Go());
  }

  protected void tearDown() {
    if (ServerSender != 0)
      crashAgentServer(ServerSender);
    if ((ServerReceiver != 0) && (ServerReceiver != ServerSender))
      crashAgentServer(ServerReceiver);
  }

  public static void main(String args[]) {
    new test11().runTest(args);
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

    AgentId receiver;

    Sender(short serverId) {
      super(serverId);
    }

    public void react(AgentId from, Notification not) throws Exception {
      try {
        if(not instanceof Go) {
          Token tok = new Token(bounce, 10);
          if (tok.bounce >= 0) {
            sendTo(receiver, tok);
            bounce -= 1;
            sendTo(getId(), new Go());
          }
        } else if (not instanceof Stop) {
          System.out.println("Stop");
          TestCase.endTest();
          // never reached
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
        if (((Token) not).bounce == 0) {
          sendTo(from, new Stop());
        }
      } else {
        super.react(from, not);
      }
    }
  }
}
