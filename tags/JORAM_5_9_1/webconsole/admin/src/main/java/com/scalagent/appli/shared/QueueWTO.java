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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.scalagent.engine.shared.BaseWTO;

/**
 * @author Yohann CINTRE
 */
public class QueueWTO extends BaseWTO {

  private Date creationDate = new Date();
  private String DMQId;
  private String destinationId;
  private long nbMsgsDeliverSinceCreation;
  private long nbMsgsReceiveSinceCreation;
  private long nbMsgsSentToDMQSinceCreation;
  private long period;
  private String[] rights;
  private boolean freeReading;
  private boolean freeWriting;
  private int threshold;
  private int waitingRequestCount;
  private int pendingMessageCount;
  private int deliveredMessageCount;
  private int nbMaxMsg;
  private List<String> messagesList;

  public Date getCreationDate() {
    return creationDate;
  }

  public String getDMQId() {
    return DMQId;
  }

  public String getDestinationId() {
    return destinationId;
  }

  public long getNbMsgsDeliverSinceCreation() {
    return nbMsgsDeliverSinceCreation;
  }

  public long getNbMsgsReceiveSinceCreation() {
    return nbMsgsReceiveSinceCreation;
  }

  public long getNbMsgsSentToDMQSinceCreation() {
    return nbMsgsSentToDMQSinceCreation;
  }

  public long getPeriod() {
    return period;
  }

  public String[] getRights() {
    return rights;
  }

  public boolean isFreeReading() {
    return freeReading;
  }

  public boolean isFreeWriting() {
    return freeWriting;
  }

  public int getThreshold() {
    return threshold;
  }

  public int getWaitingRequestCount() {
    return waitingRequestCount;
  }

  public int getPendingMessageCount() {
    return pendingMessageCount;
  }

  public int getDeliveredMessageCount() {
    return deliveredMessageCount;
  }

  public int getNbMaxMsg() {
    return nbMaxMsg;
  }

  public List<String> getMessagesList() {
    return messagesList;
  }

  public void clearMessagesList() {
    this.messagesList.clear();
  }

  public void setCreationDate(Date date) {
    this.creationDate = date;
  }

  public void setDMQId(String DMQId) {
    this.DMQId = DMQId;
  }

  public void setDestinationId(String destinationId) {
    this.destinationId = destinationId;
  }

  public void setNbMsgsDeliverSinceCreation(long nbMsgsDeliverSinceCreation) {
    this.nbMsgsDeliverSinceCreation = nbMsgsDeliverSinceCreation;
  }

  public void setNbMsgsReceiveSinceCreation(long nbMsgsReceiveSinceCreation) {
    this.nbMsgsReceiveSinceCreation = nbMsgsReceiveSinceCreation;
  }

  public void setNbMsgsSentToDMQSinceCreation(long nbMsgsSentToDMQSinceCreation) {
    this.nbMsgsSentToDMQSinceCreation = nbMsgsSentToDMQSinceCreation;
  }

  public void setPeriod(long period) {
    this.period = period;
  }

  public void setRights(String[] rights) {
    this.rights = rights;
  }

  public void setFreeReading(boolean freeReading) {
    this.freeReading = freeReading;
  }

  public void setFreeWriting(boolean freeWriting) {
    this.freeWriting = freeWriting;
  }

  public void setThreshold(int threshold) {
    this.threshold = threshold;
  }

  public void setWaitingRequestCount(int waitingRequestCount) {
    this.waitingRequestCount = waitingRequestCount;
  }

  public void setPendingMessageCount(int pendingMessageCount) {
    this.pendingMessageCount = pendingMessageCount;
  }

  public void setDeliveredMessageCount(int deliveredMessageCount) {
    this.deliveredMessageCount = deliveredMessageCount;
  }

  public void addMessageToList(String messageId) {
    messagesList.add(messageId);
  }

  public void removeMessageFromList(String messageId) {
    messagesList.remove(messageId);
  }

  public void cleanWaitingRequest() {
    setWaitingRequestCount(0);
  }

  public void cleanPendingMessage() {
    setPendingMessageCount(0);
  }

  public void setNbMaxMsg(int nbMaxMsg) {
    this.nbMaxMsg = nbMaxMsg;
  }

  public QueueWTO(String name, Date creationDate, String DMQId, String destinationId,
      long nbMsgsDeliverSinceCreation, long nbMsgsReceiveSinceCreation, long nbMsgsSentToDMQSinceCreation,
      long period, String[] rights, boolean freeReading, boolean freeWriting, int threshold,
      int waitingRequestCount, int pendingMessageCount, int deliveredMessageCount, int nbMaxMsg) {

    this.id = name;
    this.creationDate = creationDate;
    this.DMQId = DMQId;
    this.destinationId = destinationId;
    this.nbMsgsDeliverSinceCreation = nbMsgsDeliverSinceCreation;
    this.nbMsgsReceiveSinceCreation = nbMsgsReceiveSinceCreation;
    this.nbMsgsSentToDMQSinceCreation = nbMsgsSentToDMQSinceCreation;
    this.period = period;
    this.rights = rights;
    this.freeReading = freeReading;
    this.freeWriting = freeWriting;
    this.threshold = threshold;
    this.waitingRequestCount = waitingRequestCount;
    this.pendingMessageCount = pendingMessageCount;
    this.deliveredMessageCount = deliveredMessageCount;
    this.nbMaxMsg = nbMaxMsg;

    this.messagesList = new ArrayList<String>();

  }

