/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - 2010 ScalAgent Distributed Technologies
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
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s): ScalAgent Distributed Technologies
 */
package org.objectweb.joram.shared.admin;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import fr.dyade.aaa.common.stream.StreamUtil;

/**
 * A <code>Monitor_GetCluster</code> instance requests the list of the topics
 * part of a cluster.
 */
public class GetClusterRequest extends AdminRequest {
  private static final long serialVersionUID = 1L;

  /** Identifier of a topic part of the target cluster. */
  private String topic;
  
  /**
   * Constructs a <code>Monitor_GetCluster</code> instance.
   *
   * @param topic  Identifier of a topic part of the target cluster.
   */
  public GetClusterRequest(String topic) {
    this.topic = topic;
  }

  public GetClusterRequest() { }
  
  /** Returns the identifier of a topic part of the target cluster. */
  public String getTopic() {
    return topic;
  }
  
  protected int getClassId() {
    return MONITOR_GET_CLUSTER;
  }
  
  public void readFrom(InputStream is) throws IOException {
    topic = StreamUtil.readStringFrom(is);
  }

  public void writeTo(OutputStream os) throws IOException {
    StreamUtil.writeTo(topic, os);
  }
}
