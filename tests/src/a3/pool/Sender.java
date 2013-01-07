/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C)  2012 ScalAgent Distributed Technologies
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
 * Initial developer(s): ScalAgent Distributed Technologies
 * Contributor(s): 
 */
package a3.pool;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import fr.dyade.aaa.agent.Agent;
import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.agent.Channel;
import fr.dyade.aaa.agent.Notification;
import framework.TestCase;

/**
 * Send a big notification to the receiver.
 * The SO_TIMEOUT is set on the socket (see a3servers.xml)
 * We must see this information log :
 * "The session is active but Read timed out (SocketTimeoutException), nothing to do continue."
 * And all notifications are sent.
 */
public class Sender extends TestCase {
  
  public static void main(String args[]) throws Exception {
    new Sender().runTest(args);
  }
  
  protected void setUp() throws Exception {
    SenderAg sender = new SenderAg(AgentServer.getServerId());
    sender.receiver = AgentId.fromString("#0.0.1025");
    sender.deploy();
    // send init token
    Channel.sendTo(sender.getId(), new Token(1, 100000000, 500));
  }
  
  public static void stop() {
    AgentServer.stop(false);
    
    String log = "server.log.0.1";
    assertFileExist(log);
    assertTrue("The file \"" + log + "\" not contains \"The session is active but Read timed out\"", isFileContains(log, "The session is active but Read timed out"));
    endTest();
  }
  
  public static boolean isFileContains(String fileName, String str) {
    try {
    BufferedReader f = new BufferedReader( new FileReader(fileName));
    boolean ret = false;
      String line1 = f.readLine();
      while (line1 != null) {
        if (line1.contains(str)) {
          ret = true;
          break;
        }
        line1 = f.readLine();
      }
      f.close();
      return ret;
    } catch (IOException e) {
      return false;
    }
  }

  static class SenderAg  extends Agent {
    short serverId;
    AgentId receiver;

    public SenderAg(short serverId) {
      super(serverId);
      this.serverId = serverId;
    }

    public void react(AgentId from, Notification not) {
      try {
        if (not instanceof Token) {
          Token token = (Token) not;

          if (from.equals(receiver)) {
            System.out.println("Sender: receive bounce... " + token.bounce + ", from = " + from);
          }

          Thread.sleep(token.pause);

          token.bounce -= 1;
          sendTo(receiver, token);
          System.out.println("Sender: bounce = " + token.bounce + " sended to " + receiver);

          if (token.bounce > 0)
            sendTo(getId(), not);
          else if (from.getStamp() > 0) {
            System.out.println("End test: stop server " + serverId);
            stop();
          }
        } else {
          super.react(from, not);
        }
      }  catch (Throwable exc) {
        error(exc);
        exc.printStackTrace();
      }
    }
  }
}
