/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 ScalAgent Distributed Technologies
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
 * Initial developer(s):ScalAgent Distributed Technologies
 * Contributor(s):
 */
package jndi2.distributed;

import java.io.File;
import java.util.Hashtable;

import javax.naming.InitialContext;

import framework.TestCase;




/**
 * start server 0,1,2
 * test 1 :
 *        - stop server 1, delete directory s1
 *        - start server 1, lookup with ctx1
 *        - bind /B with ctx0 and lookup with ctx1,ctx2
 *
 */


public class JndiTest6 extends TestCase {
  public static String NAMING_FACTORY_PROP = "java.naming.factory.initial";
  public static String NAMING_FACTORY = "fr.dyade.aaa.jndi2.client.NamingContextFactory";
  public static String NAMING_HOST_PROP = "java.naming.factory.host";
  public static String LOCALHOST = "localhost";
  public static String NAMING_PORT_PROP = "java.naming.factory.port";
  
  public JndiTest6() {
    super();
  }

  public void run() {
    try {
      System.out.println("Start s0");
      startAgentServer(
        (short)0, (File)null, 
        new String[]{"-DTransaction=fr.dyade.aaa.util.NullTransaction",
                     "-Dfr.dyade.aaa.jndi2.impl.LooseCoupling=true"});
      
      System.out.println("Start s1");
      startAgentServer(
          (short)1, (File)null, 
          new String[]{"-DTransaction=fr.dyade.aaa.util.NullTransaction",
                     "-Dfr.dyade.aaa.jndi2.impl.LooseCoupling=true"});
      
      System.out.println("Start s2");
      startAgentServer(
          (short)2, (File)null, 
          new String[]{"-DTransaction=fr.dyade.aaa.util.NullTransaction",
                     "-Dfr.dyade.aaa.jndi2.impl.LooseCoupling=true"});
        
      Thread.sleep(3000);
      
      Hashtable env0 = new Hashtable();
      env0.put(NAMING_FACTORY_PROP, NAMING_FACTORY);
      env0.put(NAMING_HOST_PROP, LOCALHOST);
      env0.put(NAMING_PORT_PROP, "16400");
      InitialContext ctx0 = new InitialContext(env0);
      
      Hashtable env1 = new Hashtable();
      env1.put(NAMING_FACTORY_PROP, NAMING_FACTORY);
      env1.put(NAMING_HOST_PROP, LOCALHOST);
      env1.put(NAMING_PORT_PROP, "16401");
      InitialContext ctx1 = new InitialContext(env1);
      
      Hashtable env2 = new Hashtable();
      env2.put(NAMING_FACTORY_PROP, NAMING_FACTORY);
      env2.put(NAMING_HOST_PROP, LOCALHOST);
      env2.put(NAMING_PORT_PROP, "16402");
      InitialContext ctx2 = new InitialContext(env2);
      
      System.out.println("Bind on S0");
      ctx0.createSubcontext("/S0");
      ctx0.bind("/S0/A", "A");
      System.out.println("Bind on S1");
      ctx1.createSubcontext("/S1");
      ctx1.bind("/S1/B", "B");
      System.out.println("Bind on S2");
      ctx2.createSubcontext("/S2");
      ctx2.bind("/S2/C", "C");

      // Verify data on S0
      System.out.println("Verify on S0");
      assertEquals("A", ctx0.lookup("/S0/A"));
      assertEquals("B", ctx0.lookup("/S1/B"));
      assertEquals("C", ctx0.lookup("/S2/C"));
      // Verify data on S1
      System.out.println("Verify on S1");
      assertEquals("A", ctx1.lookup("/S0/A"));
      assertEquals("B", ctx1.lookup("/S1/B"));
      assertEquals("C", ctx1.lookup("/S2/C"));
      // Verify data on S2
      System.out.println("Verify on S2");
      assertEquals("A", ctx2.lookup("/S0/A"));
      assertEquals("B", ctx2.lookup("/S1/B"));
      assertEquals("C", ctx2.lookup("/S2/C"));
      
      // Stop server S1 then restart it (all datas are lost).
      System.out.println("Stop S1");
      stopAgentServer((short)1);
      Thread.sleep(1000);      
      System.out.println("Start S1");
      startAgentServer(
          (short)1, (File)null, 
          new String[]{"-DTransaction=fr.dyade.aaa.util.NullTransaction",
                     "-Dfr.dyade.aaa.jndi2.impl.LooseCoupling=true"});
      Thread.sleep(3000);
      
      // rebinds object on S1
      System.out.println("Rebind on S1");
//       ctx1 = new InitialContext(env1);
      try {
        ctx1.createSubcontext("/S1");
      } catch (Exception exc) {
        exc.printStackTrace();      
      }
      ctx1.rebind("/S1/B", "B1");
      
      // Verify data on S2
      System.out.println("Verify on S1");
      assertEquals("A", ctx1.lookup("/S0/A"));
      assertEquals("B1", ctx1.lookup("/S1/B"));
      assertEquals("C", ctx1.lookup("/S2/C"));
      // Verify data on S0
      System.out.println("Verify on S0");
      assertEquals("B1", ctx0.lookup("/S1/B"));
      // Verify data on S1
      System.out.println("Verify on S2");
      assertEquals("B1", ctx2.lookup("/S1/B"));
    } catch (Exception exc) {
      error(exc);      
    } finally {
      stopAgentServer((short)0);
      stopAgentServer((short)1);
      stopAgentServer((short)2);
      endTest();
    }
  }
  
  public static void main(String args[]) {
    new JndiTest6().run();
  }
}
