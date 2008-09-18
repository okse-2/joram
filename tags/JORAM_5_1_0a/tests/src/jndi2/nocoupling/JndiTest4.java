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
 
package jndi2.nocoupling;

import java.io.File;
import java.util.Hashtable;

import javax.naming.Binding;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;

import joram.framework.TestCase;

/**
 * Tests: Simple test with 2 servers
 *    - bind and createsubcontext
 *    - rebind, unbind, destroysubcontext
 *
 */
public class JndiTest4 extends TestCase {
  public static String NAMING_FACTORY_PROP = "java.naming.factory.initial";
  public static String NAMING_FACTORY = "fr.dyade.aaa.jndi2.client.NamingContextFactory";
  public static String NAMING_HOST_PROP = "java.naming.factory.host";
  public static String LOCALHOST = "localhost";
  public static String NAMING_PORT_PROP = "java.naming.factory.port";

  public JndiTest4() {
    super();
  }

  public void run() {
    try {
	startAgentServer(
			 (short)0, (File)null, 
			 new String[]{"-DTransaction=fr.dyade.aaa.util.NTransaction"});
	startAgentServer(
			 (short)1, (File)null, 
			 new String[]{"-DTransaction=fr.dyade.aaa.util.NTransaction"});      
	
		startAgentServer(
			 (short)2, (File)null, 
			 new String[]{"-DTransaction=fr.dyade.aaa.util.NTransaction"}); 
	
	Hashtable env1 = new Hashtable();
	env1.put(NAMING_FACTORY_PROP, NAMING_FACTORY);
	env1.put(NAMING_HOST_PROP, LOCALHOST);
	env1.put(NAMING_PORT_PROP, "16401");
	
	Hashtable env0 = new Hashtable();
	env0.put(NAMING_FACTORY_PROP, NAMING_FACTORY);
	env0.put(NAMING_HOST_PROP, LOCALHOST);
	env0.put(NAMING_PORT_PROP, "16400");

		Hashtable env2 = new Hashtable();
	env2.put(NAMING_FACTORY_PROP, NAMING_FACTORY);
	env2.put(NAMING_HOST_PROP, LOCALHOST);
	env2.put(NAMING_PORT_PROP, "16402");
	
	InitialContext ctx1 = new InitialContext(env1);
	InitialContext ctx0 = new InitialContext(env0);
		InitialContext ctx2 = new InitialContext(env2);


	Exception excp=null;
	try{
	     ctx0.bind("/B","B");
	     // ctx0.bind("/C","C");
	     // ctx0.bind("/D","D");

	     // ctx0.createSubcontext("/A");
	     // ctx0.bind("/A/C","C");
	    Thread.sleep(2000);
	    ctx1.bind("/D","D");
	    // ctx1.createSubcontext("/A/E");
	    // ctx1.bind("/A/E/S","S");
	     Thread.sleep(2000);
	}catch(Exception exc){
	    excp=exc;
	}


	NamingEnumeration<Binding> namingEnumeration = ctx0.listBindings("");
	while (namingEnumeration.hasMoreElements()) {
	    // NameClassPair classPair = (NameClassPair) namingEnumeration.next(); 
	    Binding classPair = (Binding) namingEnumeration.next();
	    
	    System.out.println("zz "+classPair.getName());
	}

	//listBindings
	namingEnumeration = ctx1.listBindings("");
	while (namingEnumeration.hasMoreElements()) {
	    // NameClassPair classPair = (NameClassPair) namingEnumeration.next();
 Binding classPair = (Binding) namingEnumeration.next();
	    System.out.println("zz1 "+classPair.getName());
	}

		namingEnumeration = ctx2.listBindings("");
	while (namingEnumeration.hasMoreElements()) {
	    //  NameClassPair classPair = (NameClassPair) namingEnumeration.next();
 Binding classPair = (Binding) namingEnumeration.next();
	    System.out.println("zz2 "+classPair.getName());
	}
	



	/*	String look = (String) ctx1.lookup("/A/C");
	assertEquals("C",look);
	System.out.println(look);
	look = (String) ctx0.lookup("/B");
	assertEquals("B",look);
	System.out.println(look);*/
	System.out.println("End test4");
	
    } catch (Exception exc) {   
	exc.printStackTrace();
	error(exc);      
    } finally {
	stopAgentServer((short)0);
	stopAgentServer((short)1);
	stopAgentServer((short)2);

	endTest();
    }
  }
    
    public static void main(String args[]) {
	new JndiTest4().run();
    }
}
