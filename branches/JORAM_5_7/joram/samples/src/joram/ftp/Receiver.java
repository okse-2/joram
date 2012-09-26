/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - 2008 ScalAgent Distributed Technologies
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
 * Initial developer(s): ScalAgent Distributed Technologies
 * Contributor(s):
 */
package ftp;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;

/**
 * Requests to receive messages from the ftp queue.
 */
public class Receiver {
  static Context ictx = null; 

  public static void main(String[] args) throws Exception {
    System.out.println();
    System.out.println("Requests to receive messages...");

    ictx = new InitialContext();
    Queue queue = (Queue) ictx.lookup("ftpQueue");
    ConnectionFactory cf = (ConnectionFactory) ictx.lookup("cf");
    ictx.close();

    Connection cnx = cf.createConnection();
    Session sess = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
    MessageConsumer recv = sess.createConsumer(queue);
    Message msg;

    cnx.start();

    msg = recv.receive();
      
    String u = msg.getStringProperty("url");
    System.out.println("url = " + u);
    System.out.println("crc=" + msg.getLongProperty("crc"));
    System.out.println("ack=" + msg.getBooleanProperty("ack"));
    
    long diff = System.currentTimeMillis() - msg.getLongProperty("date");
    System.out.println("time = " + diff);
    
    System.out.println();
    System.out.println("messages received.");

    cnx.close();
  }
}
