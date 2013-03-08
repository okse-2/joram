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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;

import fr.dyade.aaa.util.TransactionObject;
import fr.dyade.aaa.util.TransactionObjectFactory;
import fr.dyade.aaa.util.TransactionObjectFactoryRepository;

/**
 * Class Notification is the root of the notifications hierarchy. Every
 * notification's class has Notification as a superclass.
 */
public class Notification implements Serializable, Cloneable, TransactionObject {
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
  int priority = (byte) 4;
  
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
   * The agentId identifying the agent to which the notification is sent when it
   * is expired.<br>
   * Default value is null, which means the expired notification is lost.
   */
  AgentId deadNotificationAgentId = null;

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
  
  private static final byte ExpirationSet = 0x10;
  private static final byte ContextSet = 0x20;
  private static final byte DeadNotificationAgentIdSet = 0x40;
  
  /**
   *  The writeObject method is responsible for writing the state of the
   * object for its particular class so that the corresponding readObject
   * method can restore it.
   * 
   * @param out
   * @throws IOException
   */
  private void writeObject(java.io.ObjectOutputStream out) throws IOException {
    out.writeByte((priority&0x0F)|    // 4 bits
                  ((expiration > 0)?ExpirationSet:0x00)|
                  ((context!=null)?ContextSet:0x00)|
                  ((deadNotificationAgentId!=null)?DeadNotificationAgentIdSet:0x00));
    if (expiration > 0)
      out.writeLong(expiration);
    if(context != null)
      out.writeObject(context);
    if (deadNotificationAgentId!=null)
      out.writeObject(deadNotificationAgentId);
  }
  
  /**
   *  The readObject method is responsible for reading from the stream and
   * restoring the classes fields.
   * 
   * @param in
   * @throws IOException
   * @throws ClassNotFoundException
   */
  private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
    byte tmp = in.readByte();
    priority = tmp & 0x0F; // 4 bits
    if ((tmp & ExpirationSet) != 0)
      expiration = in.readLong();
    if ((tmp & ContextSet) != 0)
      context = in.readObject();
    if ((tmp & DeadNotificationAgentIdSet) != 0)
      deadNotificationAgentId = (AgentId) in.readObject();
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

  public int getClassId() {
    return TransactionObject.NOTIFICATION_CLASS_ID;
  }

  //JORAM_PERF_BRANCH
  public void encodeTransactionObject(DataOutputStream os) throws IOException {
    os.writeByte((priority & 0x0F)
        | // 4 bits
        ((expiration > 0) ? ExpirationSet : 0x00)
        | ((context != null) ? ContextSet : 0x00)
        | ((deadNotificationAgentId != null) ? DeadNotificationAgentIdSet
            : 0x00));
    if (expiration > 0)
      os.writeLong(expiration);
    if (context != null) {
      if (context instanceof TransactionObject) {
        os.writeBoolean(true);
        TransactionObject to = (TransactionObject) context;
        os.writeInt(to.getClassId());
        to.encodeTransactionObject(os);
      } else {
        os.writeBoolean(false);
      }
    }
    if (deadNotificationAgentId != null)
      deadNotificationAgentId.encodeTransactionObject(os);
  }

  //JORAM_PERF_BRANCH
  public void decodeTransactionObject(DataInputStream is) throws IOException {
    byte tmp = is.readByte();
    priority = tmp & 0x0F; // 4 bits
    if ((tmp & ExpirationSet) != 0)
      expiration = is.readLong();
    if ((tmp & ContextSet) != 0) {
      boolean isTransactionObject = is.readBoolean();
      if (isTransactionObject) {
        int classId = is.readInt();
        TransactionObjectFactory factory = TransactionObjectFactoryRepository.getFactory(classId);
        TransactionObject to = factory.newInstance();
        to.decodeTransactionObject(is);
        context = to;
      } else {
        context = null;
      }
    }
    if ((tmp & DeadNotificationAgentIdSet) != 0) {
      deadNotificationAgentId = new AgentId();
      deadNotificationAgentId.decodeTransactionObject(is);
    }
  }
  
}
