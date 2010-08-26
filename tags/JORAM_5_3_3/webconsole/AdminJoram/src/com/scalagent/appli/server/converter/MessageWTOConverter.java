/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */


package com.scalagent.appli.server.converter;

import java.util.List;

import org.objectweb.joram.mom.messages.MessageView;

import com.scalagent.appli.shared.MessageWTO;


public class MessageWTOConverter {

	/**
	 * @param device
	 * @return a DeviceWTO object created from the DeviceDTO object
	 */
//	public static MessageWTO getMessageWTO(Message msg){	
	public static MessageWTO getMessageWTO(MessageView msg){	

		MessageWTO result = new MessageWTO(
				msg.getId(),
				msg.getExpiration(), 
				msg.getTimestamp(), 
				msg.getDeliveryCount(), 
				msg.getPriority(), 
				msg.getText(), 
				msg.getType(),
				msg.getProperties());

		return result;
	}


	/**
	 * @param devices Array of DeviceDTO
	 * @return An Array of DeviceWTO
	 */
	public static MessageWTO[] getMessageWTOArray(List<MessageView> msgs) {
		
		try {
			MessageWTO[] newMessagesWTO = new MessageWTO[msgs.size()];

			int i=0;
			for(MessageView itemMsg:msgs) {
				newMessagesWTO[i]= MessageWTOConverter.getMessageWTO(itemMsg);
				i++;
			}
			return newMessagesWTO;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}