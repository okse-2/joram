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

import java.io.*;
import java.util.*;


import fr.dyade.aaa.agent.*;
import fr.dyade.aaa.agent.conf.*;
import fr.dyade.aaa.admin.script.*;
import fr.dyade.aaa.admin.cmd.*;
import framework.TestCase;


public class Script3 extends TestScript {
  public Script3() {
    super();

    add(new NewDomainCmd("a.applicationDomain","fr.dyade.aaa.agent.SimpleNetwork"));
    add(new NewDomainCmd("a.intranetConnector","fr.dyade.aaa.agent.HttpNetwork"));
    add(new SetPropertyCmd("a.intranetConnector.ActivationPeriod","10000"));
    add(new NewDomainCmd("a.scalagentConnector","fr.dyade.aaa.agent.HttpNetwork"));
    add(new SetPropertyCmd("a.scalagentConnector.ActivationPeriod","10000"));
    add(new NewNetworkCmd("s0","a.applicationDomain",26000));
    add(new NewServerCmd("a.intranet","moorea.scalagent.fr"));
    add(new SetJvmArgsCmd("a.intranet",""));
    add(new NewServiceCmd("a.intranet","fr.dyade.aaa.agent.AdminProxy","7010"));
    add(new NewServiceCmd("a.intranet","fr.dyade.aaa.agent.HttpDebug","7020"));
    add(new NewNetworkCmd("a.intranet","a.intranetConnector",0));
    add(new NewServiceCmd("a.intranet","fr.dyade.aaa.task.Scheduler",""));
    add(new NewServerCmd("a.moonbooster","SYLFELINE"));
    add(new SetJvmArgsCmd("a.moonbooster",""));
    add(new NewServiceCmd("a.moonbooster","fr.dyade.aaa.agent.AdminProxy","6010"));
    add(new NewServiceCmd("a.moonbooster","fr.dyade.aaa.agent.HttpDebug","6020"));
    add(new NewNetworkCmd("a.moonbooster","a.intranetConnector",6100));
    add(new NewNetworkCmd("a.moonbooster","a.applicationDomain",6030));
    add(new NewNetworkCmd("a.moonbooster","a.scalagentConnector",6200));
    add(new NewServiceCmd("a.moonbooster","fr.dyade.aaa.task.Scheduler",""));
    add(new NewServerCmd("a.scalagent","SYLFELINE"));
    add(new SetJvmArgsCmd("a.scalagent",""));
    add(new NewServiceCmd("a.scalagent","fr.dyade.aaa.agent.AdminProxy","8010"));
    add(new NewServiceCmd("a.scalagent","fr.dyade.aaa.agent.HttpDebug","8020"));
    add(new NewNetworkCmd("a.scalagent","a.scalagentConnector",0));
    add(new NewServiceCmd("a.scalagent","fr.dyade.aaa.task.Scheduler",""));
    add(new SetServerNatCmd("a.moonbooster",
                            "a.intranet", "193.170.1.127", 1111));
    add(new SetServerNatCmd("a.moonbooster",
                            "a.scalagent", "193.170.2.127", 2222));
  }

  public boolean test() throws Exception {
    A3CMLConfig conf = AgentServer.getAppConfig(
      new String[] {"a.applicationDomain",
                    "a.intranetConnector",
                    "a.scalagentConnector"});

    ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream("./a3cml.conf"));
    os.writeObject(conf);
    os.flush();
    os.close();

    ObjectInputStream is = new ObjectInputStream(new FileInputStream("./a3cmlref.conf"));
    A3CMLConfig ref = (A3CMLConfig) is.readObject();
    is.close();

    TestCase.assertEquals("AppConfig", conf, ref);

