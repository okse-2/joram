/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - 2010 ScalAgent Distributed Technologies
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

import fr.dyade.aaa.common.stream.StreamUtil;


public class ClusterAdd extends DestinationAdminRequest {

  private static final long serialVersionUID = 1L;

  private String addedDest;

  /**
   * Adds a destination to a cluster.
   * <p>
   * 
   * @param clusteredDest Destination part of the cluster.
   * @param addedDest Destination joining the cluster.
   */
  public ClusterAdd(String clusteredDest, String addedDest) {
    super(clusteredDest);
    this.addedDest = addedDest;
  }

  public ClusterAdd() { }
  
  public String toString() {
    StringBuffer buff = new StringBuffer();
    buff.append("ClusterAdd (clusteredDest=");
    buff.append(getDestId());
    buff.append(",joiningDest=");
    buff.append(addedDest);
    buff.append(')');
    return buff.toString();
  }
  
  protected int getClassId() {
    return ADD_DESTINATION_CLUSTER;
  }

  /**
   * @return the joiningDest
   */
  public String getAddedDest() {
    return addedDest;
  }

  public void readFrom(InputStream is) throws IOException {
    super.readFrom(is);
    addedDest = StreamUtil.readStringFrom(is);
  }

  public void writeTo(OutputStream os) throws IOException {
    super.writeTo(os);
    StreamUtil.writeTo(addedDest, os);
  }
}
