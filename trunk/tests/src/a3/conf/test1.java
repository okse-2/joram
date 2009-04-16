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

package a3.conf;


import fr.dyade.aaa.agent.*;
import fr.dyade.aaa.agent.conf.*;
import fr.dyade.aaa.admin.script.*;
import fr.dyade.aaa.admin.cmd.*;
import framework.TestCase;

public class test1 extends TestCase {
  public test1() {
    super();
  }

  protected void setUp() throws Exception {
    boolean autoStart = Boolean.getBoolean("autoStart");
    String classname = System.getProperty("classname");

    timeout = 30000L;
    Test1Agent agent = new Test1Agent(classname);
    agent.deploy();
    Channel.sendTo(agent.getId(), new Notification());
  }

  public static void main(String args[]) {
    new test1().runTest(args);
  }
}

class Test1Agent extends Agent {
  int state = 0;
  AgentId admin = null;

  String classname = null;
  String xmlref = null;

  TestScript script = null;

  public Test1Agent(String classname) {
    super();

    this.classname = classname;

    admin = new AgentId(AgentServer.getServerId(),
                        AgentServer.getServerId(),
                        AgentId.AdminIdStamp);
  }

  public void react(AgentId from, Notification not) {
    try {
      switch (state) {
      case 0:
        TestCase.assertTrue(from.isNullId());
        TestCase.assertEquals("fr.dyade.aaa.agent.Notification",
                              not.getClass().getName());

        script = (TestScript) Class.forName(classname).newInstance();
        sendTo(admin, new AdminRequestNot(script, true, false));
        break;
      case 1:
        TestCase.assertEquals(admin, from);
        TestCase.assertEquals("fr.dyade.aaa.agent.AdminAckStartStopNot",
                              not.getClass().getName());

        if (not instanceof AdminAckStartStopNot) {
          AdminAckStartStopNot reply = (AdminAckStartStopNot) not;

          A3CMLConfig a3cmlconfig = AgentServer.getConfig();
          A3CML.toXML(a3cmlconfig, ".", "a3servers.xml");
          TestCase.assertFileSameContent("a3servers.xml", "ref.xml");
          script.test();
        }

        TestCase.endTest();
        break;
      }
    } catch (Throwable exc) {
      exc.printStackTrace();
      TestCase.endTest();
    } finally {
      state += 1;
    }
  }
}
