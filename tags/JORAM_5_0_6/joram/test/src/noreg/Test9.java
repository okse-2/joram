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
package noreg;

import java.lang.reflect.Method;

import javax.jms.*;

import fr.dyade.aaa.agent.AgentServer;

import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.Destination;

/**
 * check message ID and system property
 *
 */
public class Test9 extends BaseTest {
  static int NbRound = 100;
  static ConnectionFactory cf = null;

  public static void main (String args[]) throws Exception {
      new Test9().run();
  }
  public void run(){
      try{
	  System.out.println("server start");
	  startServer();
	  String baseclass = "noreg.ColocatedBaseTest";
	  baseclass = System.getProperty("BaseClass", baseclass);
	  
	  NbRound = Integer.getInteger("NbRound", NbRound).intValue();
	  
	  AdminConnect(baseclass);
	  
	  Destination dest = createDestination("org.objectweb.joram.client.jms.Queue");
	  dest.setFreeReading();
	  dest.setFreeWriting();
	  
	  User user = User.create("anonymous", "anonymous", 0);
      
	  org.objectweb.joram.client.jms.admin.AdminModule.disconnect();
      
	  ConnectionFactory cf =  createConnectionFactory(baseclass);
	  Connection cnx = cf.createConnection();
	  ConnectionMetaData cnxmd = cnx.getMetaData();
      
	  
	  //System.out.println("Provider: " + cnxmd.getJMSProviderName() + cnxmd.getProviderVersion());
	  assertEquals("Joram",cnxmd.getJMSProviderName());

	  // System.out.println("Transaction: " + System.getProperty("Transaction"));
	  //assertEquals("fr.dyade.aaa.util.NTransaction",System.getProperty("Transaction"));
	 
	  // System.out.println("Engine: " + System.getProperty("Engine"));
	  assertEquals("fr.dyade.aaa.agent.GCEngine",System.getProperty("Engine"));
	  
	  //System.out.println("baseclass: " + baseclass);
	  assertEquals("noreg.ColocatedBaseTest",baseclass);
	  
	  //System.out.println("NbRound=" + NbRound);
	  assertEquals(10,NbRound);
	  
	  Session sess1 = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
	  Session sess2 = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
	  MessageConsumer consumer = sess1.createConsumer(dest);
	  MessageProducer producer = sess2.createProducer(dest);
	  
	  cnx.start();
	  
	  Message msg = sess2.createMessage();
	  for (int i=0; i<NbRound; i++) {
	      producer.send(msg);
	  msg = consumer.receive();
	  // System.out.println(msg.getJMSMessageID());
	  assertEquals("ID:0.0.1027c0m"+(i+1),msg.getJMSMessageID());
	  }
	  //System.out.println("Test OK");
      }catch(Throwable exc){
	  exc.printStackTrace();
	  error(exc);
      }finally{
	  System.out.println("server stop");
	  fr.dyade.aaa.agent.AgentServer.stop();
	  endTest(); 
	  
      }
  }
}
