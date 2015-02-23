/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2012 ScalAgent Distributed Technologies
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

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;

import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import framework.TestCase;

/**
 *
 */
public class Test63 extends TestCase implements ExceptionListener {

  public static void main(String[] args) {
    new Test63().run();
  }

  JMSException exc = null;
  
  public void run() {
    try {
      startAgentServer((short) 0);
      Thread.sleep(1000);
      
      // Create an administration connection on server #0
      ConnectionFactory cf = TcpConnectionFactory.create("localhost", 16010);
      ((TcpConnectionFactory) cf).getParameters().cnxPendingTimer = 5000;
      ((TcpConnectionFactory) cf).getParameters().connectingTimer = 30;
      
      AdminModule.connect(cf, "root", "root");
      // Create the anonymous user needed for test
      User.create("anonymous", "anonymous");
      AdminModule.disconnect();
      
      Thread.sleep(1000);

      Connection cnx = cf.createConnection("anonymous", "anonymous");
      cnx.setClientID("ITSME");
      cnx.setExceptionListener(this);
      
      Thread.sleep(1000L);
      killAgentServer((short) 0);
      startAgentServer((short) 0);
      Thread.sleep(1000L);
      
      if (exc == null) Thread.sleep(1000L);
      if (exc == null) Thread.sleep(1000L);
      
      assertTrue("Connexion should be closed: " + exc, (exc != null));
      assertTrue("Connection should not be reinitialized", (exc instanceof javax.jms.JMSSecurityException));
      exc = null;
      
      Connection cnx2 = cf.createConnection("anonymous", "anonymous");
      cnx2.setClientID("ITSME");
      cnx2.setExceptionListener(this);
      
      assertTrue("Connexion should be closed", (exc == null));
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      killAgentServer((short) 0);
      endTest();     
    }
  }

  @Override
  public void onException(JMSException e) {
    this.exc = e;
//    e.printStackTrace();
  }
}
