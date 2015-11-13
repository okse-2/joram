/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C)  2013 ScalAgent Distributed Technologies
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
 * Initial developer(s): ScalAgent D.T.
 * Contributor(s): 
 */
package joram.base;

import java.util.zip.Deflater;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.objectweb.joram.client.jms.FactoryParameters;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AbstractConnectionFactory;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import framework.TestCase;

/**
 * Test : 
 * - set compressedMinSize to 1024 to ConnectionFactory (admin)
 * - The text message received by the consumer is the same that the text
 * message sent by the producer
 * - test is the body is compressed
 * 
 *  Use a Queue
 */
public class Test_Compress_MText extends TestCase {

  public static void main(String[] args) {
    new Test_Compress_MText().run();
  }

  public void run() {
    try {
      System.out.println("server start");
      startAgentServer((short) 0);

      admin();
      System.out.println("admin config ok");

      Context ictx = new InitialContext();
      Queue queue = (Queue) ictx.lookup("queue");
      ConnectionFactory cf = (ConnectionFactory) ictx.lookup("cf");
      ictx.close();

      Connection cnx = cf.createConnection();
      Session sessionp = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      Session sessionc = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      cnx.start();

      String text = generateString(1024);
      
      // create a producer and a consumer
      MessageProducer producer = sessionp.createProducer(queue);
      MessageConsumer consumer = sessionc.createConsumer(queue);
      // create a text message send to the queue by the pruducer
      TextMessage msg = sessionp.createTextMessage();
      assertTrue("Bad compression level", ((org.objectweb.joram.client.jms.TextMessage)msg).getCompressionLevel() == Deflater.BEST_SPEED);
      assertEquals("Bad CompressedMinSize", 1024, ((org.objectweb.joram.client.jms.TextMessage)msg).getCompressedMinSize());
      msg.setText(text);
      producer.send(msg);
      // the consumer receive the message from the queue
      Message msg1 = consumer.receive();
      TextMessage msg2 = (TextMessage) msg1;

      // test messages
      assertEquals(msg.getJMSMessageID(), msg1.getJMSMessageID());
      assertEquals(msg.getJMSType(), msg1.getJMSType());
      assertEquals(msg.getJMSDestination(), msg1.getJMSDestination());
      assertTrue("The message body is not compressed", ((org.objectweb.joram.client.jms.Message) msg1).isCompressed());
      assertEquals(text, msg2.getText());
      
      msg = sessionp.createTextMessage();
      // unset the compression for this message
      ((org.objectweb.joram.client.jms.TextMessage)msg).setCompressedMinSize(0);
      assertTrue("Bad compression level", ((org.objectweb.joram.client.jms.TextMessage)msg).getCompressionLevel() == Deflater.BEST_SPEED);
      assertEquals("Bad CompressedMinSize", 0, ((org.objectweb.joram.client.jms.TextMessage)msg).getCompressedMinSize());

      cnx.close();
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      System.out.println("Server stop ");
      stopAgentServer((short) 0);
      endTest();
    }
  }

  private static String chars = "abcdefghijklmnopqrstuvwxyz";
  private static int charLength = chars.length();
  public static String generateString(int length) {
    StringBuilder  pass = new StringBuilder (charLength);
    for (int x = 0; x < length; x++) {
      int i = (int) (Math.random() * charLength);
      pass.append(chars.charAt(i)).append(" ");
    }
    return pass.toString();
  }

  /**
   * Admin : Create queue and a user anonymous use jndi
   */
  public void admin() throws Exception {
    // conexion
    AdminModule.connect("localhost", 2560, "root", "root", 60);
    // create a Queue
    Queue queue =  Queue.create("queue");

    // create a user
    User user = User.create("anonymous", "anonymous");
    // set permissions
    queue.setFreeReading();
    queue.setFreeWriting();

    ConnectionFactory cf = TcpConnectionFactory.create("localhost", 2560);
    FactoryParameters fp = ((AbstractConnectionFactory) cf).getParameters();
    // set the compressed minimum size
    fp.compressedMinSize = 1024;

    javax.naming.Context jndiCtx = new javax.naming.InitialContext();
    jndiCtx.bind("cf", cf);
    jndiCtx.bind("queue", queue);
    jndiCtx.close();

    AdminModule.disconnect();
  }
}
