/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2005 - 2009 ScalAgent Distributed Technologies
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
 * Initial developer(s): Feliot David  (ScalAgent D.T.)
 * Contributor(s): Badolle Fabien (ScalAgent D.T.)
 */
package joram.client;


import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TopicSubscriber;

import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;
import org.objectweb.joram.client.jms.Topic;

import framework.TestCase;

public class ClientTest16_2 extends TestCase {

  public static void main(String[] args) {
    new ClientTest16_2().run();
  }

  public void run() {
    try {
      AdminModule.connect("localhost", 2560, "root", "root", 20);

      Topic topic = Topic.create(0, "test_topic");

      ConnectionFactory cf = TcpConnectionFactory.create("localhost", 2560);
      ((TcpConnectionFactory) cf).getParameters().cnxPendingTimer = 500;

      Connection connection = cf.createConnection("anonymous", "anonymous");

      Session recSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

      try{
        TopicSubscriber consumer = recSession.createDurableSubscriber(topic, "test_sub");
        assertTrue(consumer!=null);
      }catch(JMSException exc){
        System.out.println("OK -> create fail");
        //if time in build permit reconnection before connectingTimer
        assertTrue(exc instanceof javax.jms.JMSException);
      }

      connection.start();

      Thread.sleep(5000);

      connection.close();

    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      stopAgentServerExt((short)0);
      endTest();
    }
  }
}