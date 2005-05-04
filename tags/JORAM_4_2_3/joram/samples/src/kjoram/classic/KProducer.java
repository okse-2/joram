/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
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
 * Initial developer(s): Nicolas Tachker (ScalAgent)
 * Contributor(s):
 */
import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.io.*;

import com.scalagent.kjoram.*;
import com.scalagent.kjndi.ksoap.*;

/**
 * 
 */
public class KProducer extends MIDlet {
  
  protected void startApp() throws MIDletStateChangeException {
    System.out.println();
    System.out.println("Produces messages on the queue and on the topic...");

    try {
      SoapNamingContext ictx = new SoapNamingContext("X.X.X.X",8080);
      ConnectionFactory cf = 
        new com.scalagent.kjoram.ksoap.SoapConnectionFactory(
          "X.X.X.X", 8080, 360000);
      Queue queue = (Queue) ictx.lookup("queue");
      Topic topic = (Topic) ictx.lookup("topic");
      
      com.scalagent.kjoram.Connection cnx = cf.createConnection();
      //Session sess = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      Session sess = cnx.createSession(true, 0);
      
      MessageProducer producer = sess.createProducer(null);
      
      TextMessage msg = sess.createTextMessage();
      
      for (int i = 1; i < 2; i++) {
        msg.setText("kJORAM test " + i);
        producer.send(queue, msg);
        producer.send(topic, msg);
      }
      
      sess.commit();
      cnx.close();
    } catch (Exception exc) {
      System.out.println("////////// EXCEPTION " + exc);
    }
  }

  protected void pauseApp() {
  }

  protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
  }
}
