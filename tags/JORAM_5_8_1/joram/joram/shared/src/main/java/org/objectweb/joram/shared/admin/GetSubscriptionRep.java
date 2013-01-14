/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - 2012 ScalAgent Distributed Technologies
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

public class GetSubscriptionRep extends AdminReply {
  private static final long serialVersionUID = 1L;

  private String topicId;

  private int messageCount;
  
  private int ackCount;

  private boolean durable;

  public GetSubscriptionRep(String topicId,
                            int messageCount,
                            int ackCount,
                            boolean durable) {
    super(true, null);
    this.topicId = topicId;
    this.messageCount = messageCount;
    this.ackCount = ackCount;
    this.durable = durable;
  }
  
  public GetSubscriptionRep() { }

  public final String getTopicId() {
    return topicId;
  }

  public final int getMessageCount() {
    return messageCount;
  }

  public final int getDeliveredMessageCount() {
    return ackCount;
  }
  
  public final boolean getDurable() {
    return durable;
  }
  
  protected int getClassId() {
    return GET_SUBSCRIPTION_REP;
  }
  
  public void readFrom(InputStream is) throws IOException {
    super.readFrom(is);
    topicId = StreamUtil.readStringFrom(is);
    messageCount = StreamUtil.readIntFrom(is);
    ackCount = StreamUtil.readIntFrom(is);
    durable = StreamUtil.readBooleanFrom(is);
  }

  public void writeTo(OutputStream os) throws IOException {
    super.writeTo(os);
    StreamUtil.writeTo(topicId, os);
    StreamUtil.writeTo(messageCount, os);
    StreamUtil.writeTo(ackCount, os);
    StreamUtil.writeTo(durable, os);
  }
}
