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
package com.scalagent.appli.client.widget.record;

import com.scalagent.appli.shared.SubscriptionWTO;
import com.smartgwt.client.widgets.grid.ListGridRecord;

/**
 * @author Yohann CINTRE
 */
public class SubscriptionListRecord extends ListGridRecord {

  public static String ATTRIBUTE_NAME = "name";
  public static String ATTRIBUTE_ACTIVE = "active";
  public static String ATTRIBUTE_DURABLE = "durable";
  public static String ATTRIBUTE_NBMAXMSG = "nbMaxMsg";
  public static String ATTRIBUTE_CONTEXTID = "contextId";
  public static String ATTRIBUTE_NBMSGSDELIVEREDSINCECREATION = "nbMsgsDeliveredSinceCreation";
  public static String ATTRIBUTE_NBMSGSSENTTODMQSINCECREATION = "nbMsgsSentToDMQSinceCreation";
  public static String ATTRIBUTE_PENDINGMESSAGECOUNT = "pendingMessageCount";
  public static String ATTRIBUTE_SELECTOR = "selector";
  public static String ATTRIBUTE_SUBREQUESTID = "subRequestId";

  private SubscriptionWTO sub;

  public SubscriptionListRecord() {
  }

  public SubscriptionListRecord(SubscriptionWTO sub) {
    super();

    setSubscription(sub);
    setName(sub.getId());
    setActive(sub.isActive());
    setDurable(sub.isDurable());
    setNbMaxMsg(sub.getNbMaxMsg());
    setContextId(sub.getContextId());
    setNbMsgsDeliveredSinceCreation(sub.getNbMsgsDeliveredSinceCreation());
    setNbMsgsSentToDMQSinceCreation((int) sub.getNbMsgsSentToDMQSinceCreation());
    setPendingMessageCount(sub.getPendingMessageCount());
    setSelector(sub.getSelector());
    setSubRequestId(sub.getSubRequestId());
  }

  @Override
  public String toString() {
    StringBuffer buffer = new StringBuffer();
    buffer.append("name: " + getAttribute(ATTRIBUTE_NAME));

    return buffer.toString();
  }

  public String toStringAllContent() {
    StringBuffer buffer = new StringBuffer();
    buffer.append("name: " + getAttribute(ATTRIBUTE_NAME));
    buffer.append("; active: " + getAttribute(ATTRIBUTE_ACTIVE));
    buffer.append("; durable: " + getAttribute(ATTRIBUTE_DURABLE));
    buffer.append("; nbMaxMsg: " + getAttribute(ATTRIBUTE_NBMAXMSG));
    buffer.append("; contextId: " + getAttribute(ATTRIBUTE_CONTEXTID));
    buffer.append("; nbMsgsDeliveredSinceCreation: " + getAttribute(ATTRIBUTE_NBMSGSDELIVEREDSINCECREATION));
    buffer.append("; nbMsgsSentToDMQSinceCreation: " + getAttribute(ATTRIBUTE_NBMSGSSENTTODMQSINCECREATION));
    buffer.append("; pendingMessageCount: " + getAttribute(ATTRIBUTE_PENDINGMESSAGECOUNT));
    buffer.append("; selector: " + getAttribute(ATTRIBUTE_SELECTOR));
    buffer.append("; subRequestId: " + getAttribute(ATTRIBUTE_SUBREQUESTID));
    return buffer.toString();
  }

  public void setSubscription(SubscriptionWTO sub) {
    this.sub = sub;
  }

  public void setName(String name) {
    setAttribute(ATTRIBUTE_NAME, name);
  }

  public void setActive(boolean active) {
    setAttribute(ATTRIBUTE_ACTIVE, active);
  }

  public void setDurable(boolean durable) {
    setAttribute(ATTRIBUTE_DURABLE, durable);
  }

  public void setNbMaxMsg(int nbMaxMsg) {
    setAttribute(ATTRIBUTE_NBMAXMSG, nbMaxMsg);
  }

  public void setContextId(int contextId) {
    setAttribute(ATTRIBUTE_CONTEXTID, contextId);
  }

  public void setNbMsgsDeliveredSinceCreation(int nbMsgsDeliveredSinceCreation) {
    setAttribute(ATTRIBUTE_NBMSGSDELIVEREDSINCECREATION, nbMsgsDeliveredSinceCreation);
  }

  public void setNbMsgsSentToDMQSinceCreation(int nbMsgsSentToDMQSinceCreation) {
    setAttribute(ATTRIBUTE_NBMSGSSENTTODMQSINCECREATION, nbMsgsSentToDMQSinceCreation);
  }

  public void setPendingMessageCount(int pendingMessageCount) {
    setAttribute(ATTRIBUTE_PENDINGMESSAGECOUNT, pendingMessageCount);
  }

  public void setSelector(String selector) {
    setAttribute(ATTRIBUTE_SELECTOR, selector);
  }

  public void setSubRequestId(int subRequestId) {
    setAttribute(ATTRIBUTE_SUBREQUESTID, subRequestId);
  }

  public SubscriptionWTO getSubscription() {
    return sub;
  }

  public String getName() {
    return getAttributeAsString(ATTRIBUTE_NAME);
  }

  public boolean isActive() {
    return getAttributeAsBoolean(ATTRIBUTE_ACTIVE);
  }

  public boolean isDurable() {
    return getAttributeAsBoolean(ATTRIBUTE_DURABLE);
  }

  public int getNbMaxMsg() {
    return getAttributeAsInt(ATTRIBUTE_NBMAXMSG);
  }

  public int getContextId() {
    return getAttributeAsInt(ATTRIBUTE_CONTEXTID);
  }

  public int getNbMsgsDeliveredSinceCreation() {
    return getAttributeAsInt(ATTRIBUTE_NBMSGSDELIVEREDSINCECREATION);
  }

  public int getNbMsgsSentToDMQSinceCreation() {
    return getAttributeAsInt(ATTRIBUTE_NBMSGSSENTTODMQSINCECREATION);
  }

  public int getPendingMessageCount() {
    return getAttributeAsInt(ATTRIBUTE_PENDINGMESSAGECOUNT);
  }

  public String getSelector() {
    return getAttributeAsString(ATTRIBUTE_SELECTOR);
  }

  public int getSubRequestId() {
    return getAttributeAsInt(ATTRIBUTE_SUBREQUESTID);
  }

  public String getAllAtt() {
    String[] allatt = getAttributes();
    String ret = "";
    for (int i = 0; i < allatt.length; i++)
      ret = ret + allatt[i] + " / ";
    return ret;
  }

}