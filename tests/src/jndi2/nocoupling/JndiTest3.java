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
 * Tests: - bind and create subcontext with a no master server
 */
public class JndiTest3 extends TestCase {
  public static String NAMING_FACTORY_PROP = "java.naming.factory.initial";
  public static String NAMING_FACTORY = "fr.dyade.aaa.jndi2.client.NamingContextFactory";
  public static String NAMING_HOST_PROP = "java.naming.factory.host";
  public static String LOCALHOST = "localhost";
  public static String NAMING_PORT_PROP = "java.naming.factory.port";

  public JndiTest3() {
    super();
  }

  public void run() {
    try {

      startAgentServer((short) 2, new String[] { "-DTransaction=fr.dyade.aaa.util.NTransaction" });

      Thread.sleep(1000);

      Hashtable env1 = new Hashtable();
      env1.put(NAMING_FACTORY_PROP, NAMING_FACTORY);
      env1.put(NAMING_HOST_PROP, LOCALHOST);
      env1.put(NAMING_PORT_PROP, "16682");

      InitialContext ctx1 = new InitialContext(env1);

      Exception excp = null;
      try {
        Thread.sleep(3000);
        ctx1.bind("/U", "U");
        ctx1.createSubcontext("/A");
        ctx1.bind("/A/W", "W");
      } catch (Exception exc) {
        excp = exc;
      }
      assertEquals(null, excp);

      System.out.println("End test3");

    } catch (Exception exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      stopAgentServer((short) 2);
      endTest();
    }
  }

  public static void main(String args[]) {
    new JndiTest3().run();
  }
}
