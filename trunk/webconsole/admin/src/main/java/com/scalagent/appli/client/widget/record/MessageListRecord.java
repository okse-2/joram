/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2010 - 2011 ScalAgent Distributed Technologies
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
package com.scalagent.appli.client.widget.record;

import java.util.Date;
import java.util.Map;

import com.scalagent.appli.shared.MessageWTO;
import com.smartgwt.client.widgets.grid.ListGridRecord;

/**
 * @author Yohann CINTRE
 */
public class MessageListRecord extends ListGridRecord {

  public static String ATTRIBUTE_IDS = "idS";
  public static String ATTRIBUTE_EXPIRATION = "expiration";
  public static String ATTRIBUTE_TIMESTAMP = "timestamp";
  public static String ATTRIBUTE_DELIVERYCOUNT = "deliveryCount";
  public static String ATTRIBUTE_PRIORITY = "priority";
  public static String ATTRIBUTE_TEXT = "text";
  public static String ATTRIBUTE_TYPE = "type";
  public static String ATTRIBUTE_PROPERTIES = "properties";

  private MessageWTO message;

  public MessageListRecord() {
  }

  public MessageListRecord(MessageWTO message) {
    super();

    setMessage(message);
    setIdS(message.getId());
    setExpiration(message.getExpiration());
    setTimestamp(new Date(message.getTimestamp()));
    setDeliveryCount(message.getDeliveryCount());
    setPriority(message.getPriority());
    setText(message.getText());
    setType(message.getType());
    setProperties(message.getProperties() == null ? "null" : message.getProperties().toString());

    this.message = message;
  }

  @Override
  public String toString() {
    StringBuffer buffer = new StringBuffer();
    buffer.append("ids: " + getIdS());
    buffer.append("text: " + getText());

    return buffer.toString();
  }

  public String toStringAllContent() {
    StringBuffer buffer = new StringBuffer();
    buffer.append("idS: " + getIdS());
    buffer.append("; expiration: " + getExpiration());
    buffer.append("; timestamp: " + getTimestamp());
    buffer.append("; deliverycount: " + getDeliveryCount());
    buffer.append("; priority: " + getPriority());
    buffer.append("; text: " + getText());
    buffer.append("; type: " + getType());
    buffer.append("; properties: " + getProperties());

    return buffer.toString();
  }

  public void setMessage(MessageWTO message) {
    this.message = message;
  }

  public void setIdS(String idS) {
    setAttribute(ATTRIBUTE_IDS, idS);
  }

  public void setExpiration(long expiration) {
    setAttribute(ATTRIBUTE_EXPIRATION, expiration);
  }

  public void setTimestamp(Date timestamp) {
    setAttribute(ATTRIBUTE_TIMESTAMP, timestamp);
  }

  public void setDeliveryCount(int deliveryCount) {
    setAttribute(ATTRIBUTE_DELIVERYCOUNT, deliveryCount);
  }

  public void setPriority(int priority) {
    setAttribute(ATTRIBUTE_PRIORITY, priority);
  }

  public void setText(String text) {
    setAttribute(ATTRIBUTE_TEXT, text);
  }

  public void setType(int type) {
    setAttribute(ATTRIBUTE_TYPE, type);
  }

  public void setProperties(String properties) {
    setAttribute(ATTRIBUTE_PROPERTIES, properties);
  }

  public MessageWTO getMessage() {
    return message;
  }

  public String getIdS() {
    return getAttributeAsString(ATTRIBUTE_IDS);
  }

  public long getExpiration() {
    return getAttributeAsInt(ATTRIBUTE_EXPIRATION);
  }

  public Date getTimestamp() {
    return getAttributeAsDate(ATTRIBUTE_TIMESTAMP);
  }

  public int getDeliveryCount() {
    return getAttributeAsInt(ATTRIBUTE_DELIVERYCOUNT);
  }

  public int getPriority() {
    return getAttributeAsInt(ATTRIBUTE_PRIORITY);
  }

  public String getText() {
    return getAttributeAsString(ATTRIBUTE_TEXT);
  }

  public int getType() {
    return getAttributeAsInt(ATTRIBUTE_TYPE);
  }

  public Map getProperties() {
    return getAttributeAsMap(ATTRIBUTE_PROPERTIES);
  }

  public String getAllAtt() {
    String[] allatt = getAttributes();
    String ret = "";
    for (int i = 0; i < allatt.length; i++)
      ret = ret + allatt[i] + " / ";
    return ret;
  }
}