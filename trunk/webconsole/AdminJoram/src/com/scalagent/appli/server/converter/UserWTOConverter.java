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

import java.util.Map;

import org.objectweb.joram.mom.proxies.ProxyImplMBean;

import com.scalagent.appli.shared.UserWTO;

/**
 * @author Yohann CINTRE
 */
public class UserWTOConverter {

  /**
   * @param key The ID of the user
   * @param user A ProxyImplMBean containing the user info
   * @return A UserWTO object created from the ProxyImplMBean object
   */
  public static UserWTO getUserWTO(String key, ProxyImplMBean user) {
    UserWTO result = new UserWTO(key, null, user.getPeriod(), user.getNbMsgsSentToDMQSinceCreation(),
        user.getSubscriptionNames());
    return result;
  }

  /**
   * @param map Map of ProxyImplMBean
   * @return An Array of UserWTO
   */
  public static UserWTO[] getUserWTOArray(Map<String, ProxyImplMBean> map) {

    UserWTO[] newUsers = new UserWTO[map.size()];

    int i = 0;
    for (String mapKey : map.keySet()) {
      newUsers[i] = UserWTOConverter.getUserWTO(mapKey, map.get(mapKey));
      i++;
    }

    return newUsers;
  }
}