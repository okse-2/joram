/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */


package com.scalagent.appli.server.converter;

import java.util.Map;

import org.objectweb.joram.mom.proxies.ProxyImplMBean;

import com.scalagent.appli.shared.UserWTO;


public class UserWTOConverter {

	/**
	 * @param device
	 * @return a DeviceWTO object created from the DeviceDTO object
	 */
	public static UserWTO getUserWTO(String key, ProxyImplMBean user) {
		UserWTO result = new UserWTO(key, user.getPeriod(), user.getNbMsgsSentToDMQSinceCreation(), user.getSubscriptionNames());
		return result;
	}

	/**
	 * @param devices Array of DeviceDTO
	 * @return An Array of DeviceWTO
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