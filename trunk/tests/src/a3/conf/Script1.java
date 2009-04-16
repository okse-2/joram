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


public class Script1 extends TestScript {
  public Script1() {
    super();

    add(new NewDomainCmd("a.ApplicationDomain",
                         "fr.dyade.aaa.agent.SimpleNetwork"));
    add(new NewDomainCmd("a.HttpDomain",
                         "fr.dyade.aaa.agent.HttpNetwork"));
    add(new SetPropertyCmd("a.HttpDomain.ActivationPeriod",
                           "10000"));
    add(new NewNetworkCmd("s0",
                          "a.ApplicationDomain",
                          26000));
    add(new NewServerCmd("a.central",
                         "localhost"));
    add(new NewServiceCmd("a.central",
                          "fr.dyade.aaa.agent.AdminProxy",
                          "27303"));
    add(new NewNetworkCmd("a.central",
                          "a.HttpDomain",
                          4040));
    add(new NewNetworkCmd("a.central",
                          "a.ApplicationDomain",
                          27301));
    add(new NewServerCmd("a.local",
                         "localhost"));
    add(new NewServiceCmd("a.local",
                          "fr.dyade.aaa.agent.AdminProxy",
                          "28303"));
    add(new NewNetworkCmd("a.local",
                          "a.HttpDomain",
                          0));
  }

  public boolean test() throws Exception {
    A3CMLConfig conf = AgentServer.getAppConfig(
      new String[] {"a.ApplicationDomain",
                    "a.HttpDomain"});

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
      case 2:
// (fr.dyade.aaa.agent.ServerDesc@18f5824,sid=2,name=a.local,isTransient=false,hostname=localhost,addr=null,services=null,active=true,last=0,gateway=1,port=-1,domain=AgentServer#0.a.ApplicationDomain[fr.dyade.aaa.agent.SimpleNetwork])
        TestCase.assertEquals("desc#" + desc.getServerId() + ".name",
                              desc.getServerName(), "a.local");
        TestCase.assertEquals("desc#" + desc.getServerId() + ".hostname",
                              desc.getHostname(), "localhost");
        TestCase.assertNull("desc#" + desc.getServerId() + ".services",
                            desc.getServices());
        TestCase.assertEquals("desc#" + desc.getServerId() + ".gateway",
                              desc.getGateway(), 1);
        TestCase.assertEquals("desc#" + desc.getServerId() + ".port",
                              desc.getPort(), -1);
        TestCase.assertEquals("desc#" + desc.getServerId() + ".domain.class",
                              desc.getDomainType(),
                              Class.forName("fr.dyade.aaa.agent.SimpleNetwork"));
        TestCase.assertEquals("desc#" + desc.getServerId() + ".domain.name",
                              desc.getDomainName(),
                              "AgentServer#0.a.ApplicationDomain");
        break;
      case 1:
// (fr.dyade.aaa.agent.ServerDesc@1833eca,sid=1,name=a.central,isTransient=false,hostname=localhost,addr=null,services=null,active=true,last=0,gateway=1,port=27301,domain=AgentServer#0.a.ApplicationDomain[fr.dyade.aaa.agent.SimpleNetwork])
        TestCase.assertEquals("desc#" + desc.getServerId() + ".name",
                              desc.getServerName(), "a.central");
        TestCase.assertEquals("desc#" + desc.getServerId() + ".hostname",
                              desc.getHostname(), "localhost");
        TestCase.assertNull("desc#" + desc.getServerId() + ".services",
                            desc.getServices());
        TestCase.assertEquals("desc#" + desc.getServerId() + ".gateway",
                              desc.getGateway(), 1);
        TestCase.assertEquals("desc#" + desc.getServerId() + ".port",
                              desc.getPort(), 27301);
        TestCase.assertEquals("desc#" + desc.getServerId() + ".domain.class",
                              desc.getDomainType(),
                              Class.forName("fr.dyade.aaa.agent.SimpleNetwork"));
        TestCase.assertEquals("desc#" + desc.getServerId() + ".domain.name",
                              desc.getDomainName(),
                              "AgentServer#0.a.ApplicationDomain");
        break;
      case 0:
// (fr.dyade.aaa.agent.ServerDesc@ff2413,sid=0,name=s0,isTransient=false,hostname=localhost,addr=null,services=[Lfr.dyade.aaa.agent.ServiceDesc;@9980d5,active=true,last=0,gateway=-1,port=-1,domain=Engine#0))
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
