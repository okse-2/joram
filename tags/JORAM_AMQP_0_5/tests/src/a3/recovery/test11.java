/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C)  2001 - 2009 ScalAgent Distributed Technologies
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
 * Initial developer(s): ScalAgent Distributed Technologies
 * Contributor(s): 
 */
package a3.recovery;

import fr.dyade.aaa.agent.AgentServer;
import framework.BaseTestCase;

public class test11 extends BaseTestCase {
  public test11() {
    super();
    timeout = 60000L;
  }

  public static void main(String args[]) {
    new test11().runTest(args);
  }
  
  protected void startTest() throws Exception {
    System.out.println("coucou");
    
    try {
      byte[] buf = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};

      AgentServer.init(new String[] {"0", "./s0"});
      AgentServer.start();

      System.out.println("coucou");

      AgentServer.getTransaction().createByteArray(buf, "M.SimpleQueue0");
      AgentServer.getTransaction().begin();
      AgentServer.getTransaction().commit(true);

      AgentServer.stop();
      AgentServer.reset();

      AgentServer.init(new String[] {"0", "./s0"});
      AgentServer.start();

      byte[] buf2 = AgentServer.getTransaction().loadByteArray("M.SimpleQueue0");
      assertTrue("Cannot load M.SimpleQueue0", (buf2 != null));
      
//      AgentServer.getTransaction().delete("M.SimpleQueue0");
      AgentServer.getTransaction().begin();
      AgentServer.getTransaction().commit(true);

      AgentServer.getTransaction().createByteArray(buf, "M.SimpleQueue1");
      AgentServer.getTransaction().begin();
      AgentServer.getTransaction().commit(true);

      AgentServer.stop();
      AgentServer.reset();

      AgentServer.init(new String[] {"0", "./s0"});
      AgentServer.start();

      buf2 = AgentServer.getTransaction().loadByteArray("M.SimpleQueue1");
      assertTrue("Cannot load M.SimpleQueue1", (buf2 != null));
      
      AgentServer.stop();
      AgentServer.reset();
      
      endTest();
    } catch (Exception exc) {
      exc.printStackTrace();
      throw exc;
    }
    
  }
}
