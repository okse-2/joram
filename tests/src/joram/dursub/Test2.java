/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - 2013 ScalAgent Distributed Technologies
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
package joram.dursub;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import fr.dyade.aaa.agent.AgentServer;

/**
 * Test durable subscription:
 * - Verify that unsubscribe with a null name throws an exception.
 * - Verify that unsubscribe to an active subscription throws an exception.
 * - Closes the session and verify that unsubscribe is ok.
 */
public class Test2 extends framework.TestCase {
  public static void main (String args[]) {
    new Test2().run();
  }
  
  public void run(){
    try {
      startAgentServer((short)0);
      Thread.sleep(1000L);

      ConnectionFactory cf =  TcpConnectionFactory.create("localhost", 2560);
      ((TcpConnectionFactory) cf).getParameters().connectingTimer = 10;
      ((TcpConnectionFactory) cf).getParameters().clientID = "Test2";
      AdminModule.connect(cf, "root", "root");

      User.create("anonymous", "anonymous", 0);
      
      Topic topic = Topic.create();
      topic.setFreeReading();
      topic.setFreeWriting();

      AdminModule.disconnect();

      Connection cnx1 = cf.createConnection();
      cnx1.start();
      
      Session sess1 = cnx1.createSession(false, Session.AUTO_ACKNOWLEDGE);
      JMSException exc = null;
      try{
        sess1.unsubscribe(null);
      }catch(JMSException e){
        exc = e;
      }
      assertTrue(exc != null);
      sess1.close();
      
      sess1 = cnx1.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageConsumer cons = sess1.createDurableSubscriber(topic, "dursub1");
      exc = null;
      try{
        sess1.unsubscribe("dursub1");
      }catch(JMSException e){
        exc = e;
      }
      assertTrue(exc != null);
      cons.close();

      sess1.unsubscribe("dursub1");
      
      cnx1.close();
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally{
      stopAgentServer((short)0);
      endTest(); 
    }
  }
}
