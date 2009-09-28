/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - 2004 ScalAgent Distributed Technologies
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
 * Contributor(s):
 */
package joram.studycase.itgw;

import java.lang.reflect.Method;

import javax.jms.*;

import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.local.LocalConnectionFactory;

import fr.dyade.aaa.agent.AgentServer;

class Producer implements MessageListener {
  static int NbRound = 10;
  static int NbMsgPerRound = 100;
  static int MsgSize = 100;

  static short localSID = -1;

  static Destination dest = null;
  static Destination sync = null;

  static Destination createDestination(String classname,
                                       String name,
                                       int sid) throws Exception {
    Class c = Class.forName(classname);
    Method m = c.getMethod("create",
                           new Class[]{int.class,
                                       java.lang.String.class});
    return (Destination) m.invoke(null, new Object[]{new Integer(sid), name});
  }

  public static void main(String[] args) throws Exception {
    new Producer().run(args);
  }

  Connection cnx = null;
  Session sess1 = null;
  Session sess2 = null;
  MessageProducer producer = null;
  MessageConsumer cons = null;

  public void run(String[] args) throws Exception {
    NbRound = Integer.getInteger("NbRound", NbRound).intValue();
    NbMsgPerRound = Integer.getInteger("NbMsgPerRound", NbMsgPerRound).intValue();
    MsgSize = Integer.getInteger("MsgSize", MsgSize).intValue();
    String destc = System.getProperty("Destination",
                                      "org.objectweb.joram.client.jms.Queue");

    localSID = Integer.getInteger("localSID", localSID).shortValue();

    AgentServer.init((short) localSID, "./s" + localSID, null);
    AgentServer.start();


    Thread.sleep(1000L);

    AdminModule.collocatedConnect("root", "root");

    dest = createDestination(destc, "queue", 0);
    sync = org.objectweb.joram.client.jms.Queue.create(localSID, "sync");

    User user = User.create("anonymous", "anonymous", localSID);

    sync.setFreeReading();
    sync.setFreeWriting();
    
    org.objectweb.joram.client.jms.admin.AdminModule.disconnect();

    try {
      cnx = new LocalConnectionFactory().createConnection();
      sess1 = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      cons = sess1.createConsumer(sync);
      cons.setMessageListener(this);
      sess2 = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      producer = sess2.createProducer(dest);
      cnx.start();

      byte[] content = new byte[MsgSize];
      for (int i = 0; i< MsgSize; i++)
        content[i] = (byte) (i & 0xFF);
      BytesMessage msg = sess1.createBytesMessage();
      msg.writeBytes(content);
      msg.setJMSReplyTo(sync);

      for (int i=0; i<NbRound; i++) {
        for (int j=NbMsgPerRound-1; j>=0; j--) {
          msg.setIntProperty("index", j);
          producer.send(msg);
        }
        fxCtrl(i);
      }
    } catch (Exception exc) {
      exc.printStackTrace();
      System.exit(-1);
    } finally {
      cnx.close();
    }
  }

  public synchronized void fxCtrl(int round) {
    System.out.println(" " + localSID + " " + counter + " " + round);
    while (counter < round) {
      try {
        wait();
      } catch (InterruptedException exc) {
      }
    }
  }

  int counter = 0;

  public synchronized void onMessage(Message msg) {
    try {
      System.out.println(" " + localSID + " " + counter);
      counter += 1;
      notify();
    } catch (Exception exc) {
      exc.printStackTrace();
    }
  }
}
