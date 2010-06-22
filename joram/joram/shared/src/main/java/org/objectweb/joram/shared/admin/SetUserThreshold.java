/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2007 ScalAgent Distributed Technologies
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
 * A <code>SetUserThreshold</code> instance requests to set a given
 * threshold value as the threshold for a given user.
 */
public class SetUserThreshold extends AdminRequest {
  private static final long serialVersionUID = 1L;

  /** Identifier of the user's proxy the threshold is set for. */
  private String userProxId;
  /** Threshold value. */
  private int threshold;

  /**
   * Constructs a <code>SetUserThreshold</code> instance.
   *
   * @param userProxId  Identifier of the user's proxy the threshold is set
   *          for. 
   * @param threshold  Threshold value.
   */
  public SetUserThreshold(String userProxId, int threshold) {
    this.userProxId = userProxId;
    this.threshold = threshold;
  }

  public SetUserThreshold() { }
  
  /** Returns the identifier of the user's proxy the threshold is set for. */
  public String getUserProxId() {
    return userProxId;
  }

  /** Returns the threshold value. */
  public int getThreshold() {
    return threshold;
  }
  
  protected int getClassId() {
    return SET_USER_THRESHOLD;
  }
  
  public void readFrom(InputStream is) throws IOException {
    userProxId = StreamUtil.readStringFrom(is);
    threshold = StreamUtil.readIntFrom(is);
  }

  public void writeTo(OutputStream os) throws IOException {
    StreamUtil.writeTo(userProxId, os);
    StreamUtil.writeTo(threshold, os);
  }
}
