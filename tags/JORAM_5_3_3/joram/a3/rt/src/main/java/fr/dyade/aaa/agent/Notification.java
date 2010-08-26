/*
 * Copyright (C) 2001 - 2007 ScalAgent Distributed Technologies 
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
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
 */
package fr.dyade.aaa.agent;

import java.io.Serializable;

/**
 * Class Notification is the root of the notifications hierarchy. Every
 * notification's class has Notification as a superclass.
 */
public class Notification implements Serializable, Cloneable {
  /** define serialVersionUID for interoperability */
  static final long serialVersionUID = 1L;

  /**
   * True if the notification is persistent, false otherwise. By default, this
   * field is set to true during object creation and disk loading. This field
   * is carry by network protocol.
   * 
   * There is no public setter for this attribute. According the fact that a
   * notification class is or not persistent this attribute should be statically
   * fixed in the subclass constructor.
   */
  protected transient boolean persistent = true;

  /**
   * True if the notification is detachable, false otherwise. A detachable
   * notification is saved in a different object that its containing
   * message.
   */
  protected transient boolean detachable = false;

  /**
   * True if the notification is detached, false otherwise. A detached
   * notification is not destroyed in the same way that its containing
   * message.
   */
  protected transient boolean detached = false;

  /**
   * The expiration date for this notification.
   * 
   * This field is handled by the network protocol in order to fit the time
   * synchronization problem.
   */
  long expiration = 0L;

  /**
   * Sets the expiration date for this notification.
   * 
   * A value of 0L (default) indicates that the notification does not expire.
   * 
   * @param expiration
   *            the expiration date for this notification.
   */
  public void setExpiration(long expiration) {
    this.expiration = expiration;
  }

  /**
   * Gets the notification's expiration value.
   *
   * This field is handled by the network protocol in order to fit
   * the time synchronization problem.
   *
   * If the expiration date is set to 0L (default), it indicates that
   * the notification does not expire.
   *
   * When a notification's expiration time is reached, the MOM should
   * discard it without any form of notification of message expiration.
   * Agents should not receive messages that have expired; however, the
   * MOM does not guarantee that this will not happen.
   *
   * @return The notification's expiration value.
   */
  public long getExpiration() {
    return expiration;
  }

  /**
   * The priority for this notification from 0 to 9, 9 being the highest. By
   * default, the priority is 4
   */
  byte priority = (byte) 4;
  
  /**
   * The agentId identifying the agent to which the notification is sent when it
   * is expired.<br>
   * Default value is null, which means the expired notification is lost.
   */
  AgentId deadNotificationAgentId = null;
  
  /**
   * Sets the priority for this notification.
   * 
   * A value between 0 (lowest) and 9 (highest), by default 4 (normal).
   * 
   * @param priority
   *            the priority for this notification.
   */
  public void setPriority(int priority) {
    if ((priority >= 0) && (priority <= 9))
      this.priority = (byte) priority;
  }
  
  /**
   * Gets the notification's priority value.
   * 
   * @return The notification's priority value.
   */
  public int getPriority() {
    return priority;
  }

  /**
   * If the notification is stored independently that its containing message
   * messageId contains the persistent name of this notification.
   */
  transient String messageId = null;

  public String getMessageId() {
    return messageId;
  }

  /** Context of the notification. */
  private Object context;

  /**
   * Sets the context of the notification.
   *
   * @param context the context of the notification.
   */
  public final void setContext(Object context) {
    this.context = context;
  }

  /**
   * Returns the context of the notification.
   *
   * @return the context of the notification.
   */
  public final Object getContext() {
    return context;
  }

  /**
   * Returns a clone of this notification.
   *
   * @return  a clone of this notification.
   */
  public synchronized Object clone() {
    try {
      Notification dup = (Notification) super.clone();
      dup.detached = false;
      dup.messageId = null;
      return dup;
    } catch (CloneNotSupportedException e) { 
      // this shouldn't happen, since we are Cloneable
      throw new InternalError();
    }
  }

  /**
   * Return true if notification is persistent.
   *
   * @return  persistent of this notification.
   */
  public boolean isPersistent() {
    return persistent;
  }

  /**
   * 
   * @return The agentId identifying the agent to which the notification is sent
   *         when it is expired.
   */
  public AgentId getDeadNotificationAgentId() {
    return deadNotificationAgentId;
  }

  /**
   * Sets the forwardExpiredNotAgentId value which enable sending expired
   * notifications to a specific agent
   * 
   * @param deadNotificationAgentId
   *            the AgentId to which the dead notification is forwarded
   */
  public void setDeadNotificationAgentId(AgentId deadNotificationAgentId) {
    this.deadNotificationAgentId = deadNotificationAgentId;
  }

  /**
   * Appends a string image for this object to the StringBuffer parameter.
   * 
   * @param output
   *            buffer to fill in
   * @return <code>output</code> buffer is returned
   */
  public StringBuffer toString(StringBuffer output) {
    output.append('(');
    output.append(super.toString());
    output.append(",messageId=").append(messageId);
    output.append(",persistent=").append(persistent);
    output.append(",detachable=").append(detachable);
    output.append(",detached=").append(detached);
    output.append(",context=").append(context);
    output.append(",expiration=").append(expiration);
    output.append(",priority=").append(priority);
    output.append(",deadNotificationAgentId=").append(deadNotificationAgentId);
    output.append(')');

    return output;
  }

  /**
   * Provides a string image for this object.
   *
   * @return  a string representation for this object.
   */
  public final String toString() {
    StringBuffer output = new StringBuffer();
    return toString(output).toString();
  }
}