    for (Enumeration e = AgentServer.elementsServerDesc();
         e.hasMoreElements(); ) {
      ServerDesc desc = (ServerDesc)e.nextElement();

      switch (desc.getServerId()) {
      case 3:
// (fr.dyade.aaa.agent.ServerDesc@ca470,sid=3,name=a.scalagent,isTransient=false,hostname=SYLFELINE,addr=null,services=null,active=true,last=0,gateway=2,port=-1,domain=AgentServer#0.a.applicationDomain[fr.dyade.aaa.agent.SimpleNetwork])
        TestCase.assertEquals("desc#" + desc.getServerId() + ".name",
                              desc.getServerName(), "a.scalagent");
        TestCase.assertEquals("desc#" + desc.getServerId() + ".hostname",
                              desc.getHostname(), "SYLFELINE");
        TestCase.assertNull("desc#" + desc.getServerId() + ".services",
                            desc.getServices());
        TestCase.assertEquals("desc#" + desc.getServerId() + ".gateway",
                              desc.getGateway(), 2);
        TestCase.assertEquals("desc#" + desc.getServerId() + ".port",
                              desc.getPort(), -1);
        TestCase.assertEquals("desc#" + desc.getServerId() + ".domain.class",
                              desc.getDomainType(),
                              Class.forName("fr.dyade.aaa.agent.SimpleNetwork"));
        TestCase.assertEquals("desc#" + desc.getServerId() + ".domain.name",
                              desc.getDomainName(),
                              "AgentServer#0.a.applicationDomain");
        break;
      case 2:
// (fr.dyade.aaa.agent.ServerDesc@29e357,sid=2,name=a.moonbooster,isTransient=false,hostname=SYLFELINE,addr=null,services=null,active=true,last=0,gateway=2,port=6030,domain=AgentServer#0.a.applicationDomain[fr.dyade.aaa.agent.SimpleNetwork]), 1=
        TestCase.assertEquals("desc#" + desc.getServerId() + ".name",
                              desc.getServerName(), "a.moonbooster");
        TestCase.assertEquals("desc#" + desc.getServerId() + ".hostname",
                              desc.getHostname(), "SYLFELINE");
        TestCase.assertNull("desc#" + desc.getServerId() + ".services",
                            desc.getServices());
        TestCase.assertEquals("desc#" + desc.getServerId() + ".gateway",
                              desc.getGateway(), 2);
        TestCase.assertEquals("desc#" + desc.getServerId() + ".port",
                              desc.getPort(), 6030);
        TestCase.assertEquals("desc#" + desc.getServerId() + ".domain.class",
                              desc.getDomainType(),
                              Class.forName("fr.dyade.aaa.agent.SimpleNetwork"));
        TestCase.assertEquals("desc#" + desc.getServerId() + ".domain.name",
                              desc.getDomainName(),
                              "AgentServer#0.a.applicationDomain");
        break;
      case 1:
// (fr.dyade.aaa.agent.ServerDesc@e95a56,sid=1,name=a.intranet,isTransient=false,hostname=moorea.scalagent.fr,addr=null,services=null,active=true,last=0,gateway=2,port=-1,domain=AgentServer#0.a.applicationDomain[fr.dyade.aaa.agent.SimpleNetwork])
        TestCase.assertEquals("desc#" + desc.getServerId() + ".name",
                              desc.getServerName(), "a.intranet");
        TestCase.assertEquals("desc#" + desc.getServerId() + ".hostname",
                              desc.getHostname(), "moorea.scalagent.fr");
        TestCase.assertNull("desc#" + desc.getServerId() + ".services",
                            desc.getServices());
        TestCase.assertEquals("desc#" + desc.getServerId() + ".gateway",
                              desc.getGateway(), 2);
        TestCase.assertEquals("desc#" + desc.getServerId() + ".port",
                              desc.getPort(), -1);
        TestCase.assertEquals("desc#" + desc.getServerId() + ".domain.class",
                              desc.getDomainType(),
                              Class.forName("fr.dyade.aaa.agent.SimpleNetwork"));
        TestCase.assertEquals("desc#" + desc.getServerId() + ".domain.name",
                              desc.getDomainName(),
                              "AgentServer#0.a.applicationDomain");
        break;
      case 0:
// (fr.dyade.aaa.agent.ServerDesc@da3a1e,sid=0,name=s0,isTransient=false,hostname=localhost,addr=null,services=[Lfr.dyade.aaa.agent.ServiceDesc;@11dba45,active=true,last=0,gateway=-1,port=-1,domain=Engine#0)
        TestCase.assertEquals("desc#" + desc.getServerId() + ".name",
                              desc.getServerName(), "s0");
        TestCase.assertEquals("desc#" + desc.getServerId() + ".hostname",
                              desc.getHostname(), "localhost");
//         TestCase.assertNull("desc#" + desc.getServerId() + ".services",
//                             desc.getServices());
        TestCase.assertEquals("desc#" + desc.getServerId() + ".gateway",
                              desc.getGateway(), -1);
        TestCase.assertEquals("desc#" + desc.getServerId() + ".port",
                              desc.getPort(), -1);
        TestCase.assertEquals("desc#" + desc.getServerId() + ".domain.class",
                              desc.getDomainType(),
                              Class.forName("fr.dyade.aaa.agent.Engine"));
        TestCase.assertEquals("desc#" + desc.getServerId() + ".domain.name",
                              desc.getDomainName(),
                              "Engine#0");
        break;
      default:
        break;
      }
    }

    return true;
  }
}
