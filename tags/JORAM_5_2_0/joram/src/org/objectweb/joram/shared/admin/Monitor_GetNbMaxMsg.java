/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2005 - 2007 ScalAgent Distributed Technologies
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

/**
 * A <code>Monitor_GetNbMaxMsg</code> instance requests the
 * NbMaxMsg of the destination.
 */
public class Monitor_GetNbMaxMsg extends Monitor_Request {
  private static final long serialVersionUID = 1L;

  /** Identifier of the destination. */
  private String destId;
  /** subscription name */
  private String subName = null;
  
  /**
   * Constructs a <code>Monitor_GetNbMaxMsg</code> instance.
   *
   * @param destId  Identifier of the destination.
   */
  public Monitor_GetNbMaxMsg(String destId) {
    this.destId = destId;
  }
  
  public Monitor_GetNbMaxMsg() { }

  /**
   * Constructs a <code>Monitor_GetNbMaxMsg</code> instance.
   *
   * @param destId  Identifier of the destination.
   * @param subName Subscription name.
   */
  public Monitor_GetNbMaxMsg(String destId,
                             String subName) {
    this.destId = destId;
    this.subName = subName;
  }

  /** Returns the identifier of the destination. */
  public String getId() {
    return destId;
  }

  /** Returns SubName */
  public String getSubName() {
    return subName;
  }
  
  protected int getClassId() {
    return MONITOR_GET_NB_MAX_MSG;
  }
  
  public void readFrom(InputStream is) throws IOException {
    super.readFrom(is);
    destId = StreamUtil.readStringFrom(is);
    subName = StreamUtil.readStringFrom(is);
  }

  public void writeTo(OutputStream os) throws IOException {
    super.writeTo(os);
    StreamUtil.writeTo(destId, os);
    StreamUtil.writeTo(subName, os);
  }
}
