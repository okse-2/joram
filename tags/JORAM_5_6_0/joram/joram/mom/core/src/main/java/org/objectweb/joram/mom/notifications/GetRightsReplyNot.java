/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2010 ScalAgent Distributed Technologies
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
package org.objectweb.joram.mom.notifications;

import java.util.Vector;

/**
 * This notification is used by a destination for sending the identifiers of its readers
 * and writers in response to a GetRightsRequest.
 */
public class GetRightsReplyNot extends AdminReplyNot {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;
  
  /** True if all users can read this destination. */
  private boolean isFreeReading;
  /** True if all users can write this destination. */
  private boolean isFreeWriting;
  /** Vector of readers' identifiers. */
  private Vector readers;
  /** Vector of writers' identifiers. */
  private Vector writers;

  /**
   * Constructs a <code>GetRightsReply</code> instance.
   *
   * @param request       The request this reply replies to.
   * @param isFreeReading
   * @param isFreeWriting
   * @param readers       The vector or readers' or writers' identifiers.
   * @param writers       The vector or readers' or writers' identifiers.
   */
  public GetRightsReplyNot(AdminRequestNot request,
                        boolean isFreeReading, boolean isFreeWriting,
                        Vector readers, Vector writers) {
    super(request, true, null);
    this.isFreeReading = isFreeReading;
    this.isFreeWriting = isFreeWriting;
    this.readers = readers;
    this.writers = writers;
  }
  
  /**
   * @return the isFreeReading
   */
  public boolean isFreeReading() {
    return isFreeReading;
  }

  /**
   * @return the isFreeWriting
   */
  public boolean isFreeWriting() {
    return isFreeWriting;
  }

  /**
   * Returns the vector of readers' identifiers.
   * @return the vector of readers' identifiers.
   */
  public Vector getReaders() {
    return readers;
  }
  
  /**
   * Returns the vector of writers' identifiers.
   * @return the vector of writers' identifiers.
   */
  public Vector getWriters() {
    return writers;
  }
}
