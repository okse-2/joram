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
package com.scalagent.appli.server.converter;

import java.util.Collection;

import org.objectweb.joram.mom.proxies.UserAgentMBean;

import com.scalagent.appli.shared.UserWTO;

/**
 * @author Yohann CINTRE
 */
public class UserWTOConverter {

  /**
   * @param user A UserAgentMBean containing the user info
   * @return A UserWTO object created from the UserAgentMBean object
   */
  public static UserWTO getUserWTO(UserAgentMBean user) {
    UserWTO result = new UserWTO(user.getName(), null, user.getPeriod(),
        user.getNbMsgsSentToDMQSinceCreation(), user.getSubscriptionNames());
    return result;
  }

  /**
   * @param users A collection of UserAgentMBean
   * @return An Array of UserWTO
   */
  public static UserWTO[] getUserWTOArray(Collection<UserAgentMBean> users) {

    UserWTO[] newUsers = new UserWTO[users.size()];

    int i = 0;
    for (UserAgentMBean user : users) {
      newUsers[i] = UserWTOConverter.getUserWTO(user);
      i++;
    }

    return newUsers;
  }
}