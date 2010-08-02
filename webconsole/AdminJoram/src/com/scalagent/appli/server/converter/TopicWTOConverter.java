/**
 * (c)2010 Scalagent Distributed Technologies
 */

package com.scalagent.appli.server.converter;

import java.util.Date;
import java.util.Map;

import org.objectweb.joram.mom.dest.DestinationImplMBean;
import org.objectweb.joram.mom.dest.TopicImplMBean;

import com.scalagent.appli.shared.TopicWTO;

/**
 * @author Yohann CINTRE
 */
public class TopicWTOConverter {
	

	/**
	 * 
	 * @param key
	 *            The ID of the queue
	 * @param queue
	 *            A DestinationImplMBean containing the queue info
	 * @return A QueueWTO object created from the DestinationImplMBean object
	 */
	
	/**
	 * 
	 * @param key The ID of the topic
	 * @param topic A DestinationImplMBean containing the topic info
	 * @return A TopicWTO object created from the DestinationImplMBean object
	 */
	public static TopicWTO getDeviceWTO(String key, DestinationImplMBean topic){
		TopicWTO result = new TopicWTO(key, new Date(topic.getCreationTimeInMillis()), ((TopicImplMBean)topic).getSubscriberIds(), topic.getDMQId(),
			  topic.getDestinationId(), topic.getNbMsgsDeliverSinceCreation(), topic.getNbMsgsReceiveSinceCreation(),
			  topic.getNbMsgsSentToDMQSinceCreation(), topic.getPeriod(), topic.getRights(), 
			  topic.isFreeReading(), topic.isFreeWriting());
		return result;
	}
  
	/**
	 * @param map Map of DestinationImplMBean
	 * @return An Array of TopicWTO
	 */
  	public static TopicWTO[] getTopicWTOArray(Map<String, DestinationImplMBean> map) {
    
  		
  		int nbTopic=0;
    	for (String mapKey : map.keySet()) {
    		
    		if(map.get(mapKey) instanceof TopicImplMBean) {
    			nbTopic++;
    		}	
    	}
    	

    	TopicWTO[] newTopicsWTO = new TopicWTO[nbTopic];
	    
	    int i=0;
	    for (String mapKey : map.keySet()) {
	    		
	    	if(map.get(mapKey) instanceof TopicImplMBean) {
		    	newTopicsWTO[i] =  TopicWTOConverter.getDeviceWTO(mapKey, map.get(mapKey));
		    	i++;
	    	}
		}
  
	    return newTopicsWTO;
  }

}