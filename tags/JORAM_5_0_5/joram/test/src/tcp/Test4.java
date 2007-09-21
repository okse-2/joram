/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2006 - 2007 ScalAgent Distributed Technologies
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

package tcp;

import java.io.File;

import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;

import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.tcp.TopicTcpConnectionFactory;

import framework.TestCase;

/**
 * Close a connection (stop the server).
 * Check that onException is called.
 * With and without heart beat.
 * 
 * @author feliot
 *
 *
 */
public class Test4 extends TestCase {

  public static final int MESSAGE_NUMBER = 10;

  public Test4() {
    super();
  }

  private volatile JMSException expectedException;

  public void run() {
    try {
      startAgentServer(
        (short)0, (File)null, 
        new String[]{"-DTransaction=fr.dyade.aaa.util.NTransaction"});

      AdminModule.connect("localhost", 2560,
                          "root", "root", 2);

       org.objectweb.joram.client.jms.admin.User user = org.objectweb.joram.client.jms.admin.User
          .create("anonymous", "anonymous", 0);

      TopicConnectionFactory tcf = TopicTcpConnectionFactory
          .create("localhost", 2560);
      
      org.objectweb.joram.client.jms.Topic topic = org.objectweb.joram.client.jms.Topic
          .create(0);
      topic.setFreeReading();
      topic.setFreeWriting();
      
      doTest(tcf, topic);
      
      new File("./s0/lock").delete();
      startAgentServer(
          (short)0, (File)null, 
          new String[]{"-DTransaction=fr.dyade.aaa.util.NTransaction"});

      ((TopicTcpConnectionFactory)tcf).getParameters().connectingTimer = 4;
      ((TopicTcpConnectionFactory)tcf).getParameters().cnxPendingTimer = 500;
      
      doTest(tcf, topic);

      AdminModule.disconnect();

    } catch (Exception exc) {
      stopAgentServer((short)0);
      error(exc);
    } finally {
      endTest();     
    }
  }

  private void doTest(TopicConnectionFactory tcf, Topic topic) throws Exception {
    expectedException = null;
    
    //  Topic pub/sub
    TopicConnection tc = tcf.createTopicConnection();
    TopicSession ts = tc.createTopicSession(true, 0);
    TopicPublisher tpub = ts.createPublisher(topic);
    TopicSubscriber tsub = ts.createSubscriber(topic);
    tc.start();

    tc.setExceptionListener(new ExceptionListener() {
      public void onException(JMSException exception) {
        System.out.println("onException(" + exception + ')');
        expectedException = exception;
      }
    });
   
    stopAgentServer((short)0);

    // Wait for the connection failure detection
    Thread.sleep(6000);
    
    assertTrue("onException not called with " + tcf, 
        expectedException instanceof javax.jms.JMSException);
    
    tpub.close();
    
    // Currently fails, bug ?
    // tsub.close();
    
    ts.close();
    tc.close(); 


  }

  public static void main(String args[]) {
    new Test4().run();
  }
}
