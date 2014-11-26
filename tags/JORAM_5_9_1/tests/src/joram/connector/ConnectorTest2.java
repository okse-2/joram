/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2007 ScalAgent Distributed Technologies
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
 * Initial developer(s): BADOLLE Fabien ( ScalAgent Distributed Technologies )
 * Contributor(s):
 */
package joram.connector;

import javax.jms.Connection;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;


import org.objectweb.joram.client.connector.JoramAdapter;
import org.objectweb.joram.client.jms.ConnectionFactory;

import framework.TestCase;

/**
 * JCA Connector test with a colocated Joram server.
 */
public class ConnectorTest2 extends TestCase {
  public static void main(String[] args) throws Exception {
    new ConnectorTest2().run();
  }

  public void run(){
    try{      
    	JoramAdapter ja= new JoramAdapter() ;
    	ja.setCollocated(Boolean.TRUE);
    	ja.setName("ra");
    	ja.setStartJoramServer(true);
    	ja.setStorage("s0");
    	ja.setServerId((short) 0);
    	ja.setPlatformConfigDir(".");
    	ja.setAdminFileXML("joramAdmin.xml");
    	ja.setAdminFileExportXML("joramAdminExport.xml");
    	ja.start(new ResourceBootstrapContext(new JWorkManager(1, 5, 5000)));
      
      Thread.sleep(5000);
      
      Context ictx = new InitialContext();
      Queue queue = (Queue) ictx.lookup("sampleQueue");
      assertTrue("queue not found", queue != null);
      ConnectionFactory cf = (ConnectionFactory) ictx.lookup("LCF");
      ictx.close();

      // ConnectionFactory cf = LocalConnectionFactory.create();
      
      Connection cnx = cf.createConnection("anonymous", "anonymous");
      Session session = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      // create a producer and a consumer
      MessageProducer producer = session.createProducer(queue);
      MessageConsumer consumer = session.createConsumer(queue);
      cnx.start();

      // create a message sent to the queue by the producer 
      TextMessage msg = session.createTextMessage("Message de Test");
      producer.send(msg);

      // the consumer receive the message from the queue
      TextMessage msg1 = (TextMessage) consumer.receive();
      assertTrue("Received bad message", msg.getJMSMessageID().equals(msg1.getJMSMessageID()));
      
      cnx.close();

      ja.stop();
    } catch(Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      endTest();
    }
  }
}


