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

import framework.TestCase;

/**
 * Exception when bind an element (/A/elt) without create subcontext (/A)
 */

public class Test2 extends TestCase {

  public static final int IT_NB = 2;
  public static final int CHILDREN_NB = 2;
  public static final int DEPTH_MAX = 5;

  public static void main(String[] args) {
    new Test2().run();
  }

  public void run() {
    try {

      Hashtable properties = System.getProperties();

      startAgentServer((short) 0, new String[] { "-DTransaction=fr.dyade.aaa.util.NTransaction" });

      Thread.sleep(1000);

      properties.put("java.naming.factory.initial", "fr.dyade.aaa.jndi2.client.NamingContextFactory");
      properties.put("java.naming.factory.host", "localhost");
      properties.put("java.naming.factory.port", "16600");

      Context ctx = new InitialContext();

      Exception excep = null;
      try {
        ctx.bind("/toto/toto", "hello");
      } catch (Exception exc) {
        excep = exc;
      }
      assertTrue(excep != null);

      ctx.createSubcontext("/toto");
      ctx.bind("/toto/toto1", "hello");
      ctx.close();

    } catch (Exception exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      crashAgentServer((short) 0);
      endTest();
    }
  }

}
