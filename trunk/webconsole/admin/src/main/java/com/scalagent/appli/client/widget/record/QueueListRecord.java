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

import java.util.Date;

import com.scalagent.appli.shared.QueueWTO;
import com.smartgwt.client.widgets.grid.ListGridRecord;

/**
 * @author Yohann CINTRE
 */
public class QueueListRecord extends ListGridRecord {

  public static String ATTRIBUTE_NAME = "name";
  public static String ATTRIBUTE_CREATIONDATE = "creationDate";
  public static String ATTRIBUTE_SUBSCRIBERIDS = "subscriberIds";
  public static String ATTRIBUTE_DMQID = "DMQId";
  public static String ATTRIBUTE_DESTINATIONID = "destinationId";
  public static String ATTRIBUTE_NBMSGSDELIVERSINCECREATION = "nbMsgsDeliverSinceCreation";
  public static String ATTRIBUTE_NBMSGSRECEIVESINCECREATION = "nbMsgsReceiveSinceCreation";
  public static String ATTRIBUTE_NBMSGSSENTTODMQSINCECREATION = "nbMsgsSentToDMQSinceCreation";
  public static String ATTRIBUTE_PERIOD = "period";
  public static String ATTRIBUTE_RIGHTS = "rights";
  public static String ATTRIBUTE_FREEREADING = "freeReading";
  public static String ATTRIBUTE_FREEWRITING = "freeWriting";
  public static String ATTRIBUTE_THRESHOLD = "threshold";
  public static String ATTRIBUTE_WAITINGREQUESTCOUNT = "waitingRequestCount";
  public static String ATTRIBUTE_PENDINGMESSAGECOUNT = "pendingMessageCount";
  public static String ATTRIBUTE_DELIVEREDMESSAGECOUNT = "deliveredMessageCount";
  public static String ATTRIBUTE_NBMAXMSG = "nbMaxMsg";

  private QueueWTO queue;

  public QueueListRecord() {
  }

  public QueueListRecord(QueueWTO queue) {
    super();

    setName(queue.getId());
    setCreationDate(queue.getCreationDate());
    setDMQId(queue.getDMQId());
    setDestinationId(queue.getDestinationId());
    setNbMsgsDeliverSinceCreation(queue.getNbMsgsDeliverSinceCreation());
    setNbMsgsReceiveSinceCreation(queue.getNbMsgsReceiveSinceCreation());
    setNbMsgsSentToDMQSinceCreation(queue.getNbMsgsSentToDMQSinceCreation());
    setPeriod(queue.getPeriod());
    setRights(queue.getRights());
    setFreeReading(queue.isFreeReading());
    setFreeWriting(queue.isFreeWriting());
    setThreshold(queue.getThreshold());
    setWaitingRequestCount(queue.getWaitingRequestCount());
    setPendingMessageCount(queue.getPendingMessageCount());
    setDeliveredMessageCount(queue.getDeliveredMessageCount());
    setNbMaxMsg(queue.getNbMaxMsg());

    this.queue = queue;
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
    buffer.append("; creationDate: " + getAttribute(ATTRIBUTE_CREATIONDATE));
    buffer.append("; subscriberIds: " + getAttribute(ATTRIBUTE_SUBSCRIBERIDS));
    buffer.append("; DMQid: " + getAttribute(ATTRIBUTE_DMQID));
    buffer.append("; destinationId: " + getAttribute(ATTRIBUTE_DESTINATIONID));
    buffer.append("; nbMsgsDeliverSinceCreation: " + getAttribute(ATTRIBUTE_NBMSGSDELIVERSINCECREATION));
    buffer.append("; nbMsgsReceiveSinceCreation: " + getAttribute(ATTRIBUTE_NBMSGSRECEIVESINCECREATION));
    buffer.append("; nbMsgsSentToDMQSinceCreation: " + getAttribute(ATTRIBUTE_NBMSGSSENTTODMQSINCECREATION));
    buffer.append("; period: " + getAttribute(ATTRIBUTE_PERIOD));
    buffer.append("; rights: " + getAttribute(ATTRIBUTE_RIGHTS));
    buffer.append("; freeReading: " + getAttribute(ATTRIBUTE_FREEREADING));
    buffer.append("; freeWriting: " + getAttribute(ATTRIBUTE_FREEWRITING));
    return buffer.toString();
  }

  public QueueWTO getQueue() {
    return queue;
  }

  public void setQueue(QueueWTO queue) {
    this.queue = queue;
  }

  public void setName(String topicName) {
    setAttribute(ATTRIBUTE_NAME, topicName);
  }

  public void setCreationDate(Date date) {
    setAttribute(ATTRIBUTE_CREATIONDATE, date);
  }

  public void setSubscriberIds(String[] subscriberIds) {
    setAttribute(ATTRIBUTE_SUBSCRIBERIDS, subscriberIds);
  }

  public void setDMQId(String DMQId) {
    setAttribute(ATTRIBUTE_DMQID, DMQId);
  }

