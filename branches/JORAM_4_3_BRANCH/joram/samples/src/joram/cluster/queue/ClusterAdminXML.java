/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2005 - ScalAgent Distributed Technologies
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
 * Initial developer(s): Nicolas Tachker (ScalAgent)
 * Contributor(s):
 */
package cluster.queue;

import org.objectweb.joram.client.jms.admin.*;

import java.util.Hashtable;

/**
 * Administers three agent servers for the cluster sample.
 */
public class ClusterAdminXML {
  public static void main(String[] args) throws Exception {
    System.out.println();
    System.out.println("Cluster administration...");

    AdminModule.executeXMLAdmin("joramAdmin.xml");

    javax.naming.InitialContext jndiCtx2 = new javax.naming.InitialContext();

    Hashtable h = new Hashtable();
    h.put("0",jndiCtx2.lookup("queue0"));
    h.put("1",jndiCtx2.lookup("queue1"));
    h.put("2",jndiCtx2.lookup("queue2"));

    ClusterQueue clusterQueue = new ClusterQueue(h);
    System.out.println("clusterQueue = " + clusterQueue);
    jndiCtx2.bind("clusterQueue", clusterQueue);


    clusterQueue = (ClusterQueue) jndiCtx2.lookup("clusterQueue");
    System.out.println("clusterQueue = " + clusterQueue);
    jndiCtx2.close();

    System.out.println("Admins closed.");
  }
}
