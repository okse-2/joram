/*
 * Copyright (C) 2002 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
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
 * The present code contributor is ScalAgent Distributed Technologies.
 */
package dotcom;

import fr.dyade.aaa.joram.admin.*;

import javax.jms.*;
import javax.naming.*;

/**
 * Launching JORAM administration: 
 * connecting to JORAM server, creating customer agents, creating topic
 * and creating queues.
 *
 * @author	Maistre Frederic
 */
public class DotcomAdmin 
{
  static Context ictx = null;

  public static void main(String args[]) throws Exception
  {
    System.out.println();
    System.out.println("Dotcom administration...");

    // getting InitialContext
    ictx = new InitialContext();
    
    // connecting to JORAM server
    Admin admin = new Admin("root", "root", 60);
	    
    // setting users
    User web = admin.createUser("web", "web");
    User billing = admin.createUser("billing", "billing");
    User inventory = admin.createUser("inventory", "inventory");
    User customer = admin.createUser("customer", "customer");
    User control = admin.createUser("control", "control");
    User delivery = admin.createUser("delivery", "delivery");

    // Creating the administered objects:
    QueueConnectionFactory qcf = admin.createQueueConnectionFactory();
    TopicConnectionFactory tcf = admin.createTopicConnectionFactory();

    Topic tOrders = admin.createTopic("tOrders");
    Queue qItems = admin.createQueue("qItems");
    Queue qCheck = admin.createQueue("qCheck");
    Queue qChecked = admin.createQueue("qChecked");
    Queue qBills = admin.createQueue("qBills");
    Queue qDelivery = admin.createQueue("qDelivery");

    // Setting access permissions:
    admin.setWriter(web, "tOrders");
    admin.setReader(billing, "tOrders");
    admin.setReader(inventory, "tOrders");
    admin.setReader(customer, "tOrders");
    admin.setWriter(billing, "qCheck");
    admin.setReader(control, "qCheck");
    admin.setWriter(control, "qChecked");
    admin.setReader(billing, "qChecked");
    admin.setWriter(billing, "qBills");
    admin.setReader(customer, "qBills");
    admin.setWriter(inventory, "qItems");
    admin.setReader(customer, "qItems");
    admin.setWriter(customer, "qDelivery");
    admin.setReader(delivery, "qDelivery");

    // Binding them:
    ictx.rebind("qcf", qcf);
    ictx.rebind("tcf", tcf);
    ictx.rebind("tOrders", tOrders);
    ictx.rebind("qItems", qItems);
    ictx.rebind("qCheck", qCheck);
    ictx.rebind("qChecked", qChecked);
    ictx.rebind("qBills", qBills);
    ictx.rebind("qDelivery", qDelivery);

    ictx.close();
    admin.close();

    System.out.println("Admin closed.");
  }
}
