/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C)  2001 - 2009 ScalAgent Distributed Technologies
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

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import fr.dyade.aaa.agent.Agent;
import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.agent.Channel;
import fr.dyade.aaa.agent.Notification;
import framework.TestCase;

public class test9 extends TestCase {
  public test9() {
    super();
  }

  static short ServerPing = (short) 0;
  static short ServerPong = (short) 1;
  static int  nbStopTask =0;

  protected void setUp() throws Exception {
    startAgentServer(ServerPong, new String[] { "-DTransaction.UseLockFile=false" });

    int bounce = Integer.getInteger("bounce", 150).intValue();
    // bounce = 100;
    timeout = 1000L * bounce;
    timeout = Long.getLong("timeout", timeout).longValue();
    Timer timer = new Timer(true);

    Proxy proxy = new Proxy(ServerPing);
    Queue queue = new Queue(ServerPong);

    proxy.queue = queue.getId();
    proxy.bounce = bounce;

    proxy.deploy();
    queue.deploy();
    System.out.println("start");
    timer.schedule(new StopTask(proxy.getId(), new StopNot()), 5000L, 5000L);
    
  }

  protected void tearDown() {
    crashAgentServer(ServerPong);
  }


  public static void main(String args[]) {
    new test9().runTest(args);
  }

  static class Message extends Notification {
    public int bounce;
    public String report = null;

    public Message(int bounce) {
      detachable = true;
      this.bounce = bounce;
    }

    public Message(int bounce, String report) {
      this(bounce);
      this.report = report;
    }

    public boolean detach() {
      detached = true;
      return detachable;
    }

    public StringBuffer toString(StringBuffer output) {
      output.append('(');
      super.toString(output);
      output.append(",bounce=").append(bounce);
      output.append(')');

      return output;
    }
  }

  static class StartNot extends Notification {}

  static class StopNot extends Notification {}

  static class StopTask extends TimerTask {
    AgentId to;
    Notification not;
    

    StopTask(AgentId to, Notification not) {
      this.to = to;
      this.not = not;
    }

    public void run() {
	Channel.sendTo(to, not);
    }
  }

  static class Proxy extends Agent {
    AgentId queue;
    int bounce;
    Random rand = null;

    public Proxy(short to) {
      super(to);
      rand = new Random(0x1234L);
    }

    protected void agentInitialize(boolean firstime) throws Exception {
      if (firstime) {
        sendTo(getId(), new StartNot());
      }
    }

    public void react(AgentId from, Notification not) {
      try {
        if (not instanceof StartNot) {
          sendTo(queue, new Message(bounce));
        } else if (not instanceof StopNot) {
          if (rand.nextBoolean()) {
            System.out.println("stop");
            TestCase.stopAgentServer(test9.ServerPong);
          } else {
            System.out.println("crash");
            TestCase.crashAgentServer(test9.ServerPong);
          }
          // Wait in order to prevent WAIT status on TCP connection
          Thread.sleep(500L);
          // Start server#1
          TestCase.startAgentServer(test9.ServerPong, new String[] { "-DTransaction.UseLockFile=false" });
          nbStopTask++;
          if (nbStopTask > 20)
            endTest();
        } else if (not instanceof Message) {
          Message msg = (Message) not;

          if ((bounce % 10) == 0)
            System.out.println("bounce: " + bounce);
          if (bounce == 1)
            System.out.println("last");
          assertTrue(from.equals(queue));
          assertEquals(msg.bounce, bounce);

          if (msg.bounce > 0) {
            bounce -= 1;
            sendTo(queue, new Message(bounce));
          } else {
            System.out.println(msg.report);
            endTest();
          }
        }
      } catch (Exception exc) {
        error(exc);
        endTest();
      }
    }
  }

  static class Queue extends Agent {
    int bounce = -1;
    Vector messages = null;

    public Queue(short to) {
      super(to);
      messages = new Vector();
    }

    public void react(AgentId from, Notification not) throws Exception {
      if (not instanceof Message) {
        Message msg = (Message) not;

        if (bounce == -1) bounce = ((Message) not).bounce;

        if (msg.bounce > 0) {
          messages.add(msg.getMessageId());
          msg.detach();
          sendTo(from, msg);
          Thread.sleep(100L);
        } else if (msg.bounce == 0) {
          StringBuffer strbuf = new StringBuffer();

          String name = null;
          Message msg2 = null;
          for (int i=0; i<messages.size(); i++) {
            name = (String) messages.elementAt(i);
            msg2 = (Message) AgentServer.getTransaction().load(name);

            strbuf.append(name).append("-->").append(msg2.bounce).append('\n');
            
            if (msg2.bounce != bounce) break;
            bounce -= 1;
          }
          sendTo(from, new Message(bounce, strbuf.toString()));
        }
      }
    }
  }
}
