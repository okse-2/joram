/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - 2007 ScalAgent Distributed Technologies
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
 * Initial developer(s): Freyssinet Andre (ScalAgent D.T.)
 * Contributor(s): Badolle Fabien (ScalAgent D.T.)
 */
package joram.noreg;

import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;

/**
 *
 */
public class Test4 extends BaseTest {
  static String host = "localhost";
  static int port = 16010;

  static org.objectweb.joram.client.jms.Queue queue = null;
  static QueueConnectionFactory qcf = null;

  public static void main(String[] args) throws Exception {
    new Test4().run(args);
  }

  public void run(String[] args) throws Exception {
      try{
    if (! Boolean.getBoolean("ServerOutside"))
      startServer();

    AdminModule.connect(host, port, "root", "root", 60);

    qcf = (QueueConnectionFactory) org.objectweb.joram.client.jms.tcp.QueueTcpConnectionFactory.create(host, port);
    queue = org.objectweb.joram.client.jms.Queue.create(0);
    User user = User.create("anonymous", "anonymous", 0);
    queue.setFreeReading();
    queue.setFreeWriting();
    org.objectweb.joram.client.jms.admin.AdminModule.disconnect();

    QueueConnection cnx1 = qcf.createQueueConnection();
    QueueSession sess1 = cnx1.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
    MessageProducer prod = sess1.createProducer(queue);
    cnx1.start();

    QueueConnection cnx2 = qcf.createQueueConnection();
    QueueSession sess2 = cnx2.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
    MessageConsumer cons = sess2.createConsumer(queue);
    cons.setMessageListener(new MsgListener());
    cnx2.start();

    for (int i=0; i<50; i++) {
      QueueConnection cnx3 = qcf.createQueueConnection();
      QueueSession sess3 = cnx3.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
      QueueBrowser browser = sess3.createBrowser((Queue) queue);
      cnx3.start();
      
      if (browser.getEnumeration().hasMoreElements()) {
	  //System.out.println("Heart-Beat is not already consumed");
      } else {
	  // System.out.println("Send new Heart-Beat.");
        TextMessage msg = sess1.createTextMessage("hello world:" + i);
        prod.send(msg);
      }
      cnx3.close();

      Thread.sleep(2000L);
    }
      }catch(Throwable exc){
	  exc.printStackTrace();
	  error(exc);
      }finally{
	  fr.dyade.aaa.agent.AgentServer.stop();
	  endTest();
      }
   
  }

  /**
   * Implements the <code>javax.jms.MessageListener</code> interface.
   */
  static class MsgListener implements MessageListener {
    public synchronized void onMessage(Message msg) {
      try {
	  if (msg instanceof TextMessage){
	      // System.out.println(((TextMessage) msg).getText());
	      Test4.assertTrue(((TextMessage) msg).getText().startsWith("hello world:"));
        }else
          System.out.println("unknow message");
     } catch (Exception exc) {
        exc.printStackTrace();
      }
    }
  }

}
