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

import javax.jms.*;
import javax.naming.*;

public class Sender 
{
  static Context ictx = null;

  public static void main(String argv[]) throws Exception
  {
    System.out.println();
    System.out.println("Sends messages on the queue...");

    ictx = new InitialContext();
    QueueConnectionFactory cnxF = (QueueConnectionFactory) ictx.lookup("qcf0");
    Queue dest = (Queue) ictx.lookup("queue");
    ictx.close();
    
    QueueConnection cnx = cnxF.createQueueConnection();
    QueueSession session = cnx.createQueueSession(true, 0);
    
    QueueSender sender = session.createSender(dest);
    
    TextMessage message = session.createTextMessage();
  
    int i;
    for (i = 0; i < 10; i ++) {
      message.setText("Message " + i);
      sender.send(message);
    }
    
    session.commit();

    System.out.println(i + " messages sent.");

    cnx.close();
  }
}