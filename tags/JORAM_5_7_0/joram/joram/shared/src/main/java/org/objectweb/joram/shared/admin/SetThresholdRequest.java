/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2011 ScalAgent Distributed Technologies
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
public class SetThresholdRequest extends DestinationAdminRequest {
  /** Threshold value (-1 no limit). */
  private int threshold;
  /** subscription name */
  private String subName = null;

  /**
   * Constructs a <code>SetQueueThreshold</code> instance.
   *
   * @param queueId  Identifier of the queue the threshold is set for. 
   * @param threshold  Threshold value.
   */
  public SetThresholdRequest(String destId, int threshold) {
    super(destId);
    this.threshold = threshold;
  }
  
  /**
   * Constructs a <code>SetQueueThreshold</code> instance.
   *
   * @param id        Identifier of the queue or subscription. 
   * @param nbMaxMsg  nb Max of Message (-1 no limit).
   * @param subName Subscription name.
   */
  public SetThresholdRequest(String destId, String subName, int threshold) {
    super(destId);
    this.threshold = threshold;
    this.subName = subName;
  }

  public SetThresholdRequest() { }
  
  /** Returns the threshold value. */
  public int getThreshold() {
    return threshold;
  }

  /** Returns SubName */
  public String getSubName() {
    return subName;
  }
  
  protected int getClassId() {
    return SET_THRESHOLD;
  }
  
  public void readFrom(InputStream is) throws IOException {
    super.readFrom(is);
    threshold = StreamUtil.readIntFrom(is);
    subName = StreamUtil.readStringFrom(is);
  }

  public void writeTo(OutputStream os) throws IOException {
    super.writeTo(os);
    StreamUtil.writeTo(threshold, os);
    StreamUtil.writeTo(subName, os);
  }
}
