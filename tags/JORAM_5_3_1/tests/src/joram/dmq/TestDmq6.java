/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C)  2007 - 2009 ScalAgent Distributed Technologies
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
package joram.dmq;

import java.util.List;

import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;

import framework.TestCase;
/**
 * Test : set a default dmq and set a default threshold for user
 *    
 */
public class TestDmq6 extends TestCase {
  public static void main(String[] args) {
    new TestDmq6().run();
  }

  public void run() {
    try {
      boolean defaultCreate = false, dmq = false, dmq1 = false, queue = false, topic = false;
      System.out.println("server start");
      startAgentServer((short)0);

      Thread.sleep(5000);
      AdminModule.connect("localhost", 2560,"root", "root", 60);
      System.out.println("admin ok");

      // check 
      Destination[] list1 = AdminModule.getDestinations();
      for(int i = 0;i < list1.length; i++){
        Destination dest = list1[i];
        if (dest.getAdminName().equals("queue")) {
          assertTrue(dest.isQueue());
          assertTrue(dest.isFreelyWriteable());
          assertTrue(dest.isFreelyReadable());
          assertEquals(2,((Queue)dest).getThreshold());
          queue = true;
        } else if (dest.getAdminName().equals("topic")) {
          assertTrue(dest.isTopic());
          assertTrue(dest.isFreelyWriteable());
          assertTrue(dest.isFreelyReadable());
          topic = true;
        } else if(dest.getAdminName().equals("defaultdmq")){
          List listeI = dest.getReaders();
          assertEquals("dmq",((User)listeI.get(0)).getName());
          assertEquals(dest,AdminModule.getDefaultDMQ());
          assertEquals(10,AdminModule.getDefaultThreshold());
          defaultCreate = true;
        } else if (dest.getAdminName().equals("dmq")){
          List listeI = dest.getReaders();
          assertEquals("dmq",((User)listeI.get(0)).getName());
          dmq = true;
        } else if (dest.getAdminName().equals("dmq1")){
          List listeI = dest.getReaders();
          assertEquals("dmq",((User)listeI.get(0)).getName());
          dmq1 = true;
        }
        
      }

      assertTrue(queue);
      assertTrue(topic);
      assertTrue(defaultCreate);
      assertTrue(dmq);
      assertTrue(dmq1);


      User[] list2 = AdminModule.getUsers();
      for(int i = 0;i < list2.length;i++){
        User user = list2[i];
        if(user.getName().equals("anonymous")){
          assertEquals(2,user.getThreshold());
        }
      }


      AdminModule.disconnect();

    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      System.out.println("Server stop ");
      stopAgentServer((short)0);
      endTest(); 
    }
  }


}
