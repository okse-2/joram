/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - 2007 ScalAgent Distributed Technologies
 * Copyright (C) 2004 - France Telecom R&D
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
package org.objectweb.joram.shared.admin;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.objectweb.joram.shared.stream.StreamUtil;


public class AddQueueCluster extends SpecialAdmin {
  private static final long serialVersionUID = 1L;

  public String joiningQueue;

  /**
   * Adds a queue to a cluster.
   * <p>
   *
   * @param clusterQueue  Queue part of the cluster.
   * @param joiningQueue  Queue joining the cluster.
   *
   */
  public AddQueueCluster(String clusterQueue,
                         String joiningQueue) {
    super(clusterQueue);
    this.joiningQueue = joiningQueue;
  }

  public AddQueueCluster() { }
  
  public String toString() {
    StringBuffer buff = new StringBuffer();
    buff.append("AddQueueCluster (clusterQueue=");
    buff.append(getDestId());
    buff.append(",joiningQueue=");
    buff.append(joiningQueue);
    buff.append(')');
    return buff.toString();
  }
  
  protected int getClassId() {
    return ADD_QUEUE_CLUSTER;
  }

  public void readFrom(InputStream is) throws IOException {
    super.readFrom(is);
    joiningQueue = StreamUtil.readStringFrom(is);
  }

  public void writeTo(OutputStream os) throws IOException {
    super.writeTo(os);
    StreamUtil.writeTo(joiningQueue, os);
  }
}
