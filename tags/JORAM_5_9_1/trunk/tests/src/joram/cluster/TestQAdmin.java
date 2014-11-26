/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C)  2009 ScalAgent Distributed Technologies
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
 * Initial developer(s):ScalAgent Distributed Technologies
 * Contributor(s): 
 */
package joram.cluster;

import java.util.Arrays;

import org.objectweb.joram.client.jms.ConnectionFactory;
import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminException;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import framework.TestCase;

/**
 *   
 */
public class TestQAdmin extends TestCase {

  public static void main(String[] args) {
    new TestQAdmin().run();
  }

  private void checkCluster(String[] expectedQueues, Queue queue) throws Exception {
    String[] queues = queue.getQueueClusterElements();
    Arrays.sort(expectedQueues);
    Arrays.sort(queues);
    assertTrue("Expected: " + Arrays.toString(expectedQueues) + ", got: " + Arrays.toString(queues),
        Arrays.equals(expectedQueues, queues));
  }

  public void run() {
    try {
      startAgentServer((short) 0);
      startAgentServer((short) 1);
      startAgentServer((short) 2);

      Thread.sleep(2000);

      ConnectionFactory cf0 = TcpConnectionFactory.create("localhost", 16010);

      AdminModule.connect(cf0);

      // Creates a queue on each server
      Queue queue0 = Queue.create(0, null, Destination.CLUSTER_QUEUE, null);
      Queue queue1 = Queue.create(1, null, Destination.CLUSTER_QUEUE, null);
      Queue queue2 = Queue.create(2, null, Destination.CLUSTER_QUEUE, null);
      Queue queue3 = Queue.create(0, null, Destination.CLUSTER_QUEUE, null);
      Queue queue4 = Queue.create(1, null, Destination.CLUSTER_QUEUE, null);
      Queue queue5 = Queue.create(2, null, Destination.CLUSTER_QUEUE, null);
      Queue queue6 = Queue.create(0, null, Destination.CLUSTER_QUEUE, null);


      // Builds the first cluster (0-1-2)
      queue0.addClusteredQueue(queue1);
      queue0.addClusteredQueue(queue2);
      
      // Builds the second cluster (3-4)
      queue3.addClusteredQueue(queue4);

      // Builds the second cluster (5-6)
      queue6.addClusteredQueue(queue5);

      // Check the clusters
      String[] expectedQueues = new String[] { queue0.getName(), queue1.getName(), queue2.getName() };
      checkCluster(expectedQueues, queue0);
      checkCluster(expectedQueues, queue1);
      checkCluster(expectedQueues, queue2);

      expectedQueues = new String[] { queue3.getName(), queue4.getName() };
      checkCluster(expectedQueues, queue3);
      checkCluster(expectedQueues, queue4);

      expectedQueues = new String[] { queue5.getName(), queue6.getName() };
      checkCluster(expectedQueues, queue5);
      checkCluster(expectedQueues, queue6);
      

      // Transitive closure ((0-1-2)-(3-4)-(5-6)) = (0-1-2-3-4-5-6)
      queue1.addClusteredQueue(queue3);
      queue4.addClusteredQueue(queue5);

      // Check the cluster
      expectedQueues = new String[] { queue0.getName(), queue1.getName(), queue2.getName(), queue3.getName(),
          queue4.getName(), queue5.getName(), queue6.getName() };
      checkCluster(expectedQueues, queue0);
      checkCluster(expectedQueues, queue1);
      checkCluster(expectedQueues, queue2);
      checkCluster(expectedQueues, queue3);
      checkCluster(expectedQueues, queue4);
      checkCluster(expectedQueues, queue5);
      checkCluster(expectedQueues, queue6);


      // Leave the cluster (0-1-2-3-4-5-6)/0/6 = (1-2-3-4-5)
      queue0.removeFromCluster();
      queue6.removeFromCluster();

      // Check the cluster
      expectedQueues = new String[] { queue1.getName(), queue2.getName(), queue3.getName(), queue4.getName(),
          queue5.getName() };
      checkCluster(expectedQueues, queue1);
      checkCluster(expectedQueues, queue2);
      checkCluster(expectedQueues, queue3);
      checkCluster(expectedQueues, queue4);
      checkCluster(expectedQueues, queue5);

      checkCluster(new String[] { queue0.getName() }, queue0);
      checkCluster(new String[] { queue6.getName() }, queue6);


      // Delete a queue from the cluster (1-2-3-4-5)/3 = (1-2-4-5)
      queue3.delete();

      // Check the cluster
      expectedQueues = new String[] { queue1.getName(), queue2.getName(), queue4.getName(), queue5.getName() };
      checkCluster(expectedQueues, queue1);
      checkCluster(expectedQueues, queue2);
      checkCluster(expectedQueues, queue4);
      checkCluster(expectedQueues, queue5);


      // Cluster the deleted queue
      Exception expectedException = null;
      try {
        queue1.addClusteredQueue(queue3);
      } catch (AdminException exc) {
        expectedException = exc;
      }
      assertNotNull(expectedException);

      
      // Remove 2, 4 and 5 from cluster, check 1 is alone.
      queue2.removeFromCluster();
      queue4.removeFromCluster();
      queue5.removeFromCluster();

      checkCluster(new String[] { queue1.getName() }, queue1);
      checkCluster(new String[] { queue2.getName() }, queue2);
      checkCluster(new String[] { queue4.getName() }, queue4);
      checkCluster(new String[] { queue5.getName() }, queue5);

      // Re-cluster 2 and 4
      queue2.addClusteredQueue(queue4);

      expectedQueues = new String[] { queue2.getName(), queue4.getName() };
      checkCluster(expectedQueues, queue2);
      checkCluster(expectedQueues, queue4);

      AdminModule.disconnect();

    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      stopAgentServer((short) 0);
      stopAgentServer((short) 1);
      stopAgentServer((short) 2);
      endTest();
    }
  }
}
