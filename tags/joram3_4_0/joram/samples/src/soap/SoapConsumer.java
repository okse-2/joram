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

import javax.jms.*;
import javax.naming.*;

/**
 */
public class SoapConsumer
{
  static Context ictx = null; 

  public static void main(String[] args) throws Exception
  {
    System.out.println();
    System.out.println("Consumes messages on the queue and on the topic...");

    ictx = new InitialContext();
    ConnectionFactory cf = (ConnectionFactory) ictx.lookup("soapCf");
    Queue queue = (Queue) ictx.lookup("queue");
    Topic topic = (Topic) ictx.lookup("topic");
    ictx.close();

    Connection cnx = cf.createConnection();
    Session sess = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);

    MessageConsumer qConsumer = sess.createConsumer(queue);
    MessageConsumer tConsumer = sess.createConsumer(topic);

    tConsumer.setMessageListener(new MsgListener());

    cnx.start();

    TextMessage msg;

    for (int i = 0; i < 10; i++) {
      msg = (TextMessage) qConsumer.receive();
      System.out.println("Message received from queue: " + msg.getText());
    }

    System.in.read();
    cnx.close();
  }
}

class MsgListener implements MessageListener
{
  public void onMessage(Message msg)
  {
    try {
      if (msg instanceof TextMessage)
        System.out.println("Message received from topic: " 
                           +((TextMessage) msg).getText());
    }
    catch (Exception exc) {
      System.out.println("Exception in listener: " + exc);
    }
  }
}
