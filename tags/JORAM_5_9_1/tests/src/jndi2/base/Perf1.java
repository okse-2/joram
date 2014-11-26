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
 * Initial developer(s): ScalAgent D.T.
 * Contributor(s): 
 */
package jndi2.base;

import java.util.Hashtable;
import java.util.Random;

import javax.naming.Context;
import javax.naming.InitialContext;

import fr.dyade.aaa.agent.AgentServer;
import framework.TestCase;

/**
 * Measures JNDI lookup and bind costs with many bindings in a unique context.
 */

public class Perf1 extends TestCase {
  public final static int NbRegisteredObject = 10000;
  
  public static void main(String[] args) {
    new Perf1().run();
  }
  
  static void startServer() throws Exception {
    AgentServer.init((short) 0, "./s0", null);
    AgentServer.start();

    Thread.sleep(1000L);
  }

  public void run() {
    try {
      startServer();
      Thread.sleep(1000);

      Hashtable properties = new Hashtable();
      properties.put("java.naming.factory.initial", "fr.dyade.aaa.jndi2.client.NamingContextFactory");
      properties.put("java.naming.factory.host", "localhost");
      properties.put("java.naming.factory.port", "16600");
      Context ctx = new InitialContext(properties);

      long start, end;
      
      Exception excep = null;
      try {
        start = System.nanoTime();
        for (int i=0; i<NbRegisteredObject; i++)
          ctx.bind("toto" + i, "hello#" + i);
        end = System.nanoTime();
        System.out.println("binding (ms) = " + ((end-start)/1000000L));
      } catch (Exception exc) {
        excep = exc;
        exc.printStackTrace();
      }
      assertNull(excep);
      if (excep != null) throw excep;
      
      Random rand = new Random();
      try {
        start = System.nanoTime();
        for (int i=0; i<100000; i++) {
          String hello = (String) ctx.lookup("toto" + rand.nextInt(NbRegisteredObject));
        }
        end = System.nanoTime();
        System.out.println("binding (ms) = " + ((end-start)/1000000L));
      } catch (Exception exc) {
        excep = exc;
        exc.printStackTrace();
      }
      assertNull(excep);
      if (excep != null) throw excep;
      
    } catch (Exception exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      AgentServer.stop();
      endTest();
    }
  }

}
