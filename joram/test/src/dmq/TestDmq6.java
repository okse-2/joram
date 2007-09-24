/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C)  2007 ScalAgent Distributed Technologies
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
 * Initial developer(s):Badolle Fabien (ScalAgent D.T.)
 * Contributor(s): 
 */
package dmq;

import framework.*;

import java.util.List;
import java.io.*;

import javax.naming.*;
import org.objectweb.joram.client.jms.admin.*;

import org.objectweb.joram.client.jms.*;
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
	    boolean defaultCreate = false, dmq = false, dmq1 = false;
	    System.out.println("server start");
	    startAgentServer((short)0);
	    
	    Thread.sleep(5000);
	    AdminModule.connect("localhost", 2560,"root", "root", 60);
	    System.out.println("admin ok");
	   
	    // check 
	    List liste = AdminModule.getDestinations();
	    for(int i = 0;i < liste.size(); i++){
		Destination dest = (Destination)liste.get(i);
		
		if(dest.getType().equals("queue")){
		    assertTrue( dest.isFreelyWriteable() );
		    assertTrue( dest.isFreelyReadable() );
		    assertEquals(2,((Queue)dest).getThreshold());
		}else if(dest.getType().equals("topic")){
		    assertTrue( dest.isFreelyWriteable() );
		    assertTrue( dest.isFreelyReadable() );
		}

		if(dest.getAdminName().equals("defaultdmq")){
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
	    
	    assertTrue(defaultCreate);
	    assertTrue(dmq);
	    assertTrue(dmq1);
	    
	    
	    liste = AdminModule.getUsers();
	    for(int i = 0;i < liste.size();i++){
		User user = (User)liste.get(i);
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
