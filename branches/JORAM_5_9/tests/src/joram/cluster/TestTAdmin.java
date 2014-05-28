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
import java.util.Iterator;
import java.util.List;

import org.objectweb.joram.client.jms.ConnectionFactory;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminException;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import framework.TestCase;

/**
 *   
 */
public class TestTAdmin extends TestCase {

  public static void main(String[] args) {
    new TestTAdmin().run();
  }

  private void checkCluster(String[] expectedTopics, Topic topic) throws Exception {
    List topics = topic.getClusterFellows();
    String[] topicNames = new String[topics.size()];
    int i = 0;
    for (Iterator iterator = topics.iterator(); iterator.hasNext(); i++) {
      Topic t = (Topic) iterator.next();
      topicNames[i] = t.getName();
    }
    Arrays.sort(expectedTopics);
    Arrays.sort(topicNames);
    assertTrue("Expected: " + Arrays.toString(expectedTopics) + ", got: " + Arrays.toString(topicNames),
        Arrays.equals(expectedTopics, topicNames));
  }

  public void run() {
    try {
      startAgentServer((short) 0);
      startAgentServer((short) 1);
      startAgentServer((short) 2);

      Thread.sleep(2000);

      ConnectionFactory cf0 = TcpConnectionFactory.create("localhost", 16010);

      AdminModule.connect(cf0);

      // Creates a topic on each server
      Topic topic0 = Topic.create(0);
      Topic topic1 = Topic.create(1);
      Topic topic2 = Topic.create(2);
      Topic topic3 = Topic.create(0);
      Topic topic4 = Topic.create(1);
      Topic topic5 = Topic.create(2);
      Topic topic6 = Topic.create(0);


      // Builds the first cluster (0-1-2)
      topic0.addClusteredTopic(topic1);
      topic0.addClusteredTopic(topic2);
      
      // Builds the second cluster (3-4)
      topic3.addClusteredTopic(topic4);

      // Builds the second cluster (5-6)
      topic6.addClusteredTopic(topic5);

      // Check the clusters
      String[] expectedTopics = new String[] { topic0.getName(), topic1.getName(), topic2.getName() };
      checkCluster(expectedTopics, topic0);
      checkCluster(expectedTopics, topic1);
      checkCluster(expectedTopics, topic2);

      expectedTopics = new String[] { topic3.getName(), topic4.getName() };
      checkCluster(expectedTopics, topic3);
      checkCluster(expectedTopics, topic4);

      expectedTopics = new String[] { topic5.getName(), topic6.getName() };
      checkCluster(expectedTopics, topic5);
      checkCluster(expectedTopics, topic6);
      

      // Transitive closure ((0-1-2)-(3-4)-(5-6)) = (0-1-2-3-4-5-6)
      topic1.addClusteredTopic(topic3);
      topic4.addClusteredTopic(topic5);

      // Check the cluster
      expectedTopics = new String[] { topic0.getName(), topic1.getName(), topic2.getName(), topic3.getName(),
          topic4.getName(), topic5.getName(), topic6.getName() };
      checkCluster(expectedTopics, topic0);
      checkCluster(expectedTopics, topic1);
      checkCluster(expectedTopics, topic2);
      checkCluster(expectedTopics, topic3);
      checkCluster(expectedTopics, topic4);
      checkCluster(expectedTopics, topic5);
      checkCluster(expectedTopics, topic6);


      // Leave the cluster (0-1-2-3-4-5-6)/0/6 = (1-2-3-4-5)
      topic0.removeFromCluster();
      topic6.removeFromCluster();

      // Check the cluster
      expectedTopics = new String[] { topic1.getName(), topic2.getName(), topic3.getName(), topic4.getName(),
          topic5.getName() };
      checkCluster(expectedTopics, topic1);
      checkCluster(expectedTopics, topic2);
      checkCluster(expectedTopics, topic3);
      checkCluster(expectedTopics, topic4);
      checkCluster(expectedTopics, topic5);

      checkCluster(new String[] { topic0.getName() }, topic0);
      checkCluster(new String[] { topic6.getName() }, topic6);


      // Delete a topic from the cluster (1-2-3-4-5)/3 = (1-2-4-5)
      topic3.delete();

      // Check the cluster
      expectedTopics = new String[] { topic1.getName(), topic2.getName(), topic4.getName(), topic5.getName() };
      checkCluster(expectedTopics, topic1);
      checkCluster(expectedTopics, topic2);
      checkCluster(expectedTopics, topic4);
      checkCluster(expectedTopics, topic5);


      // Cluster the deleted topic
      Exception expectedException = null;
      try {
        topic1.addClusteredTopic(topic3);
      } catch (AdminException exc) {
        expectedException = exc;
      }
      assertNotNull(expectedException);


      // Remove 2, 4 and 5 from cluster, check 1 is alone.
      topic2.removeFromCluster();
      topic4.removeFromCluster();
      topic5.removeFromCluster();

      checkCluster(new String[] { topic1.getName() }, topic1);
      checkCluster(new String[] { topic2.getName() }, topic2);
      checkCluster(new String[] { topic4.getName() }, topic4);
      checkCluster(new String[] { topic5.getName() }, topic5);

      // Re-cluster 2 and 4
      topic2.addClusteredTopic(topic4);

      expectedTopics = new String[] { topic2.getName(), topic4.getName() };
      checkCluster(expectedTopics, topic2);
      checkCluster(expectedTopics, topic4);

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
