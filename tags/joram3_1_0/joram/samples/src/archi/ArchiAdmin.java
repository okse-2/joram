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
package archi;

import fr.dyade.aaa.joram.admin.*;

import javax.jms.*;
import javax.naming.*;

public class ArchiAdmin
{
  static Context ictx = null; 
    
  public static void main(String args[]) throws Exception
  {
    System.out.println();
    System.out.println("Archi administration...");

    // Getting InitialContext:
    ictx = new InitialContext();

    // Connecting to the servers as the default administrator:
    Admin admin0 = new Admin("localhost", 16010, "root", "root", 120);
    Admin admin1 = new Admin("localhost", 16011, "root", "root", 120);
    Admin admin2 = new Admin("localhost", 16012, "root", "root", 120);

    // Creating access for user anonymous:
    User ano0 = admin0.createUser("anonymous", "anonymous");
    User ano2 = admin2.createUser("anonymous", "anonymous");

    // Creating the destinations on server 1:
    Queue queue = admin1.createQueue("queue");
    Topic topic = admin1.createTopic("topic");

    // Setting free access to the destinations:
    admin1.setFreeReading("queue");
    admin1.setFreeReading("topic");
    admin1.setFreeWriting("queue");
    admin1.setFreeWriting("topic");

    // Creating the connection factories for connecting to the servers 0 and 2:
    ConnectionFactory cf0 = admin0.createConnectionFactory();
    ConnectionFactory cf2 = admin2.createConnectionFactory();

    // Registering the administered objects in JNDI:
    ictx.rebind("cf0", cf0);
    ictx.rebind("cf2", cf2);
    ictx.rebind("queue", queue);
    ictx.rebind("topic", topic);

    ictx.close();
    admin0.close();
    admin1.close();
    admin2.close();

    System.out.println("Admins closed.");
  } 
}
