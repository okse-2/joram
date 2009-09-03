/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - 2006 ScalAgent Distributed Technologies
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

public class GetSubscriptionsRep extends AdminReply {
  private static final long serialVersionUID = 1734741581653138720L;

  private String[] subNames;

  private String[] topicIds;

  private int[] messageCounts;

  private boolean[] durable;

  public GetSubscriptionsRep(String[] subNames,
                             String[] topicIds,
                             int[] messageCounts,
                             boolean[] durable) {
    super(true, null);
    this.subNames = subNames;
    this.topicIds = topicIds;
    this.messageCounts = messageCounts;
    this.durable = durable;
  }

  public final String[] getSubNames() {
    return subNames;
  }

  public final String[] getTopicIds() {
    return topicIds;
  }

  public final int[] getMessageCounts() {
    return messageCounts;
  }

  public final boolean[] getDurable() {
    return durable;
  }
}
