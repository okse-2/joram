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

public class httpserver {
  static short ServerReceiver1 = 1;
  static short ServerReceiver2 = 2;

  public static void main(String args[]) throws Exception {
    AgentServer.init(new String[] {"0", "./s0"});
    AgentServer.start();

    Thread.sleep(100L);

    int bounce = Integer.getInteger("bounce", 10000).intValue();
    int payload = Integer.getInteger("payload", 10000).intValue();

    Sender sender = new Sender((short) 0);
    Receiver receiver1 = new Receiver(ServerReceiver1);
    Receiver receiver2 = new Receiver(ServerReceiver2);

    sender.receiver1 = receiver1.getId();
    sender.receiver2 = receiver2.getId();
    receiver1.sender = sender.getId();
    receiver2.sender = sender.getId();

    sender.deploy();
    receiver1.deploy();
    receiver2.deploy();

    Channel.sendTo(sender.getId(), new Go(bounce));
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

          bounce = ((Go) not).bounce;

          sendTo(receiver1, not);
          sendTo(receiver2, not);
        } else if (not instanceof Token) {
          System.out.println("recv#" + ((Token) not).bounce + " from " + from);
          if (((Token) not).bounce == 0) {
            stop -= 1;
            if (stop == 0) {
              System.out.println("Stop");
              System.exit(-1);
              // never reached
            }
          }
        } else {
          super.react(from, not);
        }
      }  catch (Throwable exc) {
        exc.printStackTrace();
      }
    }
  }

  static class Receiver extends Agent {
    AgentId sender;

    Receiver(short serverId) {
      super(serverId);
    }

    public void react(AgentId from, Notification not) throws Exception {
      if(not instanceof Go) {
        Token token = new Token(((Go) not).bounce, 10000);
        sendTo(sender, token);
        sendTo(getId(), token);
      } else if (not instanceof Token) {
//         if ((((Token) not).bounce %10) == 9)
          Thread.sleep(5000L);

        if (((Token) not).bounce > 0) {
          ((Token) not).bounce -= 1;

          sendTo(sender, not);
          sendTo(getId(), not);
        }
      } else {
        super.react(from, not);
      }
    }
  }
}
