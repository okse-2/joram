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

import java.io.File;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;

/**
 * Sends messages on the ftp queue.
 */
public class Producer {
  static Context ictx = null;

  public static void main(String[] args) throws Exception {
    System.out.println();
    System.out.println("Send message on the ftp queue...");

    if (args.length < 2) {
      System.out.println("Bad param : Producer <host> <file>");
      System.out.println("or        : Producer <host> <file> <user> <pass>");
      System.exit(1);
    }

    String host = args[0];
    String file = args[1];
    String user = null;
    String pass = null;
    if (args.length < 2) {
      user = args[2];
      pass = args[3];
    }

    ictx = new InitialContext();
    Queue queue = (Queue) ictx.lookup("ftpQueue");
    ConnectionFactory cf = (ConnectionFactory) ictx.lookup("cf");
    ictx.close();

    Connection cnx = cf.createConnection();
    Session sess = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
    MessageProducer producer = sess.createProducer(null);
    TextMessage msg = sess.createTextMessage();

    msg.setText("Test transfer " + file);
    if (user != null && pass != null)
      msg.setStringProperty("url", "ftp://" + user + ':' + pass + '@' + host + '/' + file + ";type=i");
    else
      msg.setStringProperty("url", "ftp://" + host + '/' + file + ";type=i");
    msg.setLongProperty("crc", new File(".", file).length());
    msg.setBooleanProperty("ack", false);

    msg.setLongProperty("date", System.currentTimeMillis());

    producer.send(queue, msg);

    System.out.println("message send");

    cnx.close();
  }
}
