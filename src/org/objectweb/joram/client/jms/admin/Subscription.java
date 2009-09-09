/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - 2006 ScalAgent Distributed Technologies
 * Copyright (C) 2004 Bull SA
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
 * Initial developer(s): Frederic Maistre (Bull SA), 
 *			 Nicolas Tachker (ScalAgent Distributed Technologies)
 * Contributor(s): ScalAgent Distributed Technologies
 */
package org.objectweb.joram.client.jms.admin;

/**
 * The <code>Subscription</code> class is a utility class needed to show
 * information about client subscription.<p>
 * Be careful, contrary to user, queue or topic administration object, the
 * <code>Subscription</code> object is just a data structure; it is initialized
 * by <code>user.getSubscription()</code> methods and no longer keep consistent
 * with the real state of subscription. 
 */
public class Subscription {
  /**
   * Name of the subscription.
   */
  private String name;

  /**
   * Related topic unique identification.
   */
  private String topicId;

  /**
   * Number of pending messages for this subscription.
   */
  private int messageCount;

  /**
   * True if the subscription is durable.
   */
  private boolean durable;

  /**
   * Creates a new <code>Subscription</code> object.
   */
  public Subscription(String name,
                      String topicId,
                      int messageCount,
                      boolean durable) {
    this.name = name;
    this.topicId = topicId;
    this.messageCount = messageCount;
    this.durable = durable;
  }

  /**
   * Returns the subscription's name.
   *
   * @return the subscription's name.
   */
  public final String getName() {
    return name;
  }

  /**
   * Returns the related topic unique identification.
   *
   * @return the related topic unique identification.
   */
  public final String getTopicId() {
    return topicId;
  }

  /**
   * Returns the number of pending messages.
   *
   * @return the number of pending messages.
   */
  public final int getMessageCount() {
    return messageCount;
  }

  /**
   * Returns true if the subscription is durable, false otherwise.
   *
   * @return true if the subscription is durable, false otherwise.
   */
  public final boolean isDurable() {
    return durable;
  }

  /**
   * Returns a String image of the subscription.
   */
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
