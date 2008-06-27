/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - 2007 ScalAgent Distributed Technologies
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

import org.objectweb.joram.shared.stream.StreamUtil;


/**
 * A <code>Monitor_GetDMQSettingsRep</code> instance holds the dead message
 * queue and threshold settings of a server, a destination or a user.
 */
public class Monitor_GetDMQSettingsRep extends Monitor_Reply {
  private static final long serialVersionUID = 1L;

  /** DMQ identifier. */
  private String dmqId;
  /** Threshold. */
  private Integer threshold;

  /**
   * Constructs a <code>Monitor_GetDMQSettingsRep</code> instance.
   *
   * @param dmqId  DMQ identifier.
   * @param threshold  Threshold.
   */
  public Monitor_GetDMQSettingsRep(String dmqId, Integer threshold) {
    this.dmqId = dmqId;
    if (threshold != null)
      this.threshold = threshold;
    else
      this.threshold = new Integer(-1);
  }

  public Monitor_GetDMQSettingsRep() { }
  
  /** Returns the DMQ identifier. */
  public String getDMQName() {
    return dmqId;
  }
  
  /** Returns the threshold. */
  public Integer getThreshold() {
    return threshold;
  }
  
  protected int getClassId() {
    return MONITOR_GET_DMQ_SETTINGS_REP;
  }
  
  public void readFrom(InputStream is) throws IOException {
    super.readFrom(is);
    dmqId = StreamUtil.readStringFrom(is);
    threshold = new Integer(StreamUtil.readIntFrom(is));
  }

  public void writeTo(OutputStream os) throws IOException {
    super.writeTo(os);
    StreamUtil.writeTo(dmqId, os);
    StreamUtil.writeTo(threshold.intValue(), os);
  }
}
