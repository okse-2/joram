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

import com.scalagent.appli.shared.UserWTO;
import com.smartgwt.client.widgets.grid.ListGridRecord;

/**
 * @author Yohann CINTRE
 */
public class UserListRecord extends ListGridRecord {

  public static String ATTRIBUTE_NAME = "name";
  public static String ATTRIBUTE_PERIOD = "period";
  public static String ATTRIBUTE_NBMSGSSENTTODMQSINCECREATION = "nbMsgsSentToDMQSinceCreation";
  public static String ATTRIBUTE_SUBSCRIPTIONNAMES = "subscriptionNames";

  private UserWTO user;

  public UserListRecord() {
  }

  public UserListRecord(UserWTO user) {
    super();

    setName(user.getId());
    setPeriod((int) user.getPeriod());
    setNbMsgsSentToDMQSinceCreation((int) user.getNbMsgsSentToDMQSinceCreation());
    setSubscriptionNames(user.getSubscriptionNames());

    this.user = user;
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
    buffer.append("; period: " + getAttribute(ATTRIBUTE_PERIOD));
    buffer.append("; nbMsgsSentToDMQSinceCreation: " + getAttribute(ATTRIBUTE_NBMSGSSENTTODMQSINCECREATION));
    buffer.append("; DMQsubscriptionNamesid: " + getAttribute(ATTRIBUTE_SUBSCRIPTIONNAMES));
    return buffer.toString();
  }

  public UserWTO getUser() {
    return user;
  }

  public void setUser(UserWTO user) {
    this.user = user;
  }

  public void setName(String name) {
    setAttribute(ATTRIBUTE_NAME, name);
  }

  public void setPeriod(int period) {
    setAttribute(ATTRIBUTE_PERIOD, period);
  }

  public void setNbMsgsSentToDMQSinceCreation(int nbMsgsSentToDMQSinceCreation) {
    setAttribute(ATTRIBUTE_NBMSGSSENTTODMQSINCECREATION, nbMsgsSentToDMQSinceCreation);
  }

  public void setSubscriptionNames(String[] subscriptionNames) {
    setAttribute(ATTRIBUTE_SUBSCRIPTIONNAMES, subscriptionNames);
  }

  public String getName() {
    return getAttributeAsString(ATTRIBUTE_NAME);
  }

  public long getNbMsgsSentToDMQSinceCreation() {
    return getAttributeAsInt(ATTRIBUTE_NBMSGSSENTTODMQSINCECREATION);
  }

  public long getPeriod() {
    return getAttributeAsInt(ATTRIBUTE_PERIOD);
  }

  public void getNbMsgsSentToDMQSinceCreation(int nbMsgsSentToDMQSinceCreation) {
    setAttribute(ATTRIBUTE_NBMSGSSENTTODMQSINCECREATION, nbMsgsSentToDMQSinceCreation);
  }

  public void getSubscriptionNames(String[] subscriptionNames) {
    setAttribute(ATTRIBUTE_SUBSCRIPTIONNAMES, subscriptionNames);
  }

  public String getAllAtt() {
    String[] allatt = getAttributes();
    String ret = "";
    for (int i = 0; i < allatt.length; i++)
      ret = ret + allatt[i] + " / ";
    return ret;
  }

}