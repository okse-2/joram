/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - Bull SA
 * Copyright (C) 2004 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
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
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s): Nicolas Tachker (ScalAgent)
 */
package dotcom;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.QueueTcpConnectionFactory;
import org.objectweb.joram.client.jms.tcp.TopicTcpConnectionFactory;


/**
 * Launching JORAM administration: 
 * connecting to JORAM server, creating customer agents, creating topic
 * and creating queues.
 */
public class DotcomAdmin {

  public static void main(String args[]) throws Exception {
    
    System.out.println();
    System.out.println("Dotcom administration...");

    // connecting to JORAM server
    AdminModule.connect("root", "root", 60);
	    
    // setting users
    User web = User.create("web", "web", 0);
    User billing = User.create("billing", "billing", 0);
    User inventory = User.create("inventory", "inventory", 0);
    User customer = User.create("customer", "customer", 0);
    User control = User.create("control", "control", 0);
    User delivery = User.create("delivery", "delivery", 0);

    // Creating the administered objects:
    javax.jms.QueueConnectionFactory qcf =
      QueueTcpConnectionFactory.create("localhost", 16010);
    javax.jms.TopicConnectionFactory tcf =
      TopicTcpConnectionFactory.create("localhost", 16010);

    Topic tOrders = Topic.create(0);
    Queue qItems = Queue.create(0);
    Queue qCheck = Queue.create(0);
    Queue qChecked = Queue.create(0);
    Queue qBills = Queue.create(0);
    Queue qDelivery = Queue.create(0);

    // Setting access permissions:
    tOrders.setWriter(web);
    tOrders.setReader(billing);
    tOrders.setReader(inventory);
    tOrders.setReader(customer);
    qCheck.setWriter(billing);
    qCheck.setReader(control);
    qChecked.setWriter(control);
    qChecked.setReader(billing);
    qBills.setWriter(billing);
    qBills.setReader(customer);
    qItems.setWriter(inventory);
    qItems.setReader(customer);
    qDelivery.setWriter(customer);
    qDelivery.setReader(delivery);

    // Binding objects in JNDI:
    javax.naming.Context jndiCtx = new javax.naming.InitialContext();
    jndiCtx.bind("qcf", qcf);
    jndiCtx.bind("tcf", tcf);
    jndiCtx.bind("tOrders", tOrders);
    jndiCtx.bind("qItems", qItems);
    jndiCtx.bind("qCheck", qCheck);
    jndiCtx.bind("qChecked", qChecked);
    jndiCtx.bind("qBills", qBills);
    jndiCtx.bind("qDelivery", qDelivery);
    jndiCtx.close();

    AdminModule.disconnect();
    System.out.println("Admin closed.");
  }
}
