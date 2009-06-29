/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2009 ScalAgent Distributed Technologies
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
 * Initial developer(s):  ScalAgent Distributed Technologies
 * Contributor(s): 
 */
package joram.reconf;

import java.io.File;

import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;

/**
 * Tests basic server reconfiguration: 2 initial servers in a domain
 *  - Successively adds N server in the domain D0.
 *  
 *  - Adds a third server (S3) in the domain D0, then removes it.
 *  - Removes S2 and the useless dommain (D0).
 *  
 * This test works with classic networks: SimpleNetwork, PoolNetwork, etc.
 */
public class ReconfTest9 extends ReconfTestBase {

  public static void main(String[] args) {
    new ReconfTest9().run();
  }

  public void run() {
    try {
      startAgentServer((short) 0, (File) null, new String[0]);
      startAgentServer((short) 1, (File) null, new String[0]);

      Thread.sleep(1000L);

      AdminModule.connect("localhost", 2560, "root", "root", 60);
      User.create("anonymous", "anonymous", 0);

      checkQueue((short) 0);
      checkQueue((short) 1);

      long start, end;
      for (int i=2; i<20; i++) {
        start = System.currentTimeMillis();
        AdminModule.addServer(i, "localhost", "D0", 17771 + i, "s" + i);
        deployAgentServer((short) i, "./s" + i);
        end = System.currentTimeMillis();
        
        System.out.println("Adds server#" + i + "-> " + (end - start));
      }
      
//      start = System.currentTimeMillis();
//      for (int i=2; i<10; i++) {
//        startAgentServer((short) i, new File("./s" + i), new String[0]);
//      }
//      end = System.currentTimeMillis();
//      System.out.println("starts servers -> " + (end - start));
//      
//      for (int i=0; i<10; i++) {      
//        start = System.currentTimeMillis();
//        checkQueue((short) i);
//        end = System.currentTimeMillis();
//        
//        System.out.println("checkQueue#" + i + "-> " + (end - start));
//      }
//
//      for (int i=2; i<10; i++) {
//        // First stop the server because it must be reachable in order to be stopped.
//        AdminModule.stopServer(i);
//        // Then clean the configuration: the server is not reachable anymore.
//        AdminModule.removeServer(i);
//      }
//      
//      checkQueue((short) 0);
//      checkQueue((short) 1);
//
//      for (int i=2; i<10; i++) {
//        start = System.currentTimeMillis();
//        AdminModule.addServer(i, "localhost", "D0", 17771 + i, "s" + i);
//        deployAgentServer((short) i, "./s" + i);
//        end = System.currentTimeMillis();
//        
//        startAgentServer((short) i, new File("./s" + i), new String[0]);
//        System.out.println("Adds server#" + i + "-> " + (end - start));
//      }
//      
//      for (int i=0; i<10; i++) {      
//        start = System.currentTimeMillis();
//        checkQueue((short) i);
//        end = System.currentTimeMillis();
//        
//        System.out.println("checkQueue#" + i + "-> " + (end - start));
//      }
//
//      for (int i=2; i<10; i++) {
//        // First stop the server because it must be reachable in order to be stopped.
//        AdminModule.stopServer(i);
//        // Then clean the configuration: the server is not reachable anymore.
//        AdminModule.removeServer(i);
//      }
//      
//      checkQueue((short) 1);
//
//      
//      
//      
////      AdminModule.addServer(3, "localhost", "D0", 17773, "./s3");
////      deployAgentServer((short) 3, "s3");
////      startAgentServer((short) 3, new File("./s3"), new String[0]);
////
////      checkQueue((short) 3);
////      checkQueue((short) 1);
////
////      // First stop the server because it must be reachable in order to be stopped.
////      AdminModule.stopServer(3);
////      // Then clean the configuration: the server is not reachable anymore.
////      AdminModule.removeServer(3);
////
////      checkQueue((short) 1);
//
      AdminModule.stopServer(1);
      AdminModule.removeServer(1);

//      AdminModule.removeDomain("D0");

      checkQueue((short) 0);
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
