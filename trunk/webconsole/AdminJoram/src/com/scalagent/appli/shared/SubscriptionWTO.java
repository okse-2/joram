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
import java.util.List;

import com.scalagent.engine.shared.BaseWTO;

/**
 * @author Yohann CINTRE
 */
public class SubscriptionWTO extends BaseWTO {

  private boolean active;
  private boolean durable;
  private int nbMaxMsg;
  private int contextId;
  private int nbMsgsDeliveredSinceCreation;
  private int nbMsgsSentToDMQSinceCreation;
  private int pendingMessageCount;
  private String selector;
  private int subRequestId;
  private List<String> messagesList;

  public boolean isActive() {
    return active;
  }

  public boolean isDurable() {
    return durable;
  }

  public int getNbMaxMsg() {
    return nbMaxMsg;
  }

  public int getContextId() {
    return contextId;
  }

  public int getNbMsgsDeliveredSinceCreation() {
    return nbMsgsDeliveredSinceCreation;
  }

  public int getNbMsgsSentToDMQSinceCreation() {
    return nbMsgsSentToDMQSinceCreation;
  }

  public int getPendingMessageCount() {
    return pendingMessageCount;
  }

  public String getSelector() {
    return selector;
  }

  public int getSubRequestId() {
    return subRequestId;
  }

  public List<String> getMessagesList() {
    return messagesList;
  }

  public void clearMessagesList() {
    this.messagesList.clear();
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public void setDurable(boolean durable) {
    this.durable = durable;
  }

  public void setNbMaxMsg(int nbMaxMsg) {
    this.nbMaxMsg = nbMaxMsg;
  }

  public void setContextId(int contextId) {
    this.contextId = contextId;
  }

  public void setNbMsgsDeliveredSinceCreation(int nbMsgsDeliveredSinceCreation) {
    this.nbMsgsDeliveredSinceCreation = nbMsgsDeliveredSinceCreation;
  }

  public void setNbMsgsSentToDMQSinceCreation(int nbMsgsSentToDMQSinceCreation) {
    this.nbMsgsSentToDMQSinceCreation = nbMsgsSentToDMQSinceCreation;
  }

  public void setPendingMessageCount(int pendingMessageCount) {
    this.pendingMessageCount = pendingMessageCount;
  }

  public void setSelector(String selector) {
    this.selector = selector;
  }

  public void addMessageToList(String messageId) {
    messagesList.add(messageId);
  }

  public void removeMessageFromList(String messageId) {
    messagesList.remove(messageId);
  }
  
  public void setSubRequestId(int subRequestId) {
    this.subRequestId = subRequestId;
  }

  public SubscriptionWTO() {
    messagesList = new ArrayList<String>();
  }

  public SubscriptionWTO(String name, boolean active, boolean durable, int nbMaxMsg, int contextId,
      int nbMsgsDeliveredSinceCreation, int nbMsgsSentToDMQSinceCreation, int pendingMessageCount,
      String selector, int subRequestId) {
    super();
    this.id = name;
    this.active = active;
    this.durable = durable;
    this.nbMaxMsg = nbMaxMsg;
    this.contextId = contextId;
    this.nbMsgsDeliveredSinceCreation = nbMsgsDeliveredSinceCreation;
    this.nbMsgsSentToDMQSinceCreation = nbMsgsSentToDMQSinceCreation;
    this.pendingMessageCount = pendingMessageCount;
    this.selector = selector;
    this.subRequestId = subRequestId;
    messagesList = new ArrayList<String>();

  }

  @Override
  public String toString() {
    return "SubscriptionWTO [name=" + id + ", nbMaxMsg=" + nbMaxMsg + ", nbMsgsDeliveredSinceCreation="
        + nbMsgsDeliveredSinceCreation + "]";
  }

  public String toStringFullContent() {
    return "SubscriptionWTO [active=" + active + ", contextId=" + contextId + ", durable=" + durable
        + ", name=" + id + ", nbMaxMsg=" + nbMaxMsg + ", nbMsgsDeliveredSinceCreation="
        + nbMsgsDeliveredSinceCreation + ", nbMsgsSentToDMQSinceCreation=" + nbMsgsSentToDMQSinceCreation
        + ", pendingMessageCount=" + pendingMessageCount + ", selector=" + selector + ", subRequestId="
        + subRequestId + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (active ? 1231 : 1237);
    result = prime * result + contextId;
    result = prime * result + (durable ? 1231 : 1237);
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + nbMaxMsg;
    result = prime * result + nbMsgsDeliveredSinceCreation;
    result = prime * result + nbMsgsSentToDMQSinceCreation;
    result = prime * result + pendingMessageCount;
    result = prime * result + ((selector == null) ? 0 : selector.hashCode());
    result = prime * result + subRequestId;
    return result;
  }

  @Override
  public boolean equals(Object anObj) {
    if (anObj == null)
      return false;
    if (anObj == this)
      return true;
    if (!(anObj instanceof SubscriptionWTO))
      return false;
    SubscriptionWTO obj = (SubscriptionWTO) anObj;
    if (obj.id.equals(this.id))
      return true;
    return false;
  }

  @Override
  public SubscriptionWTO clone() {

    SubscriptionWTO sub = new SubscriptionWTO();

    sub.id = id;
    sub.active = active;
    sub.durable = durable;
    sub.nbMaxMsg = nbMaxMsg;
    sub.contextId = contextId;
    sub.nbMsgsDeliveredSinceCreation = nbMsgsDeliveredSinceCreation;
    sub.nbMsgsSentToDMQSinceCreation = nbMsgsSentToDMQSinceCreation;
    sub.pendingMessageCount = pendingMessageCount;
    sub.selector = selector;
    sub.subRequestId = subRequestId;

    return sub;
  }

  @Override
  public boolean equalsContent(Object anObj) {

    if (!equals(anObj))
      return false;

    SubscriptionWTO obj = (SubscriptionWTO) anObj;

    boolean eq = equalsWithNull(this.id, obj.id)
        && this.active == obj.active && this.durable == obj.durable && this.nbMaxMsg == obj.nbMaxMsg
        && this.contextId == obj.contextId
        && this.nbMsgsDeliveredSinceCreation == obj.nbMsgsDeliveredSinceCreation
        && this.nbMsgsSentToDMQSinceCreation == obj.nbMsgsSentToDMQSinceCreation
        && this.pendingMessageCount == obj.pendingMessageCount && equalsWithNull(this.selector, obj.selector)
        && this.subRequestId == obj.subRequestId;

    return eq;

  }

}