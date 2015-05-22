/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2011 ScalAgent Distributed Technologies
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
package jmxconnector;

import java.net.ConnectException;

import javax.naming.NamingException;

import org.objectweb.joram.client.jms.admin.AdminException;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

/**
 * This class creates and registers all needed stuf for JMX Connector sample.
 */
public class JMXConnectorAdmin {

  /**
   * @param args
   * @throws AdminException 
   * @throws ConnectException 
   * @throws NamingException 
   */
  public static void main(String[] args) throws ConnectException, AdminException, NamingException {
    System.out.println("Launch of administration for JMXConnector sample");
    
    // Create the ConnectionFactory
    javax.jms.ConnectionFactory cf = TcpConnectionFactory.create("localhost", 16010);
    
    // Create needed JMS administered objects
    AdminModule.connect(cf, "root", "root");
    User.create("anonymous", "anonymous");
    AdminModule.disconnect();
    
    // is recorder in the JNDI, the connectionFactory
    javax.naming.Context jndiCtx = new javax.naming.InitialContext();
    jndiCtx.bind("ConnectionFactory", cf);
    jndiCtx.close();
    
    System.out.println("Admin closed.");
  }

}
