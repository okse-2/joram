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

import java.util.Arrays;
import java.util.Date;

import com.scalagent.engine.shared.BaseWTO;

/**
 * @author Yohann CINTRE
 */
public class TopicWTO extends BaseWTO {

  private Date creationDate = new Date();
  private String[] subscriberIds;
  private String DMQId;
  private String destinationId;
  private long nbMsgsDeliverSinceCreation;
  private long nbMsgsReceiveSinceCreation;
  private long nbMsgsSentToDMQSinceCreation;
  private long period;
  private String[] rights;
  private boolean freeReading;
  private boolean freeWriting;

  public Date getCreationDate() {
    return creationDate;
  }

  public String getCreationDateinString() {
    return creationDate.toString();
  }

  public long getCreationTimeInMillis() {
    return creationDate.getTime();
  }

  public String[] getSubscriberIds() {
    return subscriberIds;
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

  public void setCreationDate(Date date) {
    this.creationDate = date;
  }

  public void setSubscriberIds(String[] subscriberIds) {
    this.subscriberIds = subscriberIds;
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

  public TopicWTO(String name, Date creationDate, String[] subscriberIds, String DMQId, String destinationId,
      long nbMsgsDeliverSinceCreation, long nbMsgsReceiveSinceCreation, long nbMsgsSentToDMQSinceCreation,
      long period, String[] rights, boolean freeReading, boolean freeWriting) {

    this.id = name;
    this.creationDate = creationDate;
    this.subscriberIds = subscriberIds;
    this.DMQId = DMQId;
    this.destinationId = destinationId;
    this.nbMsgsDeliverSinceCreation = nbMsgsDeliverSinceCreation;
    this.nbMsgsReceiveSinceCreation = nbMsgsReceiveSinceCreation;
    this.nbMsgsSentToDMQSinceCreation = nbMsgsSentToDMQSinceCreation;
    this.period = period;
    this.rights = rights;
    this.freeReading = freeReading;
    this.freeWriting = freeWriting;
  }

  public TopicWTO() {
  }

  @Override
  public String toString() {
    return "[name=" + id + ", creationDate=" + creationDate + " ]";
  }

  public String toStringFullContent() {
    return "[name=" + id + ", creationDate=" + creationDate + ", subscriberIds=" + subscriberIds
        + ", DMQId=" + DMQId + ", nbMsgsDeliverSinceCreation=" + nbMsgsDeliverSinceCreation
        + ", nbMsgsReceiveSinceCreation=" + nbMsgsReceiveSinceCreation + ", nbMsgsSentToDMQSinceCreation="
        + nbMsgsSentToDMQSinceCreation + ", period=" + period + ", rights=" + rights + ", freeReading="
        + freeReading + ", freeWriting=" + freeWriting + " ]";
  }

  @Override
  public boolean equals(Object anObj) {
    if (anObj == null)
      return false;
    if (anObj == this)
      return true;
    if (!(anObj instanceof TopicWTO))
      return false;
    TopicWTO obj = (TopicWTO) anObj;
    if (obj.id == this.id)
      return true;
    return false;
  }

  @Override
  public TopicWTO clone() {

    TopicWTO topic = new TopicWTO();

    topic.setId(this.getId());
    topic.setCreationDate(this.getCreationDate());
    topic.setSubscriberIds(this.getSubscriberIds());
    topic.setDMQId(this.getDMQId());
    topic.setDestinationId(this.getDestinationId());
    topic.setNbMsgsDeliverSinceCreation(this.getNbMsgsDeliverSinceCreation());
    topic.setNbMsgsReceiveSinceCreation(this.getNbMsgsReceiveSinceCreation());
    topic.setNbMsgsSentToDMQSinceCreation(this.getNbMsgsSentToDMQSinceCreation());
    topic.setPeriod(this.getPeriod());
    topic.setRights(this.getRights());
    topic.setFreeReading(this.isFreeReading());
    topic.setFreeWriting(this.isFreeWriting());

    return topic;
  }

  @Override
  public boolean equalsContent(Object anObj) {

    if (!equals(anObj))
      return false;

    TopicWTO obj = (TopicWTO) anObj;

    boolean eq = equalsWithNull(this.id, obj.id) && equalsWithNull(this.creationDate, obj.creationDate)
        && Arrays.equals(this.subscriberIds, obj.subscriberIds) && equalsWithNull(this.DMQId, obj.DMQId)
        && equalsWithNull(this.destinationId, obj.destinationId)
        && this.nbMsgsDeliverSinceCreation == obj.nbMsgsDeliverSinceCreation
        && this.nbMsgsReceiveSinceCreation == obj.nbMsgsReceiveSinceCreation
        && this.nbMsgsSentToDMQSinceCreation == obj.nbMsgsSentToDMQSinceCreation && this.period == obj.period
        && Arrays.equals(this.rights, obj.rights) && this.freeReading == obj.freeReading
        && this.freeWriting == obj.freeWriting;

    return eq;

  }
}