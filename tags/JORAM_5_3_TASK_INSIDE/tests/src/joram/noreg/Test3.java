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

import fr.dyade.aaa.agent.AgentServer;

/**
 * Test transfer with big messages
 *
 */
public class Test3 extends BaseTest {
  static int MsgSize = 20*1024*1024;
  static int NbMsg = 100;
  static int MsgPerCommit = 10;

  static Destination dest = null;
  static ConnectionFactory cf = null;

  static String host = "localhost";
  static int port = 16010;

  public static void main(String[] args) throws Exception {
    new Test3().run(args);
  }

  public void run(String[] args) {
    try{
      if (! Boolean.getBoolean("ServerOutside"))
        startServer();
      writeIntoFile("===================== start test 3 =====================");
      MsgSize = Integer.getInteger("MsgSize", MsgSize/1024).intValue() *1024;
      NbMsg = Integer.getInteger("NbMsg", NbMsg).intValue();
      MsgPerCommit = Integer.getInteger("MsgPerCommit", MsgPerCommit).intValue(); 
      String destc = System.getProperty("Destination",
      "org.objectweb.joram.client.jms.Queue");

      host = System.getProperty("hostname", host);
      port = Integer.getInteger("port", port).intValue();

      writeIntoFile("----------------------------------------------------");
      writeIntoFile("Destination: " + destc);
      writeIntoFile("MsgSize: " + MsgSize);
      writeIntoFile("MsgPerCommit: " + MsgPerCommit);
      writeIntoFile("NbMsg: " + NbMsg);
      writeIntoFile("----------------------------------------------------");

      cf = TcpBaseTest.createConnectionFactory();
      AdminModule.connect(cf);
      dest = createDestination(destc);
      User user = User.create("anonymous", "anonymous", 0);
      dest.setFreeReading();
      dest.setFreeWriting();
      org.objectweb.joram.client.jms.admin.AdminModule.disconnect();

      Connection cnx = cf.createConnection();
      Session sess = cnx.createSession(true, Session.AUTO_ACKNOWLEDGE);
      MessageProducer prod = sess.createProducer(dest);
      MessageConsumer cons = sess.createConsumer(dest);
      cnx.start();

      byte[] content = new byte[MsgSize];
      for (int i = 0; i< MsgSize; i++)
        content[i] = (byte) (i & 0xFF);


      for (int nb=0; nb<NbMsg; nb++) {
        BytesMessage msg1 = sess.createBytesMessage();
        msg1.writeBytes(content);
        prod.send(msg1);
        if ((nb % MsgPerCommit) == 0) {
          //writeIntoFile(MsgPerCommit+" message sent ;message :" + nb);
          sess.commit();
        }
      }

      for (int nb=0; nb<NbMsg; nb++) {
        BytesMessage msg1 = (BytesMessage) cons.receive();
        if ((nb % MsgPerCommit) == 0) {
          //writeIntoFile(MsgPerCommit+" message receive : " + nb);
          sess.commit();
        }
      }
    }catch(Throwable exc){
      exc.printStackTrace();
      error(exc);
    }finally{
      AgentServer.stop();
      endTest();
    }

    System.exit(0);
  }

}
