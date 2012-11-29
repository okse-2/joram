/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - 2007 ScalAgent Distributed Technologies
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
 * Initial developer(s): Freyssinet Andre (ScalAgent D.T.)
 * Contributor(s): Badolle Fabien (ScalAgent D.T.)
 */
package joram.noreg;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.local.LocalConnectionFactory;

import fr.dyade.aaa.agent.AgentServer;

/**
 * test create queue
 */
public class Test32 extends framework.TestCase{
    public static void main (String args[]) throws Exception {
	new Test32().run();
    }
    public void run(){
	Connection cnx = null;
	try {
	    AgentServer.init((short) 0, "s0", null);
	    AgentServer.start();

	    Thread.sleep(1000L);

	    AdminModule.connect("localhost", 16010, "root", "root", 60);
	    Queue dest = Queue.create("queue");
	    dest.setFreeReading();
	    dest.setFreeWriting();
	    User user = User.create("anonymous", "anonymous", 0);
	    ConnectionFactory cf =  LocalConnectionFactory.create();
	    AdminModule.disconnect();

	    AdminModule.connect("localhost", 16010, "root", "root", 60);
	    dest = Queue.create("queue");
	    AdminModule.disconnect();
	} catch (Throwable exc) {
	    exc.printStackTrace();
	    error(exc);
	}finally{
	    AgentServer.stop();
	    endTest();
	}
    
    }
}
