/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - Bull SA
 * Copyright (C) 2004 - ScalAgent DT
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
 * Initial developer(s): Frederic Maistre (Bull SA)
 * Contributor(s): Nicolas Tachker (ScalAgent)
 */
package bridge;

import org.objectweb.joram.client.jms.admin.*;
import org.objectweb.joram.client.jms.*;
import org.objectweb.joram.client.jms.tcp.*;

import java.util.Properties;


/**
 * Administers an agent server for the bridge sample.
 */
public class BridgeAdmin
{
  public static void main(String[] args) throws Exception
  {
    System.out.println();
    System.out.println("Bridge administration...");

    AdminModule.connect("root", "root", 60);

    // Setting the bridge properties
    Properties prop = new Properties();
    // Communication mode: PTP
    prop.setProperty("jmsMode", "ptp");
    // Foreign QueueConnectionFactory JNDI name: foreignCF
    prop.setProperty("connectionFactoryName", "foreignCF");
    // Foreign Queue JNDI name: foreignDest
    prop.setProperty("destinationName", "foreignDest");

    // Creating a Topic bridge on server 0:
    Topic bridgeD = Topic.create(0,
                                 "org.objectweb.joram.mom.dest.BridgeTopic",
                                 prop);

    bridgeD.setFreeReading();
    bridgeD.setFreeWriting();

    javax.jms.ConnectionFactory cf = TcpConnectionFactory.create();

    User user = User.create("anonymous", "anonymous", 0);

    javax.naming.Context jndiCtx = new javax.naming.InitialContext();

    jndiCtx.bind("bridgeD", bridgeD);
    jndiCtx.bind("cf", cf);
    
    jndiCtx.close();

    AdminModule.disconnect();
    System.out.println("Admin closed.");
  }
}
