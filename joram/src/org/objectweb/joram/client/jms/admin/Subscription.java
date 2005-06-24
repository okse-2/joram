/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - Bull SA
 * Copyright (C) 2004 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
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
 * Initial developer(s): Frederic Maistre (Bull SA), Nicolas Tachker (ScalAgent)
 * Contributor(s): ScalAgent DT
 */
package org.objectweb.joram.client.jms.admin;

public class Subscription {

  private String name;

  private String topicId;

  private int messageCount;

  private boolean durable;

  public Subscription(String name,
                      String topicId,
                      int messageCount,
                      boolean durable) {
    this.name = name;
    this.topicId = topicId;
    this.messageCount = messageCount;
    this.durable = durable;
  }

  public final String getName() {
    return name;
  }

  public final String getTopicId() {
    return topicId;
  }

  public final int getMessageCount() {
    return messageCount;
  }

  public final boolean isDurable() {
    return durable;
  }

  public void setMessageCount(int messageCount) {
    this.messageCount = messageCount;
  }

  public String toString() {
    StringBuffer buff = new StringBuffer();
    buff.append("Subscription(");
    buff.append("name=");
    buff.append(name);
    buff.append(",topicId=");
    buff.append(topicId);
    buff.append(",messageCount=");
    buff.append(messageCount);
    buff.append(",durable=");
    buff.append(durable);
    buff.append(")");
    return buff.toString();
  }
}
