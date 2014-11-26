/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - 2013 ScalAgent Distributed Technologies
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
package joram.dursub;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.TopicSubscriber;


import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import framework.TestCase;

/**
 * see Test6.
 */
public class Test6b extends TestCase {

  public static void main(String[] args) {
    new Test6b().run();
  }

  public void run() {
  	int telnetPort = -1;
    try {
      try {
        telnetPort = Integer.getInteger("osgi.shell.telnet.port").intValue();
      } catch (NullPointerException npe) {
        error(new Exception("A telnet port must be specified to use stopAgentServerExt"));
      }
      ConnectionFactory cf = TcpConnectionFactory.create("localhost",2560 );
      ((TcpConnectionFactory) cf).getParameters().connectingTimer = 10;
      AdminModule.connect(cf, "root", "root");
      Topic topic = Topic.create("topic");
      
      Connection cnx = cf.createConnection("anonymous", "anonymous");
      cnx.setClientID("cnx_dursub");
      Session sess = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      TopicSubscriber cons = sess.createDurableSubscriber(topic, "dursub");

      cnx.start();

      TextMessage msg = (TextMessage) cons.receive(5000L);
      assertTrue(msg != null);
      if (msg != null) {
        assertEquals(msg.getText(), "msg2");
      }

      cnx.close();
      
      AdminModule.stopServer();
      AdminModule.disconnect();
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
    	if (telnetPort != -1) {
        stopAgentServerExt(telnetPort);
      }
      endTest();     
    }
  }
}

