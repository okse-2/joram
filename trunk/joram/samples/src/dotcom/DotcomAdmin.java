/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
 *
 * The contents of this file are subject to the Joram Public License,
 * as defined by the file JORAM_LICENSE.TXT 
 * 
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License on the Objectweb web site
 * (www.objectweb.org). 
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific terms governing rights and limitations under the License. 
 * 
 * The Original Code is Joram, including the java packages fr.dyade.aaa.agent,
 * fr.dyade.aaa.ip, fr.dyade.aaa.joram, fr.dyade.aaa.mom, and
 * fr.dyade.aaa.util, released May 24, 2000.
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
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
