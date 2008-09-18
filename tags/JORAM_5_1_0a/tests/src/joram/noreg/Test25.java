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

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;

import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import fr.dyade.aaa.agent.AgentServer;

class ExcList25 implements ExceptionListener {
  String name = null;

  ExcList25(String name) {
    this.name = name;
  }

  public void onException(JMSException exc) {
    System.err.println(name + ": " + exc.getMessage());
    Test25.assertTrue(exc instanceof javax.jms.JMSSecurityException);
    exc.printStackTrace();
  }
}

class MsgList25 implements MessageListener {
  public void onMessage(Message msg) {
    try {
      System.out.println("onMessage");
    } catch(Exception ex) {
      ex.printStackTrace();
    }
  }
}

/**
 *
 */
public class Test25 extends BaseTest {
    static ConnectionFactory cf;
    static Destination dest;
    static Connection cnx1;

    public static void main (String args[]) throws Exception {
	new Test25().run();
    }
    public void run(){
	try{
	    startServer();
	    Thread.sleep(500L);

	   
	    AdminModule.connect("localhost", 16010, "root", "root", 60);

	    User user = User.create("anonymous", "anonymous", 0);
	    dest = Queue.create();
	    dest.setFreeReading();
	    dest.setFreeWriting();

	    cf = TcpConnectionFactory.create("localhost", 16010);
	    org.objectweb.joram.client.jms.FactoryParameters fp = ((org.objectweb.joram.client.jms.ConnectionFactory) cf).getParameters();
	    fp.cnxPendingTimer = 500;
	    fp.connectingTimer = 90;
	    AdminModule.disconnect();

	    Connection cnx = cf.createConnection();
	    cnx.setExceptionListener(new ExcList25("Test25.ExcLst"));
	    Session session = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
	    MessageConsumer consumer = session.createConsumer(dest);
	    consumer.setMessageListener(new MsgList25());
	    cnx.start();
	   

	    Thread.sleep(1000L);
	    AgentServer.stop();
	    Thread.sleep(10000L);
	}catch(Throwable exc){
	    exc.printStackTrace();
	    error(exc);
	}finally{
	  
	    endTest();
	}
    }
}
