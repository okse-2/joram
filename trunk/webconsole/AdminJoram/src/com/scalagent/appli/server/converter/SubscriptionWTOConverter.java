/**
 * (c)2010 Scalagent Distributed Technologies
 */

package com.scalagent.appli.server.converter;

import java.util.List;

import org.objectweb.joram.mom.proxies.ClientSubscriptionMBean;

import com.scalagent.appli.shared.SubscriptionWTO;

/**
 * @author Yohann CINTRE
 */
public class SubscriptionWTOConverter {

	/**
	 * 
	 * @param sub A ClientSubscriptionMBean containing the subscription info
	 * @return A SubscriptionWTO object created from the ClientSubscriptionMBean object
	 */
	public static SubscriptionWTO getSubscriptionWTO(ClientSubscriptionMBean sub){	
		SubscriptionWTO result = new SubscriptionWTO(sub.getName(), sub.getActive(),
				sub.getDurable(), sub.getNbMaxMsg(), sub.getContextId(),
				(int)sub.getNbMsgsDeliveredSinceCreation(), (int)sub.getNbMsgsSentToDMQSinceCreation(),
				sub.getPendingMessageCount(), sub.getSelector(), sub.getSubRequestId());

		return result;
	}

	/**
	 * 
	 * @param lst A List of ClientSubscriptionMBean
	 * @return An array of SubscriptionWTO
	 */
	public static SubscriptionWTO[] getSubscriptionWTOArray(List<ClientSubscriptionMBean> lst) {
		SubscriptionWTO[] newSubWTO = new SubscriptionWTO[lst.size()];

		int i=0;
		for(ClientSubscriptionMBean item : lst) {
			newSubWTO[i]= SubscriptionWTOConverter.getSubscriptionWTO(item);
			i++;
		}

		return newSubWTO;
	}
}