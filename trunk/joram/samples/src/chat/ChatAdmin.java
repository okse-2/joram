/*
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
 * The present code contributor is JC Waeny.
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

    // Updating the administrator's identity and disconnecting:
    admin.addAdminId("admin", "pass");
    admin.close();

    // Connecting with the new identity and removing the default one:
    admin = new fr.dyade.aaa.joram.admin.Admin("admin", "pass", 60);
    admin.delAdminId("root");
    
    // Creating the JMS administered objects:        
    TopicConnectionFactory connFactory = admin.createTopicConnectionFactory();
    Topic topicChat = admin.createTopic("topic");

    // Creating a user (anonymous):
    User user = admin.createUser("anonymous", "anonymous");

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