  public void setDestinationId(String destinationId) {
    setAttribute(ATTRIBUTE_DESTINATIONID, destinationId);
  }

  public void setNbMsgsDeliverSinceCreation(long nbMsgsDeliverSinceCreation) {
    setAttribute(ATTRIBUTE_NBMSGSDELIVERSINCECREATION, nbMsgsDeliverSinceCreation);
  }

  public void setNbMsgsReceiveSinceCreation(long nbMsgsReceiveSinceCreation) {
    setAttribute(ATTRIBUTE_NBMSGSRECEIVESINCECREATION, nbMsgsReceiveSinceCreation);
  }

  public void setNbMsgsSentToDMQSinceCreation(long nbMsgsSentToDMQSinceCreation) {
    setAttribute(ATTRIBUTE_NBMSGSSENTTODMQSINCECREATION, nbMsgsSentToDMQSinceCreation);
  }

  public void setPeriod(long period) {
    setAttribute(ATTRIBUTE_PERIOD, period);
  }

  public void setRights(String[] rights) {
    setAttribute(ATTRIBUTE_RIGHTS, rights);
  }

  public void setFreeReading(boolean freeReading) {
    setAttribute(ATTRIBUTE_FREEREADING, freeReading);
  }

  public void setFreeWriting(boolean freeWriting) {
    setAttribute(ATTRIBUTE_FREEWRITING, freeWriting);
  }

  public void setThreshold(int threshold) {
    setAttribute(ATTRIBUTE_THRESHOLD, threshold);
  }

  public void setWaitingRequestCount(int waitingRequestCount) {
    setAttribute(ATTRIBUTE_WAITINGREQUESTCOUNT, waitingRequestCount);
  }

  public void setPendingMessageCount(int pendingMessageCount) {
    setAttribute(ATTRIBUTE_PENDINGMESSAGECOUNT, pendingMessageCount);
  }

  public void setDeliveredMessageCount(int deliveredMessageCount) {
    setAttribute(ATTRIBUTE_DELIVEREDMESSAGECOUNT, deliveredMessageCount);
  }

  public void setNbMaxMsg(int nbMaxMsg) {
    setAttribute(ATTRIBUTE_NBMAXMSG, nbMaxMsg);
  }

  public void cleanWaitingRequest() {
    setWaitingRequestCount(0);
  }

  public void cleanPendingMessage() {
    setPendingMessageCount(0);
  }

  public String getName() {
    return getAttributeAsString(ATTRIBUTE_NAME);
  }

  public Date getCreationDate() {
    return getAttributeAsDate(ATTRIBUTE_CREATIONDATE);
  }

  public String[] getSubscriberIds() {
    return getAttributeAsStringArray(ATTRIBUTE_SUBSCRIBERIDS);
  }

  public String getDMQId() {
    return getAttributeAsString(ATTRIBUTE_DMQID);
  }

  public String getDestinationId() {
    return getAttributeAsString(ATTRIBUTE_DESTINATIONID);
  }

  public long getNbMsgsDeliverSinceCreation() {
    return getAttributeAsInt(ATTRIBUTE_NBMSGSDELIVERSINCECREATION);
  }

  public long getNbMsgsReceiveSinceCreation() {
    return getAttributeAsInt(ATTRIBUTE_NBMSGSRECEIVESINCECREATION);
  }

  public long getNbMsgsSentToDMQSinceCreation() {
    return getAttributeAsInt(ATTRIBUTE_NBMSGSSENTTODMQSINCECREATION);
  }

  public long getPeriod() {
    return getAttributeAsInt(ATTRIBUTE_PERIOD);
  }

  public String[] getRights() {
    return getAttributeAsStringArray(ATTRIBUTE_RIGHTS);
  }

  public boolean isFreeReading() {
    return getAttributeAsBoolean(ATTRIBUTE_FREEREADING);
  }

  public boolean isFreeWriting() {
    return getAttributeAsBoolean(ATTRIBUTE_FREEWRITING);
  }

  public int getThreshold() {
    return getAttributeAsInt(ATTRIBUTE_THRESHOLD);
  }

  public int getWaitingRequestCount() {
    return getAttributeAsInt(ATTRIBUTE_WAITINGREQUESTCOUNT);
  }

  public int getPendingMessageCount() {
    return getAttributeAsInt(ATTRIBUTE_PENDINGMESSAGECOUNT);
  }

  public int getDeliveredMessageCount() {
    return getAttributeAsInt(ATTRIBUTE_DELIVEREDMESSAGECOUNT);
  }

  public int getNbMaxMsg() {
    return getAttributeAsInt(ATTRIBUTE_NBMAXMSG);
  }

  public String getAllAtt() {
    String[] allatt = getAttributes();
    String ret = "";
    for (int i = 0; i < allatt.length; i++)
      ret = ret + allatt[i] + " / ";
    return ret;
  }

}