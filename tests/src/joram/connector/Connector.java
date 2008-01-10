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

import javax.jms.Destination;
import javax.jms.Queue;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.naming.Context;
import javax.naming.InitialContext;

import joram.framework.TestCase;

import org.objectweb.joram.client.connector.ActivationSpecImpl;
import org.objectweb.joram.client.connector.JoramAdapter;
import org.objectweb.joram.client.connector.ManagedConnectionFactoryImpl;
import org.objectweb.joram.client.connector.ManagedConnectionImpl;
import org.objectweb.joram.client.connector.OutboundConnection;
import org.objectweb.joram.client.connector.OutboundConsumer;
import org.objectweb.joram.client.connector.OutboundProducer;
import org.objectweb.joram.client.connector.OutboundSession;

public class Connector extends TestCase
{
  public static void main(String[] args) throws Exception
  {
     new Connector().run();
  }
      
  public void run(){  
      try{
	  System.out.println("start");
	  startAgentServer((short) 0);
	  JWorkManager jw = new JWorkManager(1,5,5000);
	  ResourceBootstrapContext bt=new ResourceBootstrapContext(jw);
	  	  
	  JoramAdapter ja= new JoramAdapter() ;
	  ja.start(bt);
	  
	  Context ictx = new InitialContext();
	  Queue queue = (Queue) ictx.lookup("sampleQueue");
	  Topic topic = (Topic) ictx.lookup("sampleTopic");
	  ictx.close();
	  
	  ManagedConnectionFactoryImpl mcf = new ManagedConnectionFactoryImpl();
	  mcf.setResourceAdapter(ja);
	  	  
	  ManagedConnectionImpl mci = (ManagedConnectionImpl) mcf.createManagedConnection(null,null);
	  
	  OutboundConnection oc = (OutboundConnection) mci.getConnection(null,null);
	  
	  final OutboundSession os =(OutboundSession) oc.createSession(false,0);
	  
	  final OutboundProducer prod = (OutboundProducer) os.createProducer((Destination)queue);
	  final OutboundProducer prod1 = (OutboundProducer) os.createProducer((Destination)topic);
	  final OutboundProducer prod2 = (OutboundProducer) os.createProducer((Destination)topic);
	  
	  
	  OutboundConsumer cons = (OutboundConsumer) os.createConsumer((Destination)queue);
	  OutboundConsumer cons1 = (OutboundConsumer) os.createConsumer((Destination)topic);	
	  
	  
	  oc.start();
	
	  TextMessage msg = os.createTextMessage("with queue");
	  prod.send(msg);
	  TextMessage msg1 =(TextMessage)  cons.receive();
	  assertEquals("with queue", msg1.getText());
	 
	  msg = os.createTextMessage("with topic");
	  prod1.send(msg);
	  
	  msg1 =(TextMessage)  cons1.receive();
	  assertEquals("with topic", msg1.getText());
	 
	  MessagePointFactory  mep = new MessagePointFactory();
	  ActivationSpecImpl spec = new ActivationSpecImpl();
	  spec.setResourceAdapter(ja);
	  spec.setDestinationType("javax.jms.Queue");
	  spec.setDestination("sampleQueue");
	  
	  MessagePointFactory mep2 = new MessagePointFactory();
	  ActivationSpecImpl spec2 = new ActivationSpecImpl();
	  spec2.setResourceAdapter(ja);
	  spec2.setDestinationType("javax.jms.Topic");
	  spec2.setDestination("sampleTopic");
	  
	  
	  ja.endpointActivation(mep , spec); // listener on queue
	  ja.endpointActivation(mep2 , spec2);// listener on topic
	 
	 	  
	  msg = os.createTextMessage("with queue");
	  prod.send(msg);
	  	 
	  msg = os.createTextMessage("with topic");
	   prod2.send(msg);

	   new Thread() {
	       public void run() {
		   int i = 0;
		   try {
		       Thread.sleep(200);
		       while(i!=100){
			   i++;
			   TextMessage msg = os.createTextMessage("with queue "+i);
			   prod.send(msg);
		       }
		   } catch (Exception exc) {
		       
		   }
	       }
	   }.start();
	   new Thread() {
	       public void run() {
		  int i = 0;
		  try {
		      while(i!=100){
			  i++;
			  TextMessage msg = os.createTextMessage("with topic2 "+i);
			  prod2.send(msg);
		      }
		  } catch (Exception exc) {
		      
		  }
	       }
	   }.start();
	  
	  
	  Thread.sleep(30000);// wait onMessage
	  ja.stop();
      }catch(Throwable exc){
	  exc.printStackTrace();
	  error(exc);
      }finally{
	 
	  stopAgentServerExt((short)0);
	  endTest();
      }

  }

   

}


