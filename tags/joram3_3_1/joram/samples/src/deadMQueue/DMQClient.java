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
package deadMQueue;

import fr.dyade.aaa.joram.admin.*;

import javax.jms.*;
import javax.naming.*;

/**
 * Producer/Consumer generating dead messages.
 */
public class DMQClient
{
  static Context ictx = null; 

  public static void main(String[] args) throws Exception
  {
    ictx = new InitialContext();
    Queue queue = (Queue) ictx.lookup("queue");
    Topic topic = (Topic) ictx.lookup("topic");
    ConnectionFactory cf = (ConnectionFactory) ictx.lookup("cnxFact");
    ictx.close();

    Connection cnx = cf.createConnection();
    Session prodSession = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
    Session consSession = cnx.createSession(true, 0);
    MessageProducer qProducer = prodSession.createProducer(queue);
    MessageProducer tProducer = prodSession.createProducer(topic);
    
    MessageConsumer qConsumer = consSession.createConsumer(queue);
    MessageConsumer tConsumer = consSession.createConsumer(topic);

    cnx.start();

    TextMessage msg = prodSession.createTextMessage();

    // Producing expired messages:
    msg.setText("Expiry test");
    qProducer.send(msg, javax.jms.DeliveryMode.NON_PERSISTENT, 4, 1);
    tProducer.send(msg, javax.jms.DeliveryMode.NON_PERSISTENT, 4, 1);

    qConsumer.receiveNoWait();
    tConsumer.receiveNoWait();

    // Producing "undeliverable" messages: 
    msg.setText("Undeliverability test");
    qProducer.send(msg);
    tProducer.send(msg);
    qConsumer.receive();
    tConsumer.receive();
    consSession.rollback();
    qConsumer.receive();
    tConsumer.receive();
    consSession.rollback();

    cnx.close();
  }
}
