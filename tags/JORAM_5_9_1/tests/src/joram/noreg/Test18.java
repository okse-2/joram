/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 - 2009 ScalAgent Distributed Technologies
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

import fr.dyade.aaa.agent.AgentServer;

/**
 * Test the activation of the exception listener on connection close.
 */
public class Test18 extends BaseTest implements ExceptionListener {
  ConnectionFactory cf;
  Connection cnx1;
  
  int nbexc;
  
  boolean ended = false;
  
  public void onException(JMSException exc) {
    nbexc += 1;
    assertEquals("javax.jms.IllegalStateException", exc.getClass().getName());
    connect();
  }
  
  public synchronized void connect() {
    if (ended || (cnx1 != null)) return;
    
    try {
//      System.out.println("connect");
      cnx1 = cf.createConnection();
      cnx1.setExceptionListener(this);
      cnx1.start();
//      System.out.println("connect - end");
    } catch (JMSException exc2) {
      exc2.printStackTrace();
      error(exc2);
      AgentServer.stop();
      endTest();
    }
  }

  public synchronized void close() {
    try {
      //  System.out.println("close");
      if (cnx1 != null) cnx1.close();
      cnx1 = null;
    } catch (JMSException exc) {
      System.out.println("Error during close: " + exc.getMessage());
    }
  }
  
  public static void main (String args[]) throws Exception {
    new Test18().run();
  }
  
  public void run(){
    try{
      startServer();

      String baseclass = "joram.noreg.ColocatedBaseTest";
      baseclass = System.getProperty("BaseClass", baseclass);

      Thread.sleep(500L);

      cf = createConnectionFactory(baseclass);
      AdminModule.connect(cf);
      User.create("anonymous", "anonymous", 0);
      AdminModule.disconnect();

      connect();
      
      for (int i=0; i<10; i++) {
        close();
        Thread.sleep(2500L);
      }
      
      assertEquals(10, nbexc);
      
      ended = true;
      close();
    } catch(Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      AgentServer.stop();
      endTest();
    }
  }
}
