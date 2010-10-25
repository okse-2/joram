/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2010 ScalAgent Distributed Technologies
 * Copyright (C) 1996 - 2000 Dyade
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
 * A <code>SetQueueThreshold</code> instance requests to set a given
 * threshold value as the threshold for a given queue.
 */
public class SetThresholdRequest extends AdminRequest {
  private static final long serialVersionUID = 1L;

  /** Identifier of the queue the threshold is set for. */
  private String destId;
  /** Threshold value. */
  private int threshold;

  /**
   * Constructs a <code>SetQueueThreshold</code> instance.
   *
   * @param queueId  Identifier of the queue the threshold is set for. 
   * @param threshold  Threshold value.
   */
  public SetThresholdRequest(String destId, int threshold) {
    this.destId = destId;
    this.threshold = threshold;
  }

  public SetThresholdRequest() { }
  
  /** Returns the identifier of the queue the threshold is set for. */
  public String getDestId() {
    return destId;
  }

  /** Returns the threshold value. */
  public int getThreshold() {
    return threshold;
  }
  
  protected int getClassId() {
    return SET_THRESHOLD;
  }
  
  public void readFrom(InputStream is) throws IOException {
    destId = StreamUtil.readStringFrom(is);
    threshold = StreamUtil.readIntFrom(is);
  }

  public void writeTo(OutputStream os) throws IOException {
    StreamUtil.writeTo(destId, os);
    StreamUtil.writeTo(threshold, os);
  }
}
