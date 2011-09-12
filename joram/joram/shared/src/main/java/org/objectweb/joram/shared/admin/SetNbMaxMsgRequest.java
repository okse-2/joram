/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2005 - 2011 ScalAgent Distributed Technologies
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
 * Initial developer(s): Nicolas Tachker (Scalagent)
 * Contributor(s): ScalAgent Distributed Technologies
 */
package org.objectweb.joram.shared.admin;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import fr.dyade.aaa.common.stream.StreamUtil;

/**
 * A <code>SetNbMaxMsg</code> instance requests to set a 
 * number max of message in Queue or Subscription.
 */
public class SetNbMaxMsgRequest extends DestinationAdminRequest {
  /** nbMaxMsg value (-1 no limit).*/
  private int nbMaxMsg;
  /** subscription name */
  private String subName = null;

  /**
   * Constructs a <code>SetNbMaxMsg</code> instance.
   *
   * @param id        Identifier of the queue or subscription. 
   * @param nbMaxMsg  nb Max of Message (-1 no limit).
   */
  public SetNbMaxMsgRequest(String id, int nbMaxMsg) {
    super(id);
    this.nbMaxMsg = nbMaxMsg;
  }

  public SetNbMaxMsgRequest() { }
  
  /**
   * Constructs a <code>SetNbMaxMsg</code> instance.
   *
   * @param id        Identifier of the queue or subscription. 
   * @param nbMaxMsg  nb Max of Message (-1 no limit).
   * @param subName Subscription name.
   */
  public SetNbMaxMsgRequest(String id, 
                     int nbMaxMsg,
                     String subName) {
    super(id);
    this.nbMaxMsg = nbMaxMsg;
    this.subName = subName;
  }
  
  /** Returns the nbMaxMsg value. */
  public int getNbMaxMsg() {
    return nbMaxMsg;
  }

  /** Returns SubName */
  public String getSubName() {
    return subName;
  }
  
  protected int getClassId() {
    return SET_NB_MAX_MSG;
  }
  
  public void readFrom(InputStream is) throws IOException {
    super.readFrom(is);
    nbMaxMsg = StreamUtil.readIntFrom(is);
    subName = StreamUtil.readStringFrom(is);
  }

  public void writeTo(OutputStream os) throws IOException {
    super.writeTo(os);
    StreamUtil.writeTo(nbMaxMsg, os);
    StreamUtil.writeTo(subName, os);
  }
}
