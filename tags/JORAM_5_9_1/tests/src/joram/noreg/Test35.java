/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - 2009 ScalAgent Distributed Technologies
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

import javax.naming.InitialContext;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminModule;

import fr.dyade.aaa.agent.AgentServer;

/**
 *test getadminname
 */
public class Test35 extends BaseTest {
    public final static String queueName = "myQueue";
    public final static String topicName = "myTopic";

    public static void main (String args[]) throws Exception {
	new Test35().run();
    }
    public void run(){
	try{
	    startServer();

	    String baseclass = "joram.noreg.ColocatedBaseTest";
	    baseclass = System.getProperty("BaseClass", baseclass);

        AdminModule.connect(createConnectionFactory(baseclass));

	    Queue queue = Queue.create(queueName);
	    Topic topic = Topic.create(topicName);

	    InitialContext jndi = new InitialContext();
	    jndi.rebind(queueName, queue);
	    jndi.rebind(topicName, topic);

	    //System.out.println(queue.getAdminName());
	    //System.out.println(topic.getAdminName());
	    assertEquals("myQueue",queue.getAdminName());
	    assertEquals("myTopic",topic.getAdminName());
	    
	    Queue queue1 = (Queue) jndi.lookup(queueName);
	    Topic topic1 = (Topic) jndi.lookup(topicName);

	    //System.out.println(queue1.getAdminName());
	    //System.out.println(topic1.getAdminName());
	    assertEquals("myQueue",queue1.getAdminName());
	    assertEquals("myTopic",topic1.getAdminName());

	    AdminModule.disconnect();

	}catch(Throwable exc){
	    exc.printStackTrace();
	    error(exc);
	}finally{
	    AgentServer.stop();
	    endTest();
	}
    }
}
