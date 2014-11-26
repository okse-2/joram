/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2010 ScalAgent Distributed Technologies
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
package com.scalagent.appli.shared;

import java.util.Map;

import com.scalagent.engine.shared.BaseWTO;

/**
 * @author Yohann CINTRE
 */
@SuppressWarnings("unchecked")
public class MessageWTO extends BaseWTO {

  private long expiration;
  private long timestamp;
  private int deliveryCount;
  private int priority;
  private String text;
  private int type;

  private Map<String, String> properties;

  public long getExpiration() {
    return expiration;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public int getDeliveryCount() {
    return deliveryCount;
  }

  public int getPriority() {
    return priority;
  }

  public String getText() {
    return text;
  }

  public int getType() {
    return type;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public void setExpiration(long expiration) {
    this.expiration = expiration;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  public void setDeliveryCount(int deliveryCount) {
    this.deliveryCount = deliveryCount;
  }

  public void setPriority(int priority) {
    this.priority = priority;
  }

  public void setText(String text) {
    this.text = text;
  }

  public void setType(int type) {
    this.type = type;
  }

  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }

  public MessageWTO() {
  }

  public MessageWTO(String idS, long expiration, long timestamp, int deliveryCount, int priority,
      String text, int type, Map<String, String> properties) {
    super();
    this.id = idS;
    this.expiration = expiration;
    this.timestamp = timestamp;
    this.deliveryCount = deliveryCount;
    this.priority = priority;
    this.text = text;
    this.type = type;
    this.properties = properties;
  }

  public String toStringFullContent() {
    return "MessageWTO [deliveryCount=" + deliveryCount + ", expiration=" + expiration + ", id=" + id
        + ", priority=" + priority + ", text=" + text + ", timestamp=" + timestamp + ", type=" + type
        + ",properties=" + properties + "]";
  }

  @Override
  public String toString() {
    return "MessageWTO [id=" + id + ", text=" + text + ", status=" + getDbChangeStatus() + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + deliveryCount;
    result = prime * result + (int) (expiration ^ (expiration >>> 32));
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + priority;
    result = prime * result + ((text == null) ? 0 : text.hashCode());
    result = prime * result + (int) (timestamp ^ (timestamp >>> 32));
    result = prime * result + type;
    return result;
  }

  @Override
  public boolean equals(Object anObj) {
    if (anObj == null)
      return false;
    if (anObj == this)
      return true;
    if (!(anObj instanceof MessageWTO))
      return false;
    MessageWTO obj = (MessageWTO) anObj;
    if (obj.id.equals(this.id))
      return true;
    return false;
  }

  @Override
  public MessageWTO clone() {

    MessageWTO msg = new MessageWTO();
    msg.id = id;
    msg.expiration = expiration;
    msg.timestamp = timestamp;
    msg.deliveryCount = deliveryCount;
    msg.priority = priority;
    msg.text = text;
    msg.properties = properties;

    return msg;
  }

  @Override
  public boolean equalsContent(Object anObj) {

    if (!equals(anObj))
      return false;

    MessageWTO obj = (MessageWTO) anObj;

    boolean eq = equalsWithNull(this.id, obj.id) && this.expiration == obj.expiration
        && this.timestamp == obj.timestamp && this.deliveryCount == obj.deliveryCount
        && this.priority == obj.priority && this.priority == obj.priority
        && equalsWithNull(this.properties, obj.properties);
    return eq;

  }
}