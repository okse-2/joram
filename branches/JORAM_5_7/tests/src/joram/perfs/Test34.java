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
package joram.perfs;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Topic;

import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.admin.User;

import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.common.Configuration;

/**
 *
 */
public class Test34 extends BaseTest {
    static int NbMsg = 1000;
    static int MsgSize = 100;

    static Destination dest = null;
    static ConnectionFactory cf = null;

    static boolean MsgTransient = true;
    static boolean SubDurable = false;
    static boolean transacted = false;

    public static void main (String args[]) throws Exception {
	new Test34().run();
    }
    public void run(){
	try{
	    writeIntoFile("======================= start test =========================");
	    AgentServer.init((short) 0, "./s0", null);
	    AgentServer.start();
	    
	    String baseclass = "joram.perfs.ColocatedBaseTest";
	    baseclass = System.getProperty("BaseClass", baseclass);

	    String destclass = System.getProperty("Destination",
						  "org.objectweb.joram.client.jms.Queue");

	    NbMsg = Integer.getInteger("NbMsg", NbMsg).intValue();
	    MsgSize = Integer.getInteger("MsgSize", MsgSize).intValue();

	    MsgTransient = Boolean.getBoolean("MsgTransient");
	    SubDurable = Boolean.getBoolean("SubDurable");
	    transacted = Boolean.getBoolean("Transacted");

	    AdminConnect(baseclass);

	    dest = createDestination(destclass);
	    dest.setFreeReading();
	    dest.setFreeWriting();

	    User user = User.create("anonymous", "anonymous", 0);

	    org.objectweb.joram.client.jms.admin.AdminModule.disconnect();

	    writeIntoFile("----------------------------------------------------");
	    writeIntoFile("Transaction: " + Configuration.getProperty("Transaction"));
	    writeIntoFile("Engine: " + Configuration.getProperty("Engine"));
	    writeIntoFile("baseclass: " + baseclass +
			  ", Transacted=" + Configuration.getBoolean("Transacted"));
	    writeIntoFile("Message: transient=" + MsgTransient);
	    writeIntoFile("Subscriber: durable=" + SubDurable);
	    writeIntoFile("NbMsg=" + NbMsg + ", MsgSize=" + MsgSize);
	    writeIntoFile("----------------------------------------------------");

	    ConnectionFactory cf =  createConnectionFactory(baseclass);

	    Connection cnx1 = cf.createConnection();
	    Session sess1 = cnx1.createSession(transacted, Session.AUTO_ACKNOWLEDGE);
	    MessageProducer producer = sess1.createProducer(dest);
	    if (MsgTransient) {
		producer.setDeliveryMode(javax.jms.DeliveryMode.NON_PERSISTENT);
	    }
	    cnx1.start();
    
	    long dt1 = 0L;
	   
	    byte[] content = new byte[MsgSize];
	    for (int i = 0; i< MsgSize; i++)
		content[i] = (byte) (i & 0xFF);

	    long start = System.currentTimeMillis();
	    for (int i=0; i<NbMsg; i++) {
		BytesMessage msg = sess1.createBytesMessage();
		msg.writeBytes(content);
		producer.send(msg);
		if (transacted && ((i%10) == 9)) sess1.commit();
		//         if ((i%(NbMsg/10)) == ((NbMsg/10)-1)) System.out.println("+");
	    }
	    long end = System.currentTimeMillis();
	    dt1 = end - start;
	  
	    sess1.close();
	    cnx1.close();

	    writeIntoFile("--------------------------------------------------");
	    writeIntoFile("| sender " +  ((NbMsg *1000L)/(dt1)) + " msg/s");

	    Connection cnx2 = cf.createConnection();
	    Session sess2 = cnx2.createSession(false, Session.AUTO_ACKNOWLEDGE);
	    MessageConsumer cons = null;
	    if (SubDurable && (dest instanceof Topic))
		cons = sess2.createDurableSubscriber((Topic) dest, "name", null, false);
	    else
		cons = sess2.createConsumer(dest, null);
	    cnx2.start();

	    long dt2 = 0L;
	  
	     start = System.currentTimeMillis();
	    for (int i=0; i<NbMsg; i++) {
		BytesMessage msg = (BytesMessage) cons.receive();
		//         if ((i%(NbMsg/10)) == ((NbMsg/10)-1)) System.out.println("-");
	    }
	     end = System.currentTimeMillis();
	    dt2 = end - start;
	   

	    sess2.close();
	    cnx2.close();

	    writeIntoFile("| receiver " + ((NbMsg *1000L)/(dt2)) + " msg/s");
	    writeIntoFile("--------------------------------------------------");

	}catch(Throwable exc){
	    exc.printStackTrace();
	    error(exc);
	}finally{
	    AgentServer.stop();
	    endTest();
	    System.exit(-1);
	}
    }
}

