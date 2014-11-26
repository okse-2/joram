/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2012 ScalAgent Distributed Technologies
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

/**
 * A <code>SetSyncExceptionOnFullDestRequest</code> instance requests to set the 
 * syncExceptionOnFullDest value in Queue.
 */
public class SetSyncExceptionOnFullDestRequest extends DestinationAdminRequest {
  /** send a Exception if destination is full. */
  private boolean syncExceptionOnFullDest = false;

  /**
   * Constructs a <code>SetSyncExceptionOnFullDestRequest</code> instance.
   *
   * @param id        Identifier of the queue or subscription. 
   * @param syncExceptionOnFullDest  (default false).
   */
  public SetSyncExceptionOnFullDestRequest(String id, boolean syncExceptionOnFullDest) {
    super(id);
    this.syncExceptionOnFullDest = syncExceptionOnFullDest;
  }

  public SetSyncExceptionOnFullDestRequest() { }
  
  protected int getClassId() {
    return SET_SYNC_EXCEPTION_ON_FULL_DEST;
  }
  
  /**
   * @return the syncExceptionOnFullDest
   */
  public boolean isSyncExceptionOnFullDest() {
    return syncExceptionOnFullDest;
  }

  /**
   * @param syncExceptionOnFullDest the syncExceptionOnFullDest to set
   */
  public void setSyncExceptionOnFullDest(boolean syncExceptionOnFullDest) {
    this.syncExceptionOnFullDest = syncExceptionOnFullDest;
  }

  public void readFrom(InputStream is) throws IOException {
    super.readFrom(is);
    syncExceptionOnFullDest = StreamUtil.readBooleanFrom(is);
  }

  public void writeTo(OutputStream os) throws IOException {
    super.writeTo(os);
    StreamUtil.writeTo(syncExceptionOnFullDest, os);
  }
}
