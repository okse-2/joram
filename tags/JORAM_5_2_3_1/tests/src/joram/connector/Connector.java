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


import org.objectweb.joram.client.connector.ActivationSpecImpl;
import org.objectweb.joram.client.connector.JoramAdapter;
import org.objectweb.joram.client.connector.ManagedConnectionFactoryImpl;
import org.objectweb.joram.client.connector.ManagedConnectionImpl;
import org.objectweb.joram.client.connector.OutboundConnection;
import org.objectweb.joram.client.connector.OutboundConsumer;
import org.objectweb.joram.client.connector.OutboundProducer;
import org.objectweb.joram.client.connector.OutboundSession;

import framework.TestCase;

public class Connector extends TestCase {
  public static void main(String[] args) throws Exception {
    new Connector().run();
  }

  public void run(){  
    try{
      System.out.println("start");
      startAgentServer((short) 0);
      JWorkManager jw = new JWorkManager(1, 5, 5000);
      ResourceBootstrapContext bt=new ResourceBootstrapContext(jw);

      JoramAdapter ja= new JoramAdapter() ;
      ja.start(bt);

      Thread.sleep(10000);
      Context ictx = new InitialContext();
      Queue queue = (Queue) ictx.lookup("sampleQueue");
      assertTrue("queue not found", queue != null);
      Topic topic = (Topic) ictx.lookup("sampleTopic");
      assertTrue("topic not found", topic != null);
      Queue anotherQueue = (Queue) ictx.lookup("anotherQueue");
      assertTrue("anotherQueue not found", anotherQueue != null);
      ictx.close();

      ManagedConnectionFactoryImpl mcf = new ManagedConnectionFactoryImpl();
      mcf.setResourceAdapter(ja);

      ManagedConnectionImpl mci = (ManagedConnectionImpl) mcf.createManagedConnection(null,null);

      OutboundConnection oc = (OutboundConnection) mci.getConnection(null,null);

      final OutboundSession os =(OutboundSession) oc.createSession(false,0);

      final OutboundProducer prod = (OutboundProducer) os.createProducer((Destination)queue);
      final OutboundProducer prod1 = (OutboundProducer) os.createProducer((Destination)topic);
      final OutboundProducer prod2 = (OutboundProducer) os.createProducer((Destination)topic);
      final OutboundProducer prod3 = (OutboundProducer) os.createProducer((Destination)anotherQueue);

      OutboundConsumer cons = (OutboundConsumer) os.createConsumer((Destination)queue);
      OutboundConsumer cons1 = (OutboundConsumer) os.createConsumer((Destination)topic);	
      OutboundConsumer cons3 = (OutboundConsumer) os.createConsumer((Destination)anotherQueue);  

      oc.start();

      TextMessage msg = os.createTextMessage("with queue");
      prod.send(msg);
      TextMessage msg1 =(TextMessage)  cons.receive();
      assertEquals("with queue", msg1.getText());

      assertTrue("queue is not empty", ((org.objectweb.joram.client.jms.Queue) queue).getPendingMessages() == 0);

      msg = os.createTextMessage("with topic");
      prod1.send(msg);
      msg1 =(TextMessage) cons1.receive();
      assertEquals("with topic", msg1.getText());

      msg = os.createTextMessage("with anotherQueue");
      prod3.send(msg);
      msg1 =(TextMessage) cons3.receive();
      assertEquals("with anotherQueue", msg1.getText());

      assertTrue("anotherQueue is not empty", ((org.objectweb.joram.client.jms.Queue) anotherQueue).getPendingMessages() == 0);

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

      MessagePointFactory mep3 = new MessagePointFactory();
      ActivationSpecImpl spec3 = new ActivationSpecImpl();
      spec3.setResourceAdapter(ja);
      spec3.setDestinationType("javax.jms.Queue");
      spec3.setDestination("anotherQueue");
              
      ja.endpointActivation(mep , spec);    // listener on queue
      ja.endpointActivation(mep2 , spec2);  // listener on topic
      ja.endpointActivation(mep3 , spec3);  // listener on other queue

      msg = os.createTextMessage("with queue");
      prod.send(msg);
      
      msg = os.createTextMessage("with topic");
      prod2.send(msg);

      msg = os.createTextMessage("with anotherQueue");
      prod3.send(msg);

      Thread.sleep(5000);  // wait onMessage
      assertTrue("counter1=" + counter1, counter1 == 1);
      assertTrue("counter2=" + counter2, counter2 == 1);
      assertTrue("counter3=" + counter3, counter3 == 1);
      
      new Thread() {
        public void run() {
          int i = 0;
          try {
            Thread.sleep(50);
            while(i!=100){
              TextMessage msg = os.createTextMessage("with queue " + i++);
              prod.send(msg);
            }
          } catch (Exception exc) {}
        }
      }.start();
      
      new Thread() {
        public void run() {
          int i = 0;
          try {
            while(i!=100){
              TextMessage msg = os.createTextMessage("with topic " + i++);
              prod2.send(msg);
            }
          } catch (Exception exc) {}
        }
      }.start();
      
      new Thread() {
        public void run() {
          int i = 0;
          try {
            while(i!=100){
              TextMessage msg = os.createTextMessage("with anotherQueue " + i++);
              prod3.send(msg);
            }
          } catch (Exception exc) {}
        }
      }.start();

      Thread.sleep(10000);  // wait onMessage
      assertTrue("counter1=" + counter1, counter1 == 101);
      assertTrue("counter2=" + counter2, counter2 == 101);
      assertTrue("counter3=" + counter3, counter3 == 101 );
      
      ja.stop();
    }catch(Throwable exc){
      exc.printStackTrace();
      error(exc);
    }finally{
      stopAgentServerExt((short)0);
      endTest();
    }
  }
  
  private static int counter1 = 0;
  private static int counter2 = 0;
  private static int counter3 = 0;
  
  public static void countMessages(String text) {
    if (text == null) return;
    
    if (text.startsWith("with queue")) {
      counter1 += 1;
    } else if (text.startsWith("with topic")) {
      counter2 += 1;
    } else if (text.startsWith("with anotherQueue")) {
      counter3 += 1;
    }
  }
}


