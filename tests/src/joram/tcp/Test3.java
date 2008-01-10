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

package joram.tcp;

import java.io.File;

import javax.jms.IllegalStateException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.TextMessage;

import joram.framework.TestCase;

import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.tcp.QueueTcpConnectionFactory;


/**
 * Check that a receive on a closed connection
 * (closed by a server stop) raises
 * an IllegalStateException.
 * 
 * Test with and without "hear beat" timer.
 * 
 * @author feliot
 *
 */
public class Test3 extends TestCase {

  public static final int MESSAGE_NUMBER = 10;

  public Test3() {
    super();
  }

  public void run() {
    try {
      startAgentServer(
        (short)0, (File)null, 
        new String[]{"-DTransaction=fr.dyade.aaa.util.NTransaction"});
      
      Thread.sleep(1000);

      AdminModule.connect("localhost", 2560,
                          "root", "root", 10);

      org.objectweb.joram.client.jms.admin.User user = 
        org.objectweb.joram.client.jms.admin.User.create("anonymous", "anonymous", 0);

      QueueConnectionFactory qcf =
        QueueTcpConnectionFactory.create("localhost", 2560);

      org.objectweb.joram.client.jms.Queue queue = 
        org.objectweb.joram.client.jms.Queue.create(0);
      queue.setFreeReading();
      queue.setFreeWriting();
      
      doTest(qcf, queue);
      
      startAgentServer((short) 0, (File) null,
          new String[] { "-DTransaction=fr.dyade.aaa.util.NTransaction" });

      Thread.sleep(2000);

      ((QueueTcpConnectionFactory)qcf).getParameters().connectingTimer = 5;
      ((QueueTcpConnectionFactory)qcf).getParameters().cnxPendingTimer = 500;
      
      doTest(qcf, queue);

    } catch (Exception exc) {
      stopAgentServer((short)0);
      error(exc);
    } finally {
      endTest();     
    }
  }
  
  private void doTest(QueueConnectionFactory qcf, 
      Queue queue) throws Exception {
    QueueConnection qc = qcf.createQueueConnection();
    QueueSession qs = qc.createQueueSession(true, 0);
    QueueSender qsend = qs.createSender(queue);
    QueueReceiver qrec = qs.createReceiver(queue);
    TextMessage msg = qs.createTextMessage();
    qc.start();

    for (int i = 0; i < MESSAGE_NUMBER; i++) {
      msg.setText("Test number " + i);
      qsend.send(msg);
    }      
    qs.commit();

    stopAgentServer((short)0);

    // Avoids a bug: synchro between connection
    // error and further receive.
    Thread.sleep(10000);

    IllegalStateException ise = null;
    try {
      msg = (TextMessage)qrec.receive();
    } catch (IllegalStateException exc) {
      ise = exc;
    }
    
    assertTrue("Expected IllegalStateException not thrown", 
        ise != null);
  }

  public static void main(String args[]) {
    new Test3().run();
  }
}
