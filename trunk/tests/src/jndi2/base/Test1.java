/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2002 - 2010 ScalAgent Distributed Technologies
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

package jndi2.base;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.NotContextException;

import framework.TestCase;


/**
 *
 * test :
 *     - Bind and rebind a name
 *     - Invalid name binding
 *     - Invalid context creation
 *     - Bind a name to the sub context
 *     - Unbind a context
 *     - List a record
 *     - Check the persistent storage 
 */
public class Test1 extends TestCase {

  public static final String OA1 = "oa1";
  public static final String OA2 = "oa2";
  public static final String OA_ERROR = " ";
  public static final String CA_ERROR = "  ";
  public static final String CA1 = "ca1";
  public static final String CA2 = "ca2";
  public static final String OB1 = "ob1";  
  public static final String FORMER_OA1_VAL = new String("FORMER_OA1_VAL");
  public static final String OA1_VAL = new String("OA1_VAL");
  public static final String OA2_VAL = new String("OA2_VAL");
  public static final String OB1_VAL = new String("OB1_VAL");
  
  public static void main(String[] args) {
    new Test1().run();
  }

  public void run() {
    try {

      Hashtable properties = System.getProperties();
      properties.put("java.naming.factory.initial", "fr.dyade.aaa.jndi2.client.NamingContextFactory");
      properties.put("java.naming.factory.host", "localhost");
      properties.put("java.naming.factory.port", "16600");
    
      startAgentServer((short) 0, new String[] { "-DTransaction.UseLockFile=false",
          "-DTransaction=fr.dyade.aaa.util.NTransaction" });

      Thread.sleep(1000);

      Context ctx = new InitialContext();

      // - Bind and rebind a name to the root context

      try {
        ctx.bind(OA1, FORMER_OA1_VAL);
        Object obj = ctx.lookup(OA1);
        assertTrue("Bind-lookup failure", obj.equals(FORMER_OA1_VAL));
      } catch (NamingException exc) {
        error(exc);
      }

      try {
        ctx.rebind(OA1, OA1_VAL);
        Object obj = ctx.lookup(OA1);
        assertTrue("Bind-lookup failure", obj.equals(OA1_VAL));
      } catch (NamingException exc) {
        error(exc);
      }

      try {
        // slashed names
        ctx.bind("//" + OA2, OA2_VAL);
        Object obj = ctx.lookup(OA2);
        assertTrue("Bind-lookup failure", obj.equals(OA2_VAL));
      } catch (NamingException exc) {
        error(exc);
      }

      // - Invalid name binding

      NamingException expectedExc = null;
      try {
        ctx.bind(OA_ERROR, OA1_VAL);
      } catch (NamingException exc) {
        expectedExc = exc;
      }
      assertTrue("Expected bind failure (root bind)", 
                 expectedExc != null);

      // - Invalid context creation

      expectedExc = null;
      try {
        ctx.createSubcontext(CA_ERROR);
      } catch (NamingException exc) {
        expectedExc = exc;
      }
      assertTrue("Expected context creation failure (root creation)", 
                 expectedExc != null);

      // - Create a sub context

      try {
        ctx.createSubcontext(CA1);
        Context subCtx = (Context)ctx.lookup(CA1);
        assertTrue("Create subcontext failure", 
                   subCtx.getNameInNamespace().equals(CA1));
      } catch (ClassCastException exc) {
        error(exc);
      } catch (NamingException exc2) {
        error(exc2);
      }

      // - Bind a name to the sub context

      try {
        Context subCtx = (Context)ctx.lookup(CA1);
        subCtx.bind(OB1, OB1_VAL);
        Object obj = subCtx.lookup(OB1);
        assertTrue("Bind-lookup failure", obj.equals(OB1_VAL));
      } catch (NamingException exc) {
        error(exc);
      }

      // - Unbind a context

      expectedExc = null;
      try {
        ctx.unbind(CA1);        
      } catch (NamingException exc) {
        expectedExc = exc;
      }
      assertTrue("Expected unbind failure", expectedExc != null);

      // - List the root context

      try {
        NamingEnumeration enumeration = ctx.list("");
        int length = 0;
        while (enumeration.hasMoreElements()) {          
          length++;
          ((NameClassPair)enumeration.nextElement()).getName();
        }
        assertTrue("List failure (length=" + length + ')', 
                   length == 3);
      } catch (NamingException exc) {
        error(exc);
      }

      // - List a record

      try {
        NamingEnumeration enumeration = ctx.list(OA1);
      } catch (NotContextException exc) {
        expectedExc = exc;
      } catch (NamingException exc) {
        error(exc);
      }
      assertTrue("Expected list failure (record)", 
                 expectedExc != null);

      killAgentServer((short)0);
        
      Thread.sleep(1000);
      
      startAgentServer((short) 0, new String[] { "-DTransaction.UseLockFile=false",
          "-DTransaction=fr.dyade.aaa.util.ATransaction" });
      
      Thread.sleep(1000);

      ctx = new InitialContext();

      // - Check the persistent storage

      try {
        Object obj = ctx.lookup(OA1);
        assertTrue("Lookup failure", obj.equals(OA1_VAL));

        obj = ctx.lookup(OA2);
        assertTrue("Lookup failure", obj.equals(OA2_VAL));

        obj = ctx.lookup(CA1 + '/' + OB1);
        assertTrue("Lookup failure", obj.equals(OB1_VAL));

        Context subCtx = (Context)ctx.lookup(CA1);
        obj = subCtx.lookup(OB1);
        assertTrue("Lookup failure", obj.equals(OB1_VAL));
      } catch (NamingException exc) {
        error(exc);
      }
      
      // - Unbind a record

      try {
        ctx.unbind(CA1 + '/' + OB1);        
      } catch (NamingException exc) {
        error(exc);
      }

      // - Destroy a context
      
      try {
        ctx.destroySubcontext(CA1);
      } catch (NamingException exc) {
        error(exc);
      }

      // - Lookup a destroyed context

      expectedExc = null;
      try {
        Context subCtx = (Context)ctx.lookup(CA1);
      } catch (NameNotFoundException exc) {
        expectedExc = exc;
      }
      assertTrue("Expected lookup failure", expectedExc != null);

      // - List a destroyed context

      try {
        NamingEnumeration enumeration = ctx.list(CA1);
      } catch (NameNotFoundException exc) {
        expectedExc = exc;
      } catch (NamingException exc) {
        error(exc);
      }
      assertTrue("Expected list failure (destroyed context)", 
                 expectedExc != null);

      killAgentServer((short)0);
        
      Thread.sleep(1000);
      
      startAgentServer((short) 0, new String[] { "-DTransaction.UseLockFile=false",
          "-DTransaction=fr.dyade.aaa.util.NTransaction" });
      
      Thread.sleep(1000);

      ctx = new InitialContext();

      // - Destroy idempotency
      
      try {
        ctx.destroySubcontext(CA1);
      } catch (NamingException exc) {
        error(exc);
      }

      killAgentServer((short)0);

      endTest();
    } catch (Exception exc2) {
      killAgentServer((short) 0);
      exc2.printStackTrace();
      error(exc2);
      endTest();
    }
  }
}
