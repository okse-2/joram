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
 * Initial developer(s): Jose Carlos Waeny
 * Contributor(s):
 */
package chat;

import fr.dyade.aaa.joram.admin.*;

import javax.jms.*;
import javax.naming.*;

/**
 * Launching JORAM administration:
 * connecting to JORAM server, creating chat agents and creating chat topic
 *
 * @author	JC Waeny 
 * @email       jc@waeny.2y.net
 * @version     1.0
 */
public class ChatAdmin
{
  static Context ictx = null;
    
  public static void main(String args[]) throws Exception
  {
    System.out.println();
    System.out.println("Chat administration phase... ");

    // Getting InitialContext:
    ictx = new InitialContext();
            
    // Connecting to JORAM server:
    Admin admin = new Admin("root", "root", 60);

    // Creating the JMS administered objects:        
    ConnectionFactory connFactory = admin.createConnectionFactory();
    Topic topicChat = admin.createTopic("topic");

    // Creating an access for user anonymous:
    User ano = admin.createUser("anonymous", "anonymous");

    // Setting free access to the topic:
    admin.setFreeReading("topic");
    admin.setFreeWriting("topic");
            
    // binding all in JNDI
    ictx.rebind("factoryChat", connFactory);
    ictx.rebind("topicChat", topicChat);
            
    ictx.close();
    admin.close();

    System.out.println("Admin closed.");
  }
}
