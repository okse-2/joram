/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - 2006 ScalAgent Distributed Technologies
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

import java.util.Vector;


/**
 * A <code>Monitor_GetClusterRep</code> instance holds the identifiers of
 * a cluster's topics.
 */
public class Monitor_GetClusterRep extends Monitor_Reply {
  private static final long serialVersionUID = 8073241648201291805L;

  /** Identifiers of the cluster's topics. */
  private Vector topics;
  
  /**
   * Constructs a <code>Monitor_GetClusterRep</code> instance.
   *
   * @param fatherId  Identifiers of the cluster's topics.
   */
  public Monitor_GetClusterRep(Vector topics) {
    this.topics = topics;
  }

  /** Returns the identifiers of the cluster's topics. */
  public Vector getTopics() {
    if (topics == null)
      return new Vector();
    return topics;
  }
}
