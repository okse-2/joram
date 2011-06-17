/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C)  2007 - 2009 ScalAgent Distributed Technologies
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
package joram.base;

import java.util.List;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.Session;
import javax.jms.TemporaryTopic;
import javax.jms.Topic;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.objectweb.joram.client.jms.admin.AdminModule;

import framework.TestCase;




/**
 *  Test delete of Temporary Topic
 *    
 */
public class Test_Temp_T2 extends TestCase   {

  static Topic adminTopic= null;
  public static void main(String[] args) {
    new Test_Temp_T2().run();
  }

  public void run() {
    try {
      System.out.println("server start");
      startAgentServer((short)0);


      admin();
      System.out.println("admin config ok");

      Context  ictx = new InitialContext();
      ConnectionFactory cf = (ConnectionFactory) ictx.lookup("cf");
      ictx.close();

      Connection cnx = cf.createConnection();

      org.objectweb.joram.client.jms.admin.AdminModule.connect("localhost", 2560, "root", "root", 60);


      Destination[] destinations = AdminModule.getDestinations();
      TemporaryTopic topic = (TemporaryTopic) destinations[0];
      adminTopic=topic;


      // temporary topic is creating by a session 

      cnx.start();
      Session sessionp = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);   
      TemporaryTopic temptopic = sessionp.createTemporaryTopic();
      cnx.close();
      testTempTopic();

      cnx = cf.createConnection();
      cnx.start();
      sessionp = cnx.createSession(false,
                                   Session.AUTO_ACKNOWLEDGE);   
      temptopic = sessionp.createTemporaryTopic();
      TemporaryTopic temptopic1 = sessionp.createTemporaryTopic();
      TemporaryTopic temptopic2 = sessionp.createTemporaryTopic();
      TemporaryTopic temptopic3 = sessionp.createTemporaryTopic();
      TemporaryTopic temptopic4 = sessionp.createTemporaryTopic();
      cnx.close();
      testTempTopic();



    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      System.out.println("Server stop ");
      stopAgentServer((short)0);
      endTest(); 
    }
  }

  /**
   * Admin : Create a user anonymous
   *   use jndi
   */
  public void admin() throws Exception {
    // conexion 
    org.objectweb.joram.client.jms.admin.AdminModule.connect("localhost", 2560, "root", "root", 60);

    // create a user
    org.objectweb.joram.client.jms.admin.User user =
      org.objectweb.joram.client.jms.admin.User.create("anonymous", "anonymous");

    javax.jms.ConnectionFactory cf =
      org.objectweb.joram.client.jms.tcp.TcpConnectionFactory.create("localhost", 2560);


    javax.naming.Context jndiCtx = new javax.naming.InitialContext();
    jndiCtx.bind("cf", cf);
    jndiCtx.close();


    org.objectweb.joram.client.jms.admin.AdminModule.disconnect();
  }

  private static void  testTempTopic() throws Exception{
    int j=0;
    Destination[] destinations = AdminModule.getDestinations();
    for (Destination destination : destinations) {
      j++;
      TemporaryTopic topic = (TemporaryTopic)destination;
      assertEquals(adminTopic,topic);
    }
    assertEquals(1,j);

  }


}

