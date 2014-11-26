/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2010 - 2012 ScalAgent Distributed Technologies
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
package com.scalagent.appli.server.converter;

import java.util.Collection;

import org.objectweb.joram.mom.proxies.ClientSubscriptionMBean;

import com.scalagent.appli.shared.SubscriptionWTO;

/**
 * @author Yohann CINTRE
 */
public class SubscriptionWTOConverter {

  /**
   * @param sub A ClientSubscriptionMBean containing the subscription info
   * @return A SubscriptionWTO object created from the ClientSubscriptionMBean
   *         object
   */
  public static SubscriptionWTO getSubscriptionWTO(ClientSubscriptionMBean sub) {
    SubscriptionWTO result = new SubscriptionWTO(sub.getName(), (sub.getActive()!=0), sub.getDurable(),
        sub.getNbMaxMsg(), sub.getContextId(), (int) sub.getNbMsgsDeliveredSinceCreation(),
        (int) sub.getNbMsgsSentToDMQSinceCreation(), sub.getPendingMessageCount(), sub.getSelector(),
        sub.getSubRequestId());

    return result;
  }

  /**
   * @param lst A Collection of ClientSubscriptionMBean
   * @return An array of SubscriptionWTO
   */
  public static SubscriptionWTO[] getSubscriptionWTOArray(Collection<ClientSubscriptionMBean> lst) {
    SubscriptionWTO[] newSubWTO = new SubscriptionWTO[lst.size()];

    int i = 0;
    for (ClientSubscriptionMBean sub : lst) {
      newSubWTO[i] = SubscriptionWTOConverter.getSubscriptionWTO(sub);
      i++;
    }

    return newSubWTO;
  }
}