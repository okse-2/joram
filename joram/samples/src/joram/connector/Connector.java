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
package connector;

import javax.jms.Queue;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.objectweb.joram.client.connector.ActivationSpecImpl;
import org.objectweb.joram.client.connector.JoramAdapter;
import org.objectweb.joram.client.connector.ManagedConnectionFactoryImpl;
import org.objectweb.joram.client.connector.ManagedConnectionImpl;
import org.objectweb.joram.client.connector.OutboundConnection;
import org.objectweb.joram.client.connector.OutboundConsumer;
import org.objectweb.joram.client.connector.OutboundProducer;
import org.objectweb.joram.client.connector.OutboundSession;

public class Connector {

  public static void main(String[] args) throws Exception {

      JWorkManager jw = new JWorkManager(1,4,5000);
      ResourceBootstrapContext bt=new ResourceBootstrapContext(jw);


      System.out.println("JoramAdapter ...");
      JoramAdapter ja= new JoramAdapter() ;
      
      ja.start(bt);
      System.out.println("start ...");
     

      Context ictx = new InitialContext();
      Queue queue = (Queue) ictx.lookup("sampleQueue");
      Topic topic = (Topic) ictx.lookup("sampleTopic");
     
      ictx.close();

      ManagedConnectionFactoryImpl mcf = new ManagedConnectionFactoryImpl();
      mcf.setResourceAdapter(ja);
      System.out.println("ManagedConnectionFactoryImpl ok");
     
      ManagedConnectionImpl mci = (ManagedConnectionImpl) mcf.createManagedConnection(null,null);
      System.out.println("ManagedConnectionImpl ok");

      OutboundConnection oc = (OutboundConnection) mci.getConnection(null,null);
      System.out.println("OutboundConnection ok");
      
      final OutboundSession os =(OutboundSession) oc.createSession(false,1);
      System.out.println("OutboundSession ok");
     
      final OutboundProducer prod = (OutboundProducer) os.createProducer(queue);
      final OutboundProducer prod1 = (OutboundProducer) os.createProducer(topic);

      System.out.println("OutboundProducer ok");

      OutboundConsumer cons = (OutboundConsumer) os.createConsumer(queue);
      OutboundConsumer cons1 = (OutboundConsumer) os.createConsumer(topic);	
      System.out.println("OutboundConsumer ok");

      oc.start();
      System.out.println();
      System.out.println("Without MessageListener");
      TextMessage msg = os.createTextMessage("avec queue");
      prod.send(msg);
      TextMessage msg1 =(TextMessage)  cons.receive();
      System.out.println("msg receive :"+msg1.getText());
      System.out.println();

      System.out.println("With MessageListener");
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
     
      
      System.out.println("new thread producer on queue");
      new Thread() {
	  public void run() {
	      int i = 0;
	      try {
		  while(i!=100){
                          i++;
                          TextMessage msg = os.createTextMessage("with queue "+i);
                          prod.send(msg);
		  }
	      } catch (Exception exc) {
		  
	      }
	  }
      }.start();
      System.out.println("new thread producer on topic");
      new Thread() {
	  public void run() {
	      int i = 0;
	      try {
		  while(i!=100){
		      i++;
		      TextMessage msg = os.createTextMessage("with topic2 "+i);
		      prod1.send(msg);
		  }
	      } catch (Exception exc) {
		  
	      }
	  }
      }.start();
        
  
      System.out.println("wait receive, press a key to exit");
      System.in.read();
      
      ja.stop();
      System.out.println("stop");
      System.exit(0);

  }

   

}


