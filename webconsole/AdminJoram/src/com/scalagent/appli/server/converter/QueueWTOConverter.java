/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.server.converter;

import java.util.Date;
import java.util.Map;

import org.objectweb.joram.mom.dest.DestinationImplMBean;
import org.objectweb.joram.mom.dest.QueueImplMBean;

import com.scalagent.appli.shared.QueueWTO;


public class QueueWTOConverter {
	
	/**
 	* @param device
 	* @return a DeviceWTO object created from the DeviceDTO object
 	*/
	public static QueueWTO getQueueWTO(String key, DestinationImplMBean queue){	
		QueueWTO result = new QueueWTO(key, new Date(queue.getCreationTimeInMillis()), queue.getDMQId(),
			  queue.getDestinationId(), queue.getNbMsgsDeliverSinceCreation(), queue.getNbMsgsReceiveSinceCreation(),
			  queue.getNbMsgsSentToDMQSinceCreation(), queue.getPeriod(), queue.getRights(), 
			  queue.isFreeReading(), queue.isFreeWriting(), ((QueueImplMBean)queue).getThreshold(),
			  ((QueueImplMBean)queue).getWaitingRequestCount(), ((QueueImplMBean)queue).getPendingMessageCount(),
			  ((QueueImplMBean)queue).getDeliveredMessageCount(), ((QueueImplMBean)queue).getNbMaxMsg());
		return result;
	}
  

  /**
   * @param devices Array of DeviceDTO
   * @return An Array of DeviceWTO
   */
  	public static QueueWTO[] getQueueWTOArray(Map<String, DestinationImplMBean> map) {
 
  		int nbQueue=0;
    	for (String mapKey : map.keySet()) {

    		if(map.get(mapKey) instanceof QueueImplMBean) {
    			nbQueue++;
      		}	
    	}

    	
    	QueueWTO[] newQueuesWTO = new QueueWTO[nbQueue];
	    
   	    int i=0;
	    for (String mapKey : map.keySet()) {
	    	if(map.get(mapKey) instanceof QueueImplMBean) {
	    		newQueuesWTO[i]= QueueWTOConverter.getQueueWTO(mapKey, map.get(mapKey));
		    	i++;
		    }
		}

	    return newQueuesWTO;
  }

}