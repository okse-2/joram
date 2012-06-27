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
 * Initial developer(s): ScalAgent Distributed Technologies
 * Contributor(s):
 */
package org.objectweb.joram.shared.admin;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import fr.dyade.aaa.common.stream.StreamUtil;

public class GetSubscriptionsRep extends AdminReply {
  private static final long serialVersionUID = 1L;

  private String[] subNames;

  private String[] topicIds;

  private int[] messageCounts;
  
  private int[] ackCounts;

  private boolean[] durable;

  public GetSubscriptionsRep(String[] subNames,
                             String[] topicIds,
                             int[] messageCounts,
                             int[] ackCounts,
                             boolean[] durable) {
    super(true, null);
    this.subNames = subNames;
    this.topicIds = topicIds;
    this.messageCounts = messageCounts;
    this.ackCounts = ackCounts;
    this.durable = durable;
  }

  public GetSubscriptionsRep() { }
  
  public final String[] getSubNames() {
    return subNames;
  }

  public final String[] getTopicIds() {
    return topicIds;
  }

  public final int[] getMessageCounts() {
    return messageCounts;
  }
  
  public final int[] getDeliveredMessageCount() {
    return ackCounts;
  }

  public final boolean[] getDurable() {
    return durable;
  }
  
  protected int getClassId() {
    return GET_SUBSCRIPTIONS_REP;
  }
    
  public void readFrom(InputStream is) throws IOException {
    super.readFrom(is);
    subNames = StreamUtil.readArrayOfStringFrom(is);
    topicIds = StreamUtil.readArrayOfStringFrom(is);
    messageCounts = StreamUtil.readArrayOfIntFrom(is);
    ackCounts = StreamUtil.readArrayOfIntFrom(is);
    durable = StreamUtil.readArrayOfBooleanFrom(is);
  }

  public void writeTo(OutputStream os) throws IOException {
    super.writeTo(os);
    StreamUtil.writeArrayOfStringTo(subNames, os);
    StreamUtil.writeArrayOfStringTo(topicIds, os);
    StreamUtil.writeArrayOfIntTo(messageCounts, os);
    StreamUtil.writeArrayOfIntTo(ackCounts, os);
    StreamUtil.writeArrayOfBooleanTo(durable, os);
  }
}
