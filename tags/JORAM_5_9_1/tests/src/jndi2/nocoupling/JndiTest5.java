/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2010 ScalAgent Distributed Technologies
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
 * Contributor(s): Badolle Fabien (ScalAgent D.T.)
 */
package jndi2.nocoupling;

import java.util.Hashtable;

import javax.naming.InitialContext;

import framework.TestCase;

/**
 * Tests: Simple test with 2 servers
 * <ul>
 * <li>bind and createsubcontext</li>
 * <li>rebind, unbind, destroysubcontext</li>
 *</ul>
 */
public class JndiTest5 extends TestCase {
  public static String NAMING_FACTORY_PROP = "java.naming.factory.initial";
  public static String NAMING_FACTORY = "fr.dyade.aaa.jndi2.client.NamingContextFactory";
  public static String NAMING_HOST_PROP = "java.naming.factory.host";
  public static String LOCALHOST = "localhost";
  public static String NAMING_PORT_PROP = "java.naming.factory.port";

  public JndiTest5() {
    super();
  }

  public void run() {
    try {
      System.out.println("Start s0");
      startAgentServer((short) 0, new String[] { "-DTransaction=fr.dyade.aaa.util.NullTransaction" });

      System.out.println("Start s1");
      startAgentServer((short) 1, new String[] { "-DTransaction=fr.dyade.aaa.util.NullTransaction" });

      Thread.sleep(2000);

      Hashtable env0 = new Hashtable();
      env0.put(NAMING_FACTORY_PROP, NAMING_FACTORY);
      env0.put(NAMING_HOST_PROP, LOCALHOST);
      env0.put(NAMING_PORT_PROP, "16600");
      InitialContext ctx0 = new InitialContext(env0);

      Hashtable env1 = new Hashtable();
      env1.put(NAMING_FACTORY_PROP, NAMING_FACTORY);
      env1.put(NAMING_HOST_PROP, LOCALHOST);
      env1.put(NAMING_PORT_PROP, "16681");
      InitialContext ctx1 = new InitialContext(env1);

      Hashtable env2 = new Hashtable();
      env2.put(NAMING_FACTORY_PROP, NAMING_FACTORY);
      env2.put(NAMING_HOST_PROP, LOCALHOST);
      env2.put(NAMING_PORT_PROP, "16682");
      InitialContext ctx2 = new InitialContext(env2);

      Hashtable env3 = new Hashtable();
      env3.put(NAMING_FACTORY_PROP, NAMING_FACTORY);
      env3.put(NAMING_HOST_PROP, LOCALHOST);
      env3.put(NAMING_PORT_PROP, "16683");
      InitialContext ctx3 = new InitialContext(env3);

      // Binds on S0
      System.out.println("Bind on S0");
      ctx0.bind("/A", "A");
      ctx0.bind("/B", "B");

      Thread.sleep(15000);

      // Verify data on S1
      System.out.println("Verify on S1");
      assertEquals("A", ctx1.lookup("/A"));
      assertEquals("B", ctx1.lookup("/B"));

      // Binds on S1
      System.out.println("Bind on S1");
      ctx1.bind("/C", "C");
      ctx1.bind("/D", "D");

      Thread.sleep(5000);

      // Verify data on S0
      System.out.println("Verify on S0");
      assertEquals("C", ctx0.lookup("/C"));
      assertEquals("D", ctx0.lookup("/D"));

      System.out.println("Start s2");
      startAgentServer((short) 2, new String[] { "-DTransaction=fr.dyade.aaa.util.NullTransaction" });

      Thread.sleep(5000);

      // Verify data on S2
      System.out.println("Verify on S2");
      assertEquals("A", ctx2.lookup("/A"));
      assertEquals("B", ctx2.lookup("/B"));
      assertEquals("C", ctx2.lookup("/C"));
      assertEquals("D", ctx2.lookup("/D"));

      // Stop server S1 then restart it (all datas are lost).
      System.out.println("Stop S1");
      stopAgentServer((short) 1);
      Thread.sleep(1000);
      System.out.println("Start S1");
      startAgentServer((short) 1, new String[] { "-DTransaction=fr.dyade.aaa.util.NullTransaction" });
      Thread.sleep(5000);

      // Binds on S1
      System.out.println("Bind on S1");
      ctx1.rebind("/C", "C1");
      ctx1.rebind("/D", "D1");

      // Verify data on S1
      System.out.println("Verify on S1");
      assertEquals("A", ctx1.lookup("/A"));
      assertEquals("B", ctx1.lookup("/B"));
      assertEquals("C1", ctx1.lookup("/C"));
      assertEquals("D1", ctx1.lookup("/D"));

      Thread.sleep(5000);

      // Verify data on S0
      System.out.println("Verify on S0");
      assertEquals("C1", ctx0.lookup("/C"));
      assertEquals("D1", ctx0.lookup("/D"));

      // Start S3.
      System.out.println("Start S3");
      startAgentServer((short) 3, new String[] { "-DTransaction=fr.dyade.aaa.util.NullTransaction" });
      Thread.sleep(5000);

      // Binds on S0 and S1
      System.out.println("Bind on S0 and S1");
      ctx0.bind("/E", "E");
      ctx1.bind("/F", "F");

      Thread.sleep(5000);

      // Verify data on S3
      System.out.println("Verify on S3");
      assertEquals("A", ctx3.lookup("/A"));
      assertEquals("B", ctx3.lookup("/B"));
      assertEquals("C1", ctx3.lookup("/C"));
      assertEquals("D1", ctx3.lookup("/D"));
      assertEquals("E", ctx3.lookup("/E"));
      assertEquals("F", ctx3.lookup("/F"));

      // Stop server S0 then restart it (all datas are lost).
      System.out.println("Stop S0");
      stopAgentServer((short) 0);
      Thread.sleep(1000);
      System.out.println("Start S0");
      startAgentServer((short) 0, new String[] { "-DTransaction=fr.dyade.aaa.util.NullTransaction" });
      Thread.sleep(5000);

      // Verify data on S0
      System.out.println("Verify on S0");
      assertEquals("A", ctx0.lookup("/A"));
      assertEquals("B", ctx0.lookup("/B"));
      assertEquals("C1", ctx0.lookup("/C"));
      assertEquals("D1", ctx0.lookup("/D"));
      assertEquals("E", ctx0.lookup("/E"));
      assertEquals("F", ctx0.lookup("/F"));

    } catch (Exception exc) {
      exc.printStackTrace();
      error(exc);

    } finally {
      stopAgentServer((short) 0);
      stopAgentServer((short) 1);
      stopAgentServer((short) 2);
      stopAgentServer((short) 3);
      endTest();
    }
  }

  public static void main(String args[]) {
    new JndiTest5().run();
  }
}
