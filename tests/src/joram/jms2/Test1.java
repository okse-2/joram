/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2013 ScalAgent Distributed Technologies
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
 * Initial developer(s): 
 */
package joram.jms2;

import javax.jms.ConnectionFactory;
import javax.jms.JMSContext;

import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import framework.TestCase;

/**
 * Creates a JMSContext with the default user identity and an unspecified sessionMode.
 */
public class Test1 extends TestCase {
  public static void main(String[] args) {
    new Test1().run();
  }

  public void run()  {
    try {
      startAgentServer((short) 0);
      Thread.sleep(1000);
      
      ConnectionFactory cf = TcpConnectionFactory.create("localhost", 2560);
      AdminModule.connect(cf, "root", "root");   
      User.create("anonymous", "anonymous", 0);
      AdminModule.disconnect();

      JMSContext context = cf.createContext();
      // Verify that JMSContext.getSessionMode() returns JMSContext.AUTO_ACKNOWLEDGE
      int mode = context.getSessionMode();
      assertTrue("Bad default session mode: " + mode, (mode == JMSContext.AUTO_ACKNOWLEDGE));
      context.close();
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      stopAgentServer((short) 0);
      endTest();     
    }
  }
}
