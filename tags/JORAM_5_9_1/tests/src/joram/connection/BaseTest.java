/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - 2012 ScalAgent Distributed Technologies
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
 * Initial developer(s): Freyssinet Andre (ScalAgent D.T.)
 * Contributor(s): Badolle Fabien (ScalAgent D.T.)
 */
package joram.connection;

import java.lang.reflect.Method;

import javax.jms.ConnectionFactory;

import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.local.LocalConnectionFactory;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import fr.dyade.aaa.agent.AgentServer;

public class BaseTest extends framework.BaseTestCase{

  static void startServer() throws Exception {
    AgentServer.init((short) 0, "./s0", null);
    AgentServer.start();

    Thread.sleep(1000L);
  }

  static void AdminConnect(String classname) throws Exception {
    Class c = Class.forName(classname);
    Method m = c.getMethod("AdminConnect", new Class[0]);
    m.invoke(null, new Object[0]);
  }

  static ConnectionFactory createConnectionFactory(String classname) throws Exception {
    Class c = Class.forName(classname);
    Method m = c.getMethod("createConnectionFactory", new Class[0]);
    return (ConnectionFactory) m.invoke(null, new Object[0]);
  }

  static Destination createDestination(String classname) throws Exception {
    Class c = Class.forName(classname);
    Method m = c.getMethod("create", new Class[]{int.class});
    return (Destination) m.invoke(null, new Object[]{new Integer(0)});
  }

  static Destination createDestination(String classname,
                                       String name) throws Exception {
    Class c = Class.forName(classname);
    Method m = c.getMethod("create",
                           new Class[]{int.class,
                                       java.lang.String.class});
    return (Destination) m.invoke(null, new Object[]{new Integer(0), name});
  }

  static Destination createDestination(String classname,
                                       int sid,
                                       String name) throws Exception {
    Class c = Class.forName(classname);
    Method m = c.getMethod("create",
                           new Class[]{int.class,
                                       java.lang.String.class});
    return (Destination) m.invoke(null, new Object[]{new Integer(sid), name});
  }
}

class ColocatedBaseTest {
  public static void AdminConnect() throws Exception {
    AdminModule.collocatedConnect("root", "root");
  }

  public static ConnectionFactory createConnectionFactory() throws Exception {
    return new LocalConnectionFactory();
  }
}

class TcpBaseTest {
  public static void AdminConnect() throws Exception {
    String host = System.getProperty("hostname", "localhost");
    int port = Integer.getInteger("port", 16010).intValue();
      
    AdminModule.connect(host, port, "root", "root", 60);
  }

  public static ConnectionFactory createConnectionFactory() throws Exception {
    String host = System.getProperty("hostname", "localhost");
    int port = Integer.getInteger("port", 16010).intValue();

    return TcpConnectionFactory.create(host, port);
  }
}
