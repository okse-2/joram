/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2006 - 2007 ScalAgent Distributed Technologies
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
 * Initial developer(s):Feliot David (ScalAgent D.T.)
 * Contributor(s): Badolle Fabien (ScalAgent D.T.)
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


public class JndiCrashTest extends TestCase {
  public static String NAMING_FACTORY_PROP = "java.naming.factory.initial";
  public static String NAMING_FACTORY = "fr.dyade.aaa.jndi2.client.NamingContextFactory";
  public static String NAMING_HOST_PROP = "java.naming.factory.host";
  public static String LOCALHOST = "localhost";
  public static String NAMING_PORT_PROP = "java.naming.factory.port";
  
  public JndiCrashTest() {
    super();
  }

  public void run() {
    try {
      File s0 = new File("./s0");
      File s1 = new File("./s1");
      
      System.out.println("Start s0");
      startAgentServer(
        (short)0, new String[]{"-DTransaction=fr.dyade.aaa.util.NTransaction"});
      
      System.out.println("Start s1");
      startAgentServer(
          (short)1, new String[]{"-DTransaction=fr.dyade.aaa.util.NTransaction"});
      
      System.out.println("Start s2");
      startAgentServer(
          (short)2, new String[]{"-DTransaction=fr.dyade.aaa.util.NTransaction"});
        
      Thread.sleep(3000);
      
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
      
      ctx0.bind("/A", "A");
      
      //  System.out.println(" lookup1 = " + ctx1.lookup("/A"));
      assertEquals("A", ctx1.lookup("/A"));
      // TEST 1
      
      stopAgentServer((short)1);
      
      Thread.sleep(5000);
      
      deleteDirectory(s1);
      
      startAgentServer(
          (short)1, new String[]{"-DTransaction=fr.dyade.aaa.util.NTransaction"});
        
      Thread.sleep(4000);
      
      ctx1 = new InitialContext(env1);
      // System.out.println(" lookup2 = " + ctx1.lookup("/A"));
      assertEquals("A", ctx1.lookup("/A"));
      // Check that the jndi si still working after the recovery.
      ctx0.bind("/B", "B");
      
      //  System.out.println(" lookup3 = " + ctx1.lookup("/B"));
      assertEquals("B", ctx1.lookup("/B"));
      //  System.out.println(" lookup4 = " + ctx2.lookup("/B"));
      assertEquals("B", ctx2.lookup("/B"));
      
      /*
      // TEST 2
      
      stopAgentServer((short)0);
      
      Thread.sleep(5000);
      
      System.out.println("delete s0");
      
      deleteDirectory(s0);
      
      startAgentServer(
          (short)0, (File)null, 
          new String[]{"-DTransaction=fr.dyade.aaa.util.NTransaction"});
        
      Thread.sleep(3000);
      
      System.out.println(" lookup = " + ctx0.lookup("/A"));
      
      ctx1 = new InitialContext(env1);
      System.out.println(" lookup3 = " + ctx1.lookup("/A"));
      
      ctx0 = new InitialContext(env0);
      ctx0.bind("/B", "B");
      System.out.println(" lo");
     
      System.out.println(" lookup4 = " + ctx1.lookup("/B"));
      */
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
    new JndiCrashTest().run();
  }
}