  public QueueWTO() {
    this.messagesList = new ArrayList<String>();
  }

  @Override
  public String toString() {
    return "[name=" + id + ", messagesList=" + messagesList + " ]";
  }

  public String toStringFullContent() {
    return "[name=" + id + ", creationDate=" + creationDate + ", DMQId=" + DMQId
        + ", nbMsgsDeliverSinceCreation=" + nbMsgsDeliverSinceCreation + ", nbMsgsReceiveSinceCreation="
        + nbMsgsReceiveSinceCreation + ", nbMsgsSentToDMQSinceCreation=" + nbMsgsSentToDMQSinceCreation
        + ", period=" + period + ", rights=" + rights + ", freeReading=" + freeReading + ", freeWriting="
        + freeWriting + ", threshold=" + threshold + ", waitingRequestCount=" + waitingRequestCount
        + ", pendingMessageCount=" + pendingMessageCount + ", deliveredMessageCount=" + deliveredMessageCount
        + ", nbMaxMsg=" + nbMaxMsg + " ]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((DMQId == null) ? 0 : DMQId.hashCode());
    result = prime * result + ((creationDate == null) ? 0 : creationDate.hashCode());
    result = prime * result + deliveredMessageCount;
    result = prime * result + ((destinationId == null) ? 0 : destinationId.hashCode());
    result = prime * result + (freeReading ? 1231 : 1237);
    result = prime * result + (freeWriting ? 1231 : 1237);
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + nbMaxMsg;
    result = prime * result + (int) (nbMsgsDeliverSinceCreation ^ (nbMsgsDeliverSinceCreation >>> 32));
    result = prime * result + (int) (nbMsgsReceiveSinceCreation ^ (nbMsgsReceiveSinceCreation >>> 32));
    result = prime * result + (int) (nbMsgsSentToDMQSinceCreation ^ (nbMsgsSentToDMQSinceCreation >>> 32));
    result = prime * result + pendingMessageCount;
    result = prime * result + (int) (period ^ (period >>> 32));
    result = prime * result + Arrays.hashCode(rights);
    result = prime * result + threshold;
    result = prime * result + waitingRequestCount;
    return result;
  }

  @Override
  public boolean equals(Object anObj) {
    if (anObj == null)
      return false;
    if (anObj == this)
      return true;
    if (!(anObj instanceof QueueWTO))
      return false;
    QueueWTO obj = (QueueWTO) anObj;
    if (obj.id.equals(this.id))
      return true;
    return false;
  }

  @Override
  public QueueWTO clone() {

    QueueWTO queue = new QueueWTO();

    queue.setId(this.getId());
    queue.setCreationDate(this.getCreationDate());
    queue.setDMQId(this.getDMQId());
    queue.setDestinationId(this.getDestinationId());
    queue.setNbMsgsDeliverSinceCreation(this.getNbMsgsDeliverSinceCreation());
    queue.setNbMsgsReceiveSinceCreation(this.getNbMsgsReceiveSinceCreation());
    queue.setNbMsgsSentToDMQSinceCreation(this.getNbMsgsSentToDMQSinceCreation());
    queue.setPeriod(this.getPeriod());
    queue.setRights(this.getRights());
    queue.setFreeReading(this.isFreeReading());
    queue.setFreeWriting(this.isFreeWriting());
    queue.setThreshold(this.getThreshold());
    queue.setWaitingRequestCount(this.getWaitingRequestCount());
    queue.setPendingMessageCount(this.getPendingMessageCount());
    queue.setDeliveredMessageCount(this.getDeliveredMessageCount());
    queue.setNbMaxMsg(this.getNbMaxMsg());

    return queue;
  }

  @Override
  public boolean equalsContent(Object anObj) {

    if (!equals(anObj))
      return false;

    QueueWTO obj = (QueueWTO) anObj;

    boolean eq = equalsWithNull(this.id, obj.id) && equalsWithNull(this.creationDate, obj.creationDate)
        && equalsWithNull(this.DMQId, obj.DMQId) && equalsWithNull(this.destinationId, obj.destinationId)
        && this.nbMsgsDeliverSinceCreation == obj.nbMsgsDeliverSinceCreation
        && this.nbMsgsReceiveSinceCreation == obj.nbMsgsReceiveSinceCreation
        && this.nbMsgsSentToDMQSinceCreation == obj.nbMsgsSentToDMQSinceCreation && this.period == obj.period
        && Arrays.equals(this.rights, obj.rights) && this.freeReading == obj.freeReading
        && this.freeWriting == obj.freeWriting && this.threshold == obj.threshold
        && this.waitingRequestCount == obj.waitingRequestCount
        && this.pendingMessageCount == obj.pendingMessageCount
        && this.deliveredMessageCount == obj.deliveredMessageCount && this.nbMaxMsg == obj.nbMaxMsg;

    return eq;

  }
}