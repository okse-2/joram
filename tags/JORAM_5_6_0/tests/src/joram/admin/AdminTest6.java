/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2010 ScalAgent Distributed Technologies
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
package joram.admin;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.JMSSecurityException;
import javax.jms.Session;

import org.objectweb.joram.client.jms.admin.AdminException;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import framework.TestCase;

/**
 * This test verify the behavior of the user deletion.
 */
public class AdminTest6 extends TestCase {

  public static void main(String[] args) {
    new AdminTest6().run();
  }

  public void run() {
    try {
      startAgentServer((short)0, new String[]{"-DTransaction=fr.dyade.aaa.util.NullTransaction"});
      Thread.sleep(2000);

      // Create an administration connection on server #0
      ConnectionFactory cf = TcpConnectionFactory.create("localhost", 2560);
      ((TcpConnectionFactory) cf).getParameters().connectingTimer = 60;

      AdminModule.connect(cf, "root", "root");
      User user = User.create("anonymous", "anonymous", 0);
      
      Connection connection = cf.createConnection("anonymous", "anonymous");
      Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      
      Thread.sleep(1000);

      // Can't delete a user with an active connection
      AdminException expectedException = null;
      try {
        user.delete();
      } catch (AdminException exc) {
        expectedException = exc;
      }
      assertNotNull(expectedException);

      // Delete a user
      User toto = User.create("toto", "toto");
      toto.delete();

      // Can't delete a non-existent user
      expectedException = null;
      try {
        toto.delete();
      } catch (AdminException exc) {
        expectedException = exc;
      }
      assertNotNull(expectedException);

      // Close connection
      connection.close();

      // Now we can delete anonymous
      user.delete();
      AdminModule.disconnect();

      JMSException expectedJMSException = null;
      try {
        connection = cf.createConnection("anonymous", "anonymous");
      } catch (JMSSecurityException exc) {
        expectedJMSException = exc;
      }
      assertNotNull(expectedJMSException);

    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      stopAgentServer((short)0);
      endTest();     
    }
  }
}
