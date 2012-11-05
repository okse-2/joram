/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - 2007 ScalAgent Distributed Technologies
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
 * TEST : creation of durable subscriber. If already active during cretion there is an exception 
 *
 */
public class Test1 extends framework.TestCase {
  static Topic topic = null;
  static ConnectionFactory cf = null;

  static String host = "localhost";
  static int port = 2560;

  public Test1() {
    super();
  }

  public static void main(String[] args) throws Exception {
    new Test1().run(args);
  }

  protected void AdminConnect() throws Exception {
    AdminModule.connect(host, port, "root", "root", 60);
  }

  protected ConnectionFactory createConnectionFactory() throws Exception {
    return TcpConnectionFactory.create(host, port);
  }

  protected void startServer() throws Exception {
    AgentServer.init((short) 0, "./s0", null);
    AgentServer.start();

    Thread.sleep(1000L);
  }

  protected void stopServer() throws Exception {
  }

  public void run(String[] args) throws Exception {
      try{
      
	  if (! Boolean.getBoolean("ServerOutside"))
	      startServer();
	  
	  host = System.getProperty("hostname", host);
	  port = Integer.getInteger("port", port).intValue();
	  
	  AdminConnect();
	  
	  topic = Topic.create();
	  cf =  createConnectionFactory();
	  
	  User user = User.create("anonymous", "anonymous", 0);
	  topic.setFreeReading();
	  topic.setFreeWriting();
	  
	  org.objectweb.joram.client.jms.admin.AdminModule.disconnect();
	  
	  Connection cnx = cf.createConnection();
	  Session sess1 = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
	  MessageConsumer cons1= sess1.createDurableSubscriber(topic, "dursub1");
	  
	  try {
	      Session sess2 = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
	      MessageConsumer cons2 = sess2.createDurableSubscriber(topic, "dursub1");
	  } catch (JMSException exc) {
	      //System.out.println(" The durable subscription has already been activated");
	      assertTrue(exc instanceof  javax.jms.JMSException);
	      // exc.printStackTrace();
	  }
	  
	  sess1.close();
	  
	  try {
	      Session sess2 = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
	      MessageConsumer cons2 = sess2.createDurableSubscriber(topic, "dursub1");
	  } catch (JMSException exc) {
	      // exc.printStackTrace();
	  }
	  
	  cnx.close();
	  
	  cnx = cf.createConnection();
	  try {
	      Session sess3 = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
	      MessageConsumer cons3 = sess3.createDurableSubscriber(topic, "dursub1");
	  } catch (JMSException exc) {
	     exc.printStackTrace();
	  }
      }catch(Throwable exc){
	  exc.printStackTrace();
	  error(exc);
      }finally{
	  AgentServer.stop();
	  endTest();
      }
  }
}
