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
    Session qSess = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
    Session tSess = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);

    MessageConsumer qConsumer = qSess.createConsumer(queue);
    MessageConsumer tConsumer = tSess.createConsumer(topic);

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
