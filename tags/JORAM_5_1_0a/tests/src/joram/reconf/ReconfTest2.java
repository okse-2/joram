/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2006 - 2008 ScalAgent Distributed Technologies
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
 * Initial developer(s):  ScalAgent D.T.
 * Contributor(s): Badolle Fabien (ScalAgent D.T.)
 */
package joram.reconf;

import java.io.File;

import joram.framework.TestCase;

import org.objectweb.joram.client.jms.admin.AdminException;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.NameAlreadyUsedException;
import org.objectweb.joram.client.jms.admin.StartFailureException;
import org.objectweb.joram.client.jms.admin.User;

/**
 * Testing: server reconfiguration
 */
public class ReconfTest2 extends TestCase {

  public static void main(String[] args) {
    new ReconfTest2().run();
  }

  public void run() {
    try {
      startAgentServer((short) 0, (File) null,
          new String[] { "-DTransaction=fr.dyade.aaa.util.NullTransaction" });

      AdminModule.connect("localhost", 2560, "root", "root", 60);

      User.create("anonymous", "anonymous", 0);

      //System.out.println("Add domain D0");

      Exception e = null;
      try {
        AdminModule.addDomain("D0", 0, 99999999);
      } catch (Exception exc) {
        //System.out.println("Expected error: " + exc);
        e = exc;
      }
      assertTrue("Exception expected: port out of range", e instanceof StartFailureException);

      //System.out.println("Retry adding domain D0");
      AdminModule.addDomain("D0", 0, 17770);

      Exception e2 = null;
      // System.out.println("Retry adding domain D0");
      try {
        AdminModule.addDomain("D0", 0, 17770);
      } catch (Exception exc) {
        //System.out.println("Expected error: " + exc);
        e2 = exc;
      }
      assertTrue("Exception expected: domain name already used", e2 instanceof NameAlreadyUsedException);

      //System.out.println("Add server s1");
      AdminModule.addServer(1, "localhost", "D0", 17771, "s1");

      Exception e3 = null;
      //System.out.println("try to remove D0");
      try {
        AdminModule.removeDomain("D0");
      } catch (Exception exc) {
        //System.out.println("Expected error: " + exc);
        e3 = exc;
      }
      assertTrue("Exception expected: domain contains more than 1 server", e3 instanceof AdminException);

      ReconfTest.startServer((short) 1, "s1");

      ReconfTest.checkQueue((short) 1);

      //System.out.println("Add domain D1");
      AdminModule.addDomain("D1", 1, 18770);

      //System.out.println("Add server s2");
      AdminModule.addServer(2, "localhost", "D1", 18771, "s2");

      ReconfTest.startServer((short) 2, "s2");

      ReconfTest.checkQueue((short) 2);

      //System.out.println("Remove server s1");
      Exception e4 = null;
      try {
        AdminModule.removeServer(1);
      } catch (Exception exc) {
        //System.out.println("Expected error: " + exc);
        e4 = exc;
      }
      assertTrue("Exception expected: server belongs to more than 1 domain", e4 instanceof AdminException);

      // First stop the server because it must be reachable
      // in order to be stopped.
      //System.out.println("Stop server s2");
      AdminModule.stopServer(2);
      //System.out.println("Server s2 stopped");

      // Then clean the configuration: 
      // the server is not reachable
      // anymore.
      //System.out.println("Remove server s2");
      AdminModule.removeServer(2);

      //System.out.println("Remove domain D1");
      AdminModule.removeDomain("D1");

      //System.out.println("Stop server s1");
      AdminModule.stopServer(1);
      //System.out.println("Server s1 stopped");

      //System.out.println("Remove server s1");
      AdminModule.removeServer(1);

      //System.out.println("Remove domain D0");
      AdminModule.removeDomain("D0");
      
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      System.out.println("Stop server s0");
      stopAgentServer((short) 0);
      endTest();
    }
  }

}
