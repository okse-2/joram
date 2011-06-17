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
 * Initial developer(s): (ScalAgent D.T.)
 * Contributor(s): Badolle Fabien (ScalAgent D.T.)
 */
package jndi2.nocoupling;

import java.util.Hashtable;

import javax.naming.InitialContext;

import framework.TestCase;

/**
 * Tests: test with 2 servers
 * <ul>
 * <li>start server 0. bind and create subcontext</li>
 * <li>start server 1 and kill server 0</li>
 * <li>lookup with server 1</li>
 * </ul>
 */
public class JndiTest2 extends TestCase {
  public static String NAMING_FACTORY_PROP = "java.naming.factory.initial";
  public static String NAMING_FACTORY = "fr.dyade.aaa.jndi2.client.NamingContextFactory";
  public static String NAMING_HOST_PROP = "java.naming.factory.host";
  public static String LOCALHOST = "localhost";
  public static String NAMING_PORT_PROP = "java.naming.factory.port";

  public JndiTest2() {
    super();
  }

  public void run() {
    try {
      startAgentServer((short) 0, new String[] { "-DTransaction=fr.dyade.aaa.util.NTransaction" });

      Thread.sleep(2000);

      Hashtable env0 = new Hashtable();
      env0.put(NAMING_FACTORY_PROP, NAMING_FACTORY);
      env0.put(NAMING_HOST_PROP, LOCALHOST);
      env0.put(NAMING_PORT_PROP, "16600");

      InitialContext ctx0 = new InitialContext(env0);
      Exception excp = null;
      try {
        ctx0.bind("/B", "B");
        ctx0.createSubcontext("/A");
        ctx0.bind("/A/D", "D");
        Thread.sleep(3000);
      } catch (Exception exc) {
        excp = exc;
      }
      assertEquals(null, excp);

      startAgentServer((short) 1, new String[] { "-DTransaction=fr.dyade.aaa.util.NTransaction" });

      Thread.sleep(2000);

      Hashtable env1 = new Hashtable();
      env1.put(NAMING_FACTORY_PROP, NAMING_FACTORY);
      env1.put(NAMING_HOST_PROP, LOCALHOST);
      env1.put(NAMING_PORT_PROP, "16601");

      InitialContext ctx1 = new InitialContext(env1);

      Thread.sleep(30000);
      stopAgentServer((short) 0);

      String look = (String) ctx1.lookup("/B");
      assertEquals("B", look);

      look = (String) ctx1.lookup("/A/D");
      assertEquals("D", look);

      System.out.println("End test2");

    } catch (Exception exc) {
      exc.printStackTrace();
      error(exc);
      // In case the exception occurred before stopping the server 0
      stopAgentServer((short) 0);
    } finally {
      stopAgentServer((short) 1);
      endTest();
    }
  }

  public static void main(String args[]) {
    new JndiTest2().run();
  }
}
