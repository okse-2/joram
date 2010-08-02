/**
 * (c)2010 Scalagent Distributed Technologies
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
	 * 
	 * @param key The ID of the user
	 * @param user A ProxyImplMBean containing the user info
	 * @return A UserWTO object created from the ProxyImplMBean object
	 */
	public static UserWTO getUserWTO(String key, ProxyImplMBean user) {
		UserWTO result = new UserWTO(key, user.getPeriod(), user.getNbMsgsSentToDMQSinceCreation(), user.getSubscriptionNames());
		return result;
	}

	/**
	 * @param map Map of ProxyImplMBean
	 * @return An Array of UserWTO
	 */
	public static UserWTO[] getUserWTOArray(Map<String, ProxyImplMBean> map) {

		UserWTO[] newUsers = new UserWTO[map.size()];

		int i=0;
		for (String mapKey : map.keySet()) {
			newUsers[i]= UserWTOConverter.getUserWTO(mapKey, map.get(mapKey));
			i++;
		}

		return newUsers;
	}
}