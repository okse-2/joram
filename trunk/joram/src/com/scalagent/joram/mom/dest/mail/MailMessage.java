/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2007 - 2008 ScalAgent Distributed Technologies
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
 * Initial developer(s): Nicolas Tachker (ScalAgent)
 * Contributor(s): 
 */
package com.scalagent.joram.mom.dest.mail;

import org.objectweb.joram.shared.messages.Message;

/**
 * A mail message encapsulates a proprietary message which is also used
 * for effective MOM transport facility.
 */
public class MailMessage {
  private Message sharedMsg;

  /**
   * Constructs a bright new <code>MailMessage</code>.
   */
  public MailMessage() {
    sharedMsg = new Message();
  }

  /**
   * Instantiates a <code>MailMessage</code> wrapping a consumed
   * MOM simple message.
   *
   * @param momMsg  The MOM message to wrap.
   */
  public MailMessage(org.objectweb.joram.shared.messages.Message momMsg) {
    this.sharedMsg = momMsg;
  } 
  
  /**
   * 
   * @return shared message structure
   */
  public Message getSharedMessage() {
    return sharedMsg;
  }
  
  /**
   * The client message type: SIMPLE, TEXT, OBJECT, MAP, STREAM, BYTES.
   * By default, the message type is SIMPLE.
   * 
   * @return int
   */
  public int getType() {
    return sharedMsg.type;
  }

  /**
   * The message identifier.
   * @return identifier
   */
  public String getIdentifier() {
    return sharedMsg.id;
  }

  /**
   * <code>true</code> if the message must be persisted.
   * @return persistent
   */
  public boolean getPersistent() {
    return sharedMsg.persistent;
  }

  /**
   * The message priority from 0 to 9, 9 being the highest.
   * By default, the priority is 4?
   * 
   * @return priority
   */
  public int getJMSPriority() {
    return sharedMsg.priority;
  }

  /**
   * The message expiration time, by default 0 for infinite time-to-live.
   * @return expiration
   */
  public long getJMSExpiration() {
    return sharedMsg.expiration;
  }

  /**
   * The message time stamp.
   * @return time
   */
  public long getTimestamp() {
    return sharedMsg.timestamp;
  }

  /**
   * The message destination identifier.
   * @return  destination id.
   */
  public String getDestinationId() {
    return sharedMsg.toId;
  }

  /**
   * The message destination type.
   * @return destination type
   */
  public String getToType() {
    return sharedMsg.toType;
  }

  /**
   * The correlation identifier field.
   * @return correlation id
   */
  public String getCorrelationId() {
    return sharedMsg.correlationId;
  }

  /**
   * The reply to destination identifier.
   * @return reply id
   */
  public String getReplyToId() {
    return sharedMsg.replyToId;
  }

  /**
   * <code>true</code> if the "reply to" destination is a queue.
   * @return reply type
   */
  public String replyToType() {
    return sharedMsg.replyToType;
  }

  /**
   * The number of delivery attempts for this message.
   * @return delivery attempts
   */
  public int getDeliveryCount() {
    return sharedMsg.deliveryCount;
  }

  /**
   * <code>true</code> if the message has been denied at least once by a
   * consumer.
   * @return denied
   */
  public boolean getDenied() {
    return sharedMsg.redelivered;
  }

  /**
   * <code>true</code> if the message is considered as undeliverable.
   * @param name
   * @return property value
   */
  public Object getProperty(String name) {
    return sharedMsg.getProperty(name);
  }

  /**
   *  The text body of the message.
   * @return the text body of the message.
   */
  public String getText() {
    return sharedMsg.getText();
  }

  /**
   * The message identifier.
   * @param id
   */
  public void setIdentifier(String id) {
    sharedMsg.id = id;
  }

  /**
   * <code>true</code> if the message must be persisted.
   * @param persistent
   */
  public void setPersistent(boolean persistent) {
    sharedMsg.persistent = persistent;
  }

  /**
   * The message priority from 0 to 9, 9 being the highest.
   * By default, the priority is 4?
   * @param priority
   */
  public void setPriority(int priority) {
    sharedMsg.priority = priority;
  }

  /**
   * The message expiration time, by default 0 for infinite time-to-live.
   * @param expiration
   */
  public void setExpiration(long expiration) {
    sharedMsg.expiration = expiration;
  }

  /**
   * The message time stamp.
   * @param timestamp
   */
  public void setTimestamp(long timestamp) {
    sharedMsg.timestamp = timestamp;
  }

  /**
   * Sets the message destination.
   *
   * @param id  The destination identifier.
   * @param type The type of the destination.
   */
  public void setDestination(String id, String type) {
    sharedMsg.setDestination(id, type);
  }

  /**
   * The correlation identifier field.
   * @param correlationId
   */
  public void setCorrelationId(String correlationId) {
    sharedMsg.correlationId = correlationId;
  }

  /**
   * Sets the destination to which a reply should be sent.
   *
   * @param id  The destination identifier.
   * @param type The destination type.
   */
  public void setReplyTo(String id, String type) {
    sharedMsg.setReplyTo(id, type);
  }

  /**
   * The number of delivery attempts for this message.
   * @param deliveryCount
   */
  public void setDeliveryCount(int deliveryCount) {
    sharedMsg.deliveryCount = deliveryCount;
  }

  /**
   * <code>true</code> if the message has been denied at least once by a
   * consumer.
   * @param redelivered
   */
  public void setDenied(boolean redelivered) {
    sharedMsg.redelivered = redelivered;
  }

  /**
   * Sets a message property.
   * @param propName the property name
   * @param propValue the property value
   */
  public void setProperty(String propName, Object propValue) {
    sharedMsg.setProperty(propName, propValue);
  }

  /**
   * Sets a String as the body of the message.
   * @param text
   */
  public void setText(String text) {
    sharedMsg.setText(text);
  }
}
