/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.server.converter;

import java.util.List;

import org.objectweb.joram.mom.proxies.ClientSubscriptionMBean;

import com.scalagent.appli.shared.SubscriptionWTO;


public class SubscriptionWTOConverter {
	
	/**
 	* @param device
 	* @return a DeviceWTO object created from the DeviceDTO object
 	*/
	public static SubscriptionWTO getSubscriptionWTO(ClientSubscriptionMBean sub){	
//				
		
		SubscriptionWTO result = new SubscriptionWTO(sub.getName(), sub.getActive(),
				sub.getDurable(), sub.getNbMaxMsg(), sub.getContextId(),
				(int)sub.getNbMsgsDeliveredSinceCreation(), (int)sub.getNbMsgsSentToDMQSinceCreation(),
				sub.getPendingMessageCount(), sub.getSelector(), sub.getSubRequestId());
				
		return result;
	}
  
 

  /**
   * @param devices Array of DeviceDTO
   * @return An Array of DeviceWTO
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