/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2015 ScalAgent Distributed Technologies
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
 * Initial developer(s): ScalAgent D.T.
 * Contributor(s): 
 */
package joram.bridgejms;

import java.util.Properties;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import javax.jms.ConnectionFactory;

import org.objectweb.joram.client.jms.admin.JMSAcquisitionQueue;
import org.objectweb.joram.client.jms.admin.JMSDistributionTopic;

public class AdminTest16x {
  javax.naming.Context jndiCtx = null;
  
  AdminTest16x () throws Exception {
    // Initializes JNDI context
    jndiCtx = new javax.naming.InitialContext();
  }
  
  public void localAdmin(ConnectionFactory cf, String client) throws Exception {
    // Connects to the server
    AdminModule.connect(cf);
    int sid = AdminModule.getLocalServerId();
    
    // Creates the local user
    User.create("anonymous", "anonymous");
    
    // Creates the distribution topic
    Properties props = new Properties();
    props.setProperty("period", "1000");     
    props.setProperty("jms.ConnectionUpdatePeriod", "1000");
    props.setProperty("distribution.async", "true");
    Topic topic1 = JMSDistributionTopic.create(sid, "topic1", "queue1" + client, props);
    topic1.setFreeWriting();
    
    // Creates the acquisition queue
    props.clear();
    props.setProperty("jms.ConnectionUpdatePeriod", "1000");
    props.setProperty("persistent", "true");
    props.setProperty("acquisition.max_msg", "50");
    props.setProperty("acquisition.min_msg", "20");
    props.setProperty("acquisition.max_pnd", "200");
    props.setProperty("acquisition.min_pnd", "50");
    Queue queue2 = JMSAcquisitionQueue.create(sid, "queue2", "queue2" + client, props);
    queue2.setFreeReading();
    
    AdminModule.disconnect();
  }

  public void centralAdmin(ConnectionFactory cf, String clients) throws Exception {
    // Connects to the server
    AdminModule.connect(cf);
    
    jndiCtx.rebind("centralCF", cf);

    // Creates the local user
    User.create("anonymous", "anonymous");
    
    String ctab[] = clients.split(":");
    for (int i=0; i<ctab.length; i++) {
      addClient(ctab[i]);
    }

    jndiCtx.close();
    AdminModule.disconnect();
  }
 
  public void addClient(String client) throws Exception {
    // Creates queue #1 for client
    Queue queue1 = Queue.create("queue1"+ client);
    queue1.setFreeReading();
    queue1.setFreeWriting();
    jndiCtx.rebind("queue1"+ client, queue1);
    System.out.println("creates queue1 = " + queue1);
    
    // Creates queue #2 for client
    Queue queue2 = Queue.create("queue2"+ client);
    queue2.setFreeReading();
    queue2.setFreeWriting();
    jndiCtx.rebind("queue2"+ client, queue2);
    System.out.println("creates queue2 = " + queue2);
  }
}
