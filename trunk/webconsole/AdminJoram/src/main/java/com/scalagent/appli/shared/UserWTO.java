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

import com.scalagent.engine.shared.BaseWTO;

/**
 * @author Yohann CINTRE
 */
public class UserWTO extends BaseWTO {

  private String password;
  private long period;
  private long nbMsgsSentToDMQSinceCreation;
  private String[] subscriptionNames;

  public String getPassword() {
    return password;
  }

  public long getPeriod() {
    return period;
  }

  public long getNbMsgsSentToDMQSinceCreation() {
    return nbMsgsSentToDMQSinceCreation;
  }

  public String[] getSubscriptionNames() {
    return subscriptionNames;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public void setPeriod(long period) {
    this.period = period;
  }

  public void setNbMsgsSentToDMQSinceCreation(long nbMsgsSentToDMQSinceCreation) {
    this.nbMsgsSentToDMQSinceCreation = nbMsgsSentToDMQSinceCreation;
  }

  public void setSubscriptionNames(String[] subscriptionNames) {
    this.subscriptionNames = subscriptionNames;
  }

  public UserWTO() {
  }

  public UserWTO(String name, String password, long period, long nbMsgsSentToDMQSinceCreation,
      String[] subscriptionNames) {
    super();
    this.id = name;
    this.password = password;
    this.period = period;
    this.nbMsgsSentToDMQSinceCreation = nbMsgsSentToDMQSinceCreation;
    this.subscriptionNames = subscriptionNames;
  }

  public String toStringFullContent() {
    return "UserWTO [name=" + id + ", nbMsgsSentToDMQSinceCreation=" + nbMsgsSentToDMQSinceCreation
        + ", period=" + period + ", subscriptionNames=" + Arrays.toString(subscriptionNames) + ", id=" + id
        + "]";
  }

  @Override
  public String toString() {
    return "UserWTO [name=" + id + ", subscriptionNames=" + Arrays.toString(subscriptionNames) + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + (int) (nbMsgsSentToDMQSinceCreation ^ (nbMsgsSentToDMQSinceCreation >>> 32));
    result = prime * result + (int) (period ^ (period >>> 32));
    result = prime * result + Arrays.hashCode(subscriptionNames);
    return result;
  }

  @Override
  public boolean equals(Object anObj) {
    if (anObj == null)
      return false;
    if (anObj == this)
      return true;
    if (!(anObj instanceof UserWTO))
      return false;
    UserWTO obj = (UserWTO) anObj;
    if (obj.id.equals(this.id))
      return true;
    return false;
  }

  @Override
  public UserWTO clone() {

    UserWTO usr = new UserWTO();

    usr.id = id;
    usr.period = period;
    usr.nbMsgsSentToDMQSinceCreation = nbMsgsSentToDMQSinceCreation;
    usr.subscriptionNames = subscriptionNames;

    return usr;
  }

  @Override
  public boolean equalsContent(Object anObj) {

    if (!equals(anObj))
      return false;

    UserWTO obj = (UserWTO) anObj;

    boolean eq = equalsWithNull(this.id, obj.id) && this.period == obj.period
        && this.nbMsgsSentToDMQSinceCreation == obj.nbMsgsSentToDMQSinceCreation
        && Arrays.equals(subscriptionNames, obj.subscriptionNames);

    return eq;
  }
}