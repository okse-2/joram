/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
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
 * Contributor(s):
 */
package dotcom;

import fr.dyade.aaa.joram.admin.*;


/**
 * Launching JORAM administration: 
 * connecting to JORAM server, creating customer agents, creating topic
 * and creating queues.
 */
public class DotcomAdmin 
{
  public static void main(String args[]) throws Exception
  {
    System.out.println();
    System.out.println("Dotcom administration...");

    // connecting to JORAM server
    AdminItf admin = new fr.dyade.aaa.joram.admin.AdminImpl();
    admin.connect("root", "root", 60);
	    
    // setting users
    User web = admin.createUser("web", "web", 0);
    User billing = admin.createUser("billing", "billing", 0);
    User inventory = admin.createUser("inventory", "inventory", 0);
    User customer = admin.createUser("customer", "customer", 0);
    User control = admin.createUser("control", "control", 0);
    User delivery = admin.createUser("delivery", "delivery", 0);

    // Creating the administered objects:
    javax.jms.QueueConnectionFactory qcf =
      admin.createQueueConnectionFactory("localhost", 16010);
    javax.jms.TopicConnectionFactory tcf =
      admin.createTopicConnectionFactory("localhost", 16010);

    javax.jms.Topic tOrders = admin.createTopic(0);
    javax.jms.Queue qItems = admin.createQueue(0);
    javax.jms.Queue qCheck = admin.createQueue(0);
    javax.jms.Queue qChecked = admin.createQueue(0);
    javax.jms.Queue qBills = admin.createQueue(0);
    javax.jms.Queue qDelivery = admin.createQueue(0);

    // Setting access permissions:
    admin.setWriter(web, tOrders);
    admin.setReader(billing, tOrders);
    admin.setReader(inventory, tOrders);
    admin.setReader(customer, tOrders);
    admin.setWriter(billing, qCheck);
    admin.setReader(control, qCheck);
    admin.setWriter(control, qChecked);
    admin.setReader(billing, qChecked);
    admin.setWriter(billing, qBills);
    admin.setReader(customer, qBills);
    admin.setWriter(inventory, qItems);
    admin.setReader(customer, qItems);
    admin.setWriter(customer, qDelivery);
    admin.setReader(delivery, qDelivery);

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

    admin.disconnect();
    System.out.println("Admin closed.");
  }
}
