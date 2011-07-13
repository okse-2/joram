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
import java.util.List;

import fr.dyade.aaa.common.stream.StreamUtil;

/**
 * A <code>ClusterListReply</code> instance holds the identifiers of a cluster's
 * destinations.
 */
public class ClusterListReply extends AdminReply {

  private static final long serialVersionUID = 1L;

  /** Identifiers of the cluster's topics. */
  private List clusteredDest;

  /**
   * Constructs a <code>Monitor_GetClusterRep</code> instance.
   * 
   * @param clusteredDest Identifiers of the cluster's destinations.
   */
  public ClusterListReply(List clusteredDest) {
    super(true, null);
    this.clusteredDest = clusteredDest;
  }

  /** Returns the identifiers of the cluster's topics. */
  public List getDestinations() {
    return clusteredDest;
  }

  public ClusterListReply() {
  }

  protected int getClassId() {
    return LIST_CLUSTER_DEST_REP;
  }

  public void readFrom(InputStream is) throws IOException {
    super.readFrom(is);
    clusteredDest = StreamUtil.readArrayListOfStringFrom(is);
  }

  public void writeTo(OutputStream os) throws IOException {
    super.writeTo(os);
    StreamUtil.writeListOfStringTo(clusteredDest, os);
  }
}
