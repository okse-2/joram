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
package classic;

import javax.jms.*;
import javax.naming.*;

/**
 * Browses the queue.
 */
public class Browser
{
  static Context ictx = null; 

  public static void main(String[] args) throws Exception
  {
    System.out.println();
    System.out.println("Browses the queue: ");

    ictx = new InitialContext();
    Queue queue = (Queue) ictx.lookup("queue");
    QueueConnectionFactory qcf = (QueueConnectionFactory) ictx.lookup("qcf");
    ictx.close();

    QueueConnection qc = qcf.createQueueConnection();
    QueueSession qs = qc.createQueueSession(true, 0);
    QueueBrowser browser = qs.createBrowser(queue);

    java.util.Enumeration messages = browser.getEnumeration();

    Message msg;

    while (messages.hasMoreElements()) {
      msg = (Message) messages.nextElement();

      if (msg instanceof TextMessage)
        System.out.println(((TextMessage) msg).getText());
    }

    System.out.println();
    System.out.println("Queue browsed.");

    qc.close();
  }
}
