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
package soap;

import fr.dyade.aaa.joram.admin.*;
import fr.dyade.aaa.joram.soap.TopicSoapConnectionFactory;


/**
 * Administers a platform for the soap samples.
 */
public class SoapAdmin
{
  public static void main(String[] args) throws Exception
  {
    System.out.println();
    System.out.println("Soap administration...");

    TopicSoapConnectionFactory cnxFact =
      new TopicSoapConnectionFactory("localhost", 8080, 60);
    cnxFact.getParameters().connectingTimer = 60;

    SoapExt_AdminItf admin = new SoapExt_AdminImpl();
    admin.connect(cnxFact, "root", "root");

    javax.jms.ConnectionFactory soapCf = admin.createSoapConnectionFactory(1);

    javax.jms.Queue queue = admin.createQueue(0);
    javax.jms.Topic topic = admin.createTopic(0);

    User soapUser = admin.createSoapUser("anonymous", "anonymous", 1);

    admin.setFreeReading(queue);
    admin.setFreeWriting(queue);
    admin.setFreeReading(topic);
    admin.setFreeWriting(topic);

    admin.disconnect();

    javax.naming.Context jndiCtx = new javax.naming.InitialContext();
    jndiCtx.bind("soapCf", soapCf);
    jndiCtx.bind("queue", queue);
    jndiCtx.bind("topic", topic);
    jndiCtx.close();

    System.out.println("Admin finished.");
  }
}
