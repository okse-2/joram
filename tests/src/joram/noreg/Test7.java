/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - 2009 ScalAgent Distributed Technologies
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
import org.objectweb.joram.client.jms.admin.User;

import fr.dyade.aaa.util.Configuration;

/**
 * check system property and create multiple queue
 *
 */
public class Test7 extends BaseTest {
  static int NbDest = 150;
  static int NbMsg = 10;
  static int MsgSize = 100;
  static Destination dest[] = null;
  static ConnectionFactory cf = null;

  public static void main (String args[]) throws Exception {
    new Test7().run();
  }
  
  public void run() {
    try {
      writeIntoFile("===================== start test =========================");
      if (! Boolean.getBoolean("ServerOutside"))
        startServer();

      String baseclass = "joram.noreg.ColocatedBaseTest";
      baseclass = System.getProperty("BaseClass", baseclass);
      NbDest = Integer.getInteger("NbDest", NbDest).intValue();
      NbMsg = Integer.getInteger("NbMsg", NbMsg).intValue();
      MsgSize = Integer.getInteger("MsgSize", MsgSize).intValue();
      String destclass = System.getProperty("Destination",
      "org.objectweb.joram.client.jms.Queue");
      writeIntoFile("----------------------------------------------------");
      writeIntoFile("| Transaction: " + Configuration.getProperty("Transaction"));
      writeIntoFile("| Engine: " + Configuration.getProperty("Engine"));
      writeIntoFile("| baseclass: " + baseclass);
      writeIntoFile("| Destination: " + destclass + "NbDest=" + NbDest);
      writeIntoFile("| NbMsg=" + NbMsg + ", MsgSize=" + MsgSize);
      writeIntoFile("----------------------------------------------------");

      ColocatedBaseTest.AdminConnect();

      dest = new Destination[NbDest];
      for (int i=0; i<NbDest; i++) {
        dest[i] = createDestination(destclass,"zz"+i);
        dest[i].setFreeReading();
        dest[i].setFreeWriting();
      }

      User user = User.create("anonymous", "anonymous", 0);
      cf = createConnectionFactory(baseclass);
      org.objectweb.joram.client.jms.admin.AdminModule.disconnect();

      Connection cnx = cf.createConnection();
      Session sess = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageProducer prod = sess.createProducer(null);
      MessageConsumer cons[] = new MessageConsumer[NbDest];
      for (int i=0; i<NbDest; i++)
        cons[i] = sess.createConsumer(dest[i]);
      cnx.start();

      byte[] content = new byte[MsgSize];
      for (int i = 0; i< MsgSize; i++)
        content[i] = (byte) (i & 0xFF);

      BytesMessage msg=null;
      for (int i=0; i<NbMsg; i++) {
        for (int j=0; j<NbDest; j++) {
          msg = sess.createBytesMessage();
          msg.writeBytes(content);
          prod.send(dest[j], msg);
        }
        //	System.out.println("message sent: " + i);
      }

      for (int i=0; i<NbMsg; i++) {
        for (int j=0; j<NbDest; j++) {
          msg = (BytesMessage) cons[j].receive(3000);
          assertNotNull(msg);
        }

        //	System.out.println("message received: " + i);
      }

    } catch(Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      System.out.println("Server stop ");
      fr.dyade.aaa.agent.AgentServer.stop();
      endTest(); 
    }

  }
}
