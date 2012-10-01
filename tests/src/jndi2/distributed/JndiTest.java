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
 * Test using createSubcontext.
 * 
 *
 */
public class JndiTest extends TestCase {
    public static String NAMING_FACTORY_PROP = "java.naming.factory.initial";
    public static String NAMING_FACTORY = "fr.dyade.aaa.jndi2.client.NamingContextFactory";
    public static String NAMING_HOST_PROP = "java.naming.factory.host";
    public static String LOCALHOST = "localhost";
    public static String NAMING_PORT_PROP = "java.naming.factory.port";

    public JndiTest() {
	super();
    }

    public void run() {
	try {
	    startAgentServer(
			     (short)0, new String[]{"-DTransaction=fr.dyade.aaa.util.NTransaction"});
	    startAgentServer(
			     (short)1, new String[]{"-DTransaction=fr.dyade.aaa.util.NTransaction"});
	    startAgentServer(
			     (short)2, new String[]{"-DTransaction=fr.dyade.aaa.util.NTransaction"});
    
	    Hashtable env0 = new Hashtable();
	    env0.put(NAMING_FACTORY_PROP, NAMING_FACTORY);
	    env0.put(NAMING_HOST_PROP, LOCALHOST);
	    env0.put(NAMING_PORT_PROP, "16600");

	    Hashtable env1 = new Hashtable();
	    env1.put(NAMING_FACTORY_PROP, NAMING_FACTORY);
	    env1.put(NAMING_HOST_PROP, LOCALHOST);
	    env1.put(NAMING_PORT_PROP, "16601");

	    Hashtable env2 = new Hashtable();
	    env2.put(NAMING_FACTORY_PROP, NAMING_FACTORY);
	    env2.put(NAMING_HOST_PROP, LOCALHOST);
	    env2.put(NAMING_PORT_PROP, "16602");
    
	    InitialContext ctx0 = new InitialContext(env0);
	    InitialContext ctx1 = new InitialContext(env1);
	    InitialContext ctx2 = new InitialContext(env2);
      
	    Thread.sleep(3000);
	  
	    ctx1.createSubcontext("/A");
	    ctx1.lookup("/A");
	    ctx1.createSubcontext("/A/B");
	    ctx1.lookup("/A/B");

	    Exception excp=null;
	    try{
		Context ctx3= (Context) ctx0.lookup("/A");
		ctx3.lookup("/B");
	    }catch(Exception exc){
		excp=exc;
	    }
	    assertTrue(excp==null);
      
	    ctx0.lookup("/A");
	    ctx0.createSubcontext("/A/C");
	    ctx0.lookup("/A/C");

      
	    ctx1.lookup("/A/C");

	    ctx2.bind("/A/F1", "F1");
	    assertEquals("F1", ctx0.lookup("/A/F1"));
	    ctx0.unbind("/A/F1");

	    ctx2.destroySubcontext("/A/C");
	    ctx0.destroySubcontext("/A/B");
	    ctx2.destroySubcontext("/A");
      
	    Thread.sleep(3000);

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
	new JndiTest().run();
    }
}
