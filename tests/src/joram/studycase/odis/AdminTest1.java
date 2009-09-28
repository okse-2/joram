/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - 2004 ScalAgent Distributed Technologies
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
 * Contributor(s):
 */
package joram.studycase.odis;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Topic;

/**
 *
 */
public class AdminTest1 {

  static final Queue createQueue(String name, javax.naming.Context jndiCtx) throws Exception {
    // Creating queue.
    Queue queue = Queue.create(0, name);
    // Setting access rights.
    queue.setFreeReading();
    queue.setFreeWriting();
    // binding in JNDI if needed.
    if (jndiCtx != null) jndiCtx.rebind(name, queue);

    return queue;
  }

  static final Topic createTopic(String name, javax.naming.Context jndiCtx) throws Exception {
    // Creating prettyTopic.
    Topic topic = Topic.create(0, name);
    // Setting access rights.
    topic.setFreeReading();
    topic.setFreeWriting();
    // binding in JNDI if needed.
    if (jndiCtx != null) jndiCtx.rebind(name, topic);

    return topic;
  }
}
