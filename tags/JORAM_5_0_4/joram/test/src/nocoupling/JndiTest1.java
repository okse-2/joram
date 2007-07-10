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
 
package nocoupling;

import framework.TestCase;

import java.io.*;
import java.util.*;
import javax.naming.*;

/**
 * Tests: Simple test with 2 servers
 *    - bind and createsubcontext
 *    - rebind, unbind, destroysubcontext
 *
 */
public class JndiTest1 extends TestCase {
  public static String NAMING_FACTORY_PROP = "java.naming.factory.initial";
  public static String NAMING_FACTORY = "fr.dyade.aaa.jndi2.client.NamingContextFactory";
  public static String NAMING_HOST_PROP = "java.naming.factory.host";
  public static String LOCALHOST = "localhost";
  public static String NAMING_PORT_PROP = "java.naming.factory.port";

  public JndiTest1() {
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
	
	
	
	Hashtable env1 = new Hashtable();
	env1.put(NAMING_FACTORY_PROP, NAMING_FACTORY);
	env1.put(NAMING_HOST_PROP, LOCALHOST);
	env1.put(NAMING_PORT_PROP, "16401");
	
	Hashtable env0 = new Hashtable();
	env0.put(NAMING_FACTORY_PROP, NAMING_FACTORY);
	env0.put(NAMING_HOST_PROP, LOCALHOST);
	env0.put(NAMING_PORT_PROP, "16400");
	
	
	InitialContext ctx1 = new InitialContext(env1);
	InitialContext ctx0 = new InitialContext(env0);
	Exception excp=null;
	try{
	    ctx0.bind("/B","B");
	    ctx0.createSubcontext("/A");
	    ctx0.createSubcontext("/A/C");
	    ctx0.createSubcontext("/A/C/F");
	    ctx0.createSubcontext("/A/C/U");
	    ctx0.bind("/A/D","D");
	    ctx0.bind("/A/E","E");
	    ctx0.bind("/A/C/G","G");
	    ctx0.bind("/A/C/F/I","I");
	    
	    Thread.sleep(3000);
	    
	    ctx1.bind("/H","H");
	    ctx1.createSubcontext("/A/C/F/L");
	    ctx1.createSubcontext("/A/C/F/P");
	    ctx1.bind("/A/M","M");
	    ctx1.bind("/A/C/N","N");
	    ctx1.bind("/A/C/F/L/O","O");
	    
	    Thread.sleep(3000);
	}catch(Exception exc){
	    excp=exc;
	}
	assertEquals(null,excp);

	// check element is created with lookup method
	String look = (String) ctx0.lookup("/A/C/F/L/O");
	assertEquals("O",look);
	
	look = (String) ctx0.lookup("/A/C/F/I");
	assertEquals("I",look);
	
	look = (String) ctx1.lookup("/A/C/G");
	assertEquals("G",look);

	look = (String) ctx1.lookup("/A/M");
	assertEquals("M",look);
	Thread.sleep(3000);
    
	try{
	    ctx0.rebind("/A/D","D");
	    ctx1.rebind("/B","B");   // rebind a element bind with ctx0
	    ctx0.rebind("/H","H");   // rebind a element bind with ctx1
	    ctx1.rebind("/A/M","M");
	}catch(Exception exc){
	    excp=exc;
	}
	assertEquals(null,excp);
	
     		
	ctx0.destroySubcontext("/A/C/F/P");
	Thread.sleep(3000);
	// check context destroy
	try{
	    look=((fr.dyade.aaa.jndi2.client.NamingContextImpl) ctx1.lookup("/A/C/F/P")).getNameInNamespace();
	}catch(NameNotFoundException nnfe){
	    excp=nnfe;
	}
	assertTrue(excp instanceof NameNotFoundException);
	
	ctx1.destroySubcontext("/A/C/U");
	Thread.sleep(3000);
	try{
	    look=((fr.dyade.aaa.jndi2.client.NamingContextImpl) ctx0.lookup("/A/C/U")).getNameInNamespace();
	}catch(NameNotFoundException nnfe){
	    excp=nnfe;
	}
	assertTrue(excp instanceof NameNotFoundException);


	ctx1.unbind("/A/D");
	Thread.sleep(3000);
	try{
	    look= (String)ctx0.lookup("/A/D");
	}catch(NameNotFoundException nnfe){
	    excp=nnfe;
	}
	assertTrue(excp instanceof NameNotFoundException);


	ctx0.unbind("/H");
	Thread.sleep(3000);
	try{
	    look= (String)ctx1.lookup("/H");
	}catch(NameNotFoundException nnfe){
	    excp=nnfe;
	}
	assertTrue(excp instanceof NameNotFoundException);

	System.out.println("End test1");
	
    } catch (Exception exc) {   
	exc.printStackTrace();
	error(exc);      
    } finally {
	stopAgentServer((short)0);
	stopAgentServer((short)1);
	endTest();
    }
  }
    
    public static void main(String args[]) {
	new JndiTest1().run();
    }
}
