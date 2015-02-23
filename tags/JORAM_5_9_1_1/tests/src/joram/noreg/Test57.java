/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 ScalAgent Distributed Technologies
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
package joram.noreg;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;

import framework.TestCase;


/**
 * Test a server restart with lot of big messages.
 */
public class Test57 extends BaseTest {
  static int MsgSize = 1*1024*1024;
  static int NbMsg = 100;

  static Destination dest = null;
  static ConnectionFactory cf = null;

  static String host = "localhost";
  static int port = 16010;

  public static void main(String[] args) throws Exception {
    new Test57().run(args);
  }

  public void run(String[] args) {
    try{
      System.out.println("server start");
      TestCase.startAgentServer((short)0);
      Thread.sleep(2000);

      writeIntoFile("===================== start test 57 =====================");
      MsgSize = Integer.getInteger("MsgSize", MsgSize/1024).intValue() *1024;
      NbMsg = Integer.getInteger("NbMsg", NbMsg).intValue();
      String destc = System.getProperty("Destination",
                                        "org.objectweb.joram.client.jms.Queue");

      writeIntoFile("----------------------------------------------------");
      writeIntoFile("Destination: " + destc);
      writeIntoFile("MsgSize: " + MsgSize);
      writeIntoFile("NbMsg: " + NbMsg);
      writeIntoFile("----------------------------------------------------");

      cf = TcpBaseTest.createConnectionFactory();
      AdminModule.connect(cf);
      dest = createDestination(destc);
      User.create("anonymous", "anonymous", 0);
      dest.setFreeReading();
      dest.setFreeWriting();
      org.objectweb.joram.client.jms.admin.AdminModule.disconnect();

      Connection cnx = cf.createConnection();
      Session sess = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageProducer prod = sess.createProducer(dest);
      cnx.start();

      byte[] content = new byte[MsgSize];
      for (int i = 0; i< MsgSize; i++)
        content[i] = (byte) (i & 0xFF);


      try {
      for (int nb=0; nb<NbMsg; nb++) {
        BytesMessage msg = sess.createBytesMessage();
        msg.writeBytes(content);
        prod.send(msg);
        System.out.println("message sent" +nb);
      }
      System.out.println("message sent");

      prod.close();
      sess.close();
      cnx.close();
      } catch (javax.jms.JMSException exc) {
        
      }

      // kill and restart server
      System.out.println("Server stop ");
      Thread.sleep(30000L);
      TestCase.stopAgentServer((short)0);
      System.out.println("server start");
      TestCase.startAgentServer((short)0);
      Thread.sleep(30000L);

      cnx = cf.createConnection();
      sess = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageConsumer cons = sess.createConsumer(dest);
      cnx.start();

      for (int nb=0; nb<NbMsg; nb++) {
        BytesMessage msg = (BytesMessage) cons.receive();
        System.out.println("message received" + nb);
      }
      System.out.println("message received");
      
      cons.close();
      sess.close();
      cnx.close();
    }catch(Throwable exc){
      exc.printStackTrace();
      error(exc);
    }finally{
      TestCase.stopAgentServer((short)0);
      endTest();
    }

    System.exit(0);
  }

}
