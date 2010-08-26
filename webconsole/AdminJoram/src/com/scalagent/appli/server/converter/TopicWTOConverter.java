/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */


package com.scalagent.appli.server.converter;

import java.util.Date;
import java.util.Map;

import org.objectweb.joram.mom.dest.DestinationImplMBean;
import org.objectweb.joram.mom.dest.TopicImplMBean;

import com.scalagent.appli.shared.TopicWTO;


public class TopicWTOConverter {
	
	/**
 	* @param device
 	* @return a DeviceWTO object created from the DeviceDTO object
 	*/
	public static TopicWTO getDeviceWTO(String key, DestinationImplMBean topic){
		TopicWTO result = new TopicWTO(key, new Date(topic.getCreationTimeInMillis()), ((TopicImplMBean)topic).getSubscriberIds(), topic.getDMQId(),
			  topic.getDestinationId(), topic.getNbMsgsDeliverSinceCreation(), topic.getNbMsgsReceiveSinceCreation(),
			  topic.getNbMsgsSentToDMQSinceCreation(), topic.getPeriod(), topic.getRights(), 
			  topic.isFreeReading(), topic.isFreeWriting());
		return result;
	}
  
 

  /**
   * @param devices Array of DeviceDTO
   * @return An Array of DeviceWTO
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