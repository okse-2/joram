/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2012 ScalAgent Distributed Technologies
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
package com.scalagent.ctrlgreen;

import java.io.File;
import java.io.PrintStream;
import java.net.ConnectException;

import org.objectweb.joram.client.jms.ConnectionFactory;
import org.objectweb.joram.client.jms.FactoryParameters;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminException;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.local.LocalConnectionFactory;

import fr.dyade.aaa.agent.AgentServer;

public class ServerFramework {  
  static Queue actionQueue = null;
  static Topic CMDBTopic = null;
  static Topic VMWareTopic = null;
  static Queue parameterQueue = null;
  
  static Queue DMQ = null;

  public static void main(String[] args) throws Exception {
    File a3conf = new File("a3servers.xml");
    if (! a3conf.exists()) {
      PrintStream ps = new PrintStream(a3conf);
      ps.println("<config>");
      ps.println("<property name=\"Transaction\" value=\"fr.dyade.aaa.util.NullTransaction\"/>");
      ps.println("  <server id=\"0\" name=\"s0\" hostname=\"localhost\">");
      ps.println("   <service class=\"org.objectweb.joram.mom.proxies.ConnectionManager\" args=\"root root\"/>");
      ps.println("   <service class=\"org.objectweb.joram.mom.proxies.tcp.TcpProxyService\" args=\"16010\"/>");
      ps.println("   <service class=\"fr.dyade.aaa.jndi2.server.JndiServer\" args=\"16400\"/>");
      ps.println("  </server>");
      ps.println("</config>");
      ps.close();
    }
    AgentServer.init(new String[] {"0", "./s0"});
    AgentServer.start();
    Thread.sleep(1000L);
    
    ConnectionFactory cf = LocalConnectionFactory.create();
    FactoryParameters parameters = cf.getParameters();
    parameters.connectingTimer = 10;
    parameters.cnxPendingTimer = 5000;
    
    try {
      AdminModule.connect(cf, "root", "root");
    } catch (Exception exc) {
      Trace.fatal("Cannot connect.", exc);
      throw exc;
    }

    try {
      DMQ = org.objectweb.joram.client.jms.Queue.create("DMQ");
      DMQ.setFreeReading();
      DMQ.setFreeWriting();
      DMQ.registerAsDefaultDMQ();
      actionQueue = org.objectweb.joram.client.jms.Queue.create("Action");
      actionQueue.setFreeReading();
      actionQueue.setFreeWriting();
      actionQueue.setDMQ(DMQ);
      CMDBTopic = org.objectweb.joram.client.jms.Topic.create("CMDB");
      CMDBTopic.setFreeReading();
      CMDBTopic.setFreeWriting();
      CMDBTopic.setDMQ(DMQ);
      VMWareTopic = org.objectweb.joram.client.jms.Topic.create("VMWare");
      VMWareTopic.setFreeReading();
      VMWareTopic.setFreeWriting();
      VMWareTopic.setDMQ(DMQ);
      parameterQueue= org.objectweb.joram.client.jms.Queue.create("Parameter");
      parameterQueue.setFreeReading();
      parameterQueue.setFreeWriting();
      parameterQueue.setDMQ(DMQ);
      User user = User.create("anonymous", "anonymous");
      user.setDMQ(DMQ);
    } catch (Exception exc) {
      Trace.fatal("Cannot get destinations.", exc);
      AdminModule.disconnect();
      throw exc;
    }
    AdminModule.disconnect();
    
    System.out.println("Server is ready.");
    System.in.read();
    System.exit(0);
  }
}
