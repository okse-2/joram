/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2007 ScalAgent Distributed Technologies
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

package jndi2.distributed;


import java.io.*;
import java.util.*;
import javax.naming.*;

import framework.TestCase;


/**
 * Test suggested by Nigel Rantor:
 * 1) start JNDI server 0
 * 2) bind a name to node 0
 * 3) shut down node 0
 * 4) start JNDI server 1
 * 5) start JNDI server 0
 * -> lookup
 * 6) shut down node 1
 * 7) rebind the name to node 0
 * 8) shut down node 0
 * 9) start JNDI server 1
 * 10) start JNDI server 0 
 * -> lookup
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
      startAgentServer(
        (short)0, new String[]{"-DTransaction=fr.dyade.aaa.util.NTransaction"});
    
      Hashtable env0 = new Hashtable();
      env0.put(NAMING_FACTORY_PROP, NAMING_FACTORY);
      env0.put(NAMING_HOST_PROP, LOCALHOST);
      env0.put(NAMING_PORT_PROP, "16600");
      
      Thread.sleep(4000);

      InitialContext ctx0 = new InitialContext(env0);
      ctx0.bind("/A", "A");

      stopAgentServer((short)0);

      Thread.sleep(4000);

      startAgentServer(
        (short)1, new String[]{"-DTransaction=fr.dyade.aaa.util.NTransaction"});

      startAgentServer(
        (short)0, new String[]{"-DTransaction=fr.dyade.aaa.util.NTransaction"});

      Thread.sleep(2000);

      Hashtable env1 = new Hashtable();
      env1.put(NAMING_FACTORY_PROP, NAMING_FACTORY);
      env1.put(NAMING_HOST_PROP, LOCALHOST);
      env1.put(NAMING_PORT_PROP, "16601");

      InitialContext ctx1 = new InitialContext(env1);
      ctx1.lookup("/A");

      stopAgentServer((short)1);
      
      Thread.sleep(3000);

      ctx0.rebind("/A", "A(2)");

      stopAgentServer((short)0);

      Thread.sleep(2000);
      
      startAgentServer(
        (short)1, new String[]{"-DTransaction=fr.dyade.aaa.util.NTransaction"});

      startAgentServer(
        (short)0, new String[]{"-DTransaction=fr.dyade.aaa.util.NTransaction"});

      Thread.sleep(2000);

      String res = (String)ctx1.lookup("/A");
      assertTrue("Lookup failure", res.equals("A(2)"));
      
    } catch (Exception exc) {
      error(exc);      
    } finally {
      stopAgentServer((short)0);
      stopAgentServer((short)1);
      endTest();
    }
  }

  public static void main(String args[]) {
    new JndiTest5().run();
  }
}
