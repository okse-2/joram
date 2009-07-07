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

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.ConnectionMetaData;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;

import fr.dyade.aaa.common.Configuration;

/**
 * check message ID and system property
 * 
 */
public class Test9 extends BaseTest {
  static int NbRound = 100;
  static ConnectionFactory cf = null;

  public static void main(String args[]) throws Exception {
    new Test9().run();
  }

  public void run() {
    try {
      System.out.println("server start");
      startServer();
      String baseclass = "joram.noreg.ColocatedBaseTest";
      baseclass = System.getProperty("BaseClass", baseclass);

      NbRound = Integer.getInteger("NbRound", NbRound).intValue();

      ConnectionFactory cf = createConnectionFactory(baseclass);
      AdminModule.connect(cf);

      Destination dest = createDestination("org.objectweb.joram.client.jms.Queue");
      dest.setFreeReading();
      dest.setFreeWriting();

      User.create("anonymous", "anonymous", 0);

      org.objectweb.joram.client.jms.admin.AdminModule.disconnect();

      Connection cnx = cf.createConnection();
      ConnectionMetaData cnxmd = cnx.getMetaData();

      // System.out.println("Provider: " + cnxmd.getJMSProviderName() +
      // cnxmd.getProviderVersion());
      assertEquals("Joram", cnxmd.getJMSProviderName());

      // System.out.println("Transaction: " +
      // Configuration.getProperty("Transaction"));
      // assertEquals("fr.dyade.aaa.util.NTransaction",Configuration.getProperty("Transaction"));

      // System.out.println("Engine: " + System.getProperty("Engine"));
      // assertEquals("fr.dyade.aaa.agent.GCEngine", Configuration.getProperty("Engine"));

      // System.out.println("baseclass: " + baseclass);
      assertEquals("joram.noreg.ColocatedBaseTest", baseclass);

      // System.out.println("NbRound=" + NbRound);
      assertEquals(10, NbRound);

      Session sess1 = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      Session sess2 = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageConsumer consumer = sess1.createConsumer(dest);
      MessageProducer producer = sess2.createProducer(dest);

      cnx.start();

      Message msg = sess2.createMessage();
      Message msgRcv;
      for (int i = 0; i < NbRound; i++) {
        producer.send(msg);
        msgRcv = consumer.receive();
        assertEquals(msg.getJMSMessageID(), msgRcv.getJMSMessageID());
      }
      // System.out.println("Test OK");
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      System.out.println("server stop");
      fr.dyade.aaa.agent.AgentServer.stop();
      endTest();

    }
  }
}
