/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2015 ScalAgent Distributed Technologies
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
 * A <code>GetJMXAttsRequest</code> instance requests the dump of selected
 * JMX attributes server side.
 */
public class GetJMXAttsRequest extends AdminRequest {
  private static final long serialVersionUID = 1L;

  /** Identifier of the destination. */
  private String destId;
  /** The comma separated list of requested JMX attribute names. */
  public String attributes;

  /**
   * Constructs a <code>Monitor_GetStat</code> instance.
   *
   * @param destId  Identifier of the destination.
   */
  public GetJMXAttsRequest(String destId) {
    this.destId = destId;
    this.attributes = null;
  }
  
  /**
   * Constructs a <code>Monitor_GetStat</code> instance.
   *
   * @param destId      Identifier of the destination.
   * @param attributes  A comma separated list of requested JMX attribute names.
   */
  public GetJMXAttsRequest(String destId, String attributes) {
    this.destId = destId;
    this.attributes = attributes;
  }

  public GetJMXAttsRequest() { }
  
  /** Returns the identifier of the destination. */
  public String getDest() {
    return destId;
  }
  
  /** Returns the comma separated list of requested JMX attribute names. */
  public String[] getAttributes() {
    return attributes.split(";");
  }
  
  protected int getClassId() {
    return MONITOR_GET_JMX_ATTS;
  }
  
  public void readFrom(InputStream is) throws IOException {
    destId = StreamUtil.readStringFrom(is);
    attributes = StreamUtil.readStringFrom(is);
  }

  public void writeTo(OutputStream os) throws IOException {
    StreamUtil.writeTo(destId, os);
    StreamUtil.writeTo(attributes, os);
  }
}
