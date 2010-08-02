/**
 * (c)2010 Scalagent Distributed Technologies
 */

package com.scalagent.appli.server.converter;

import java.util.List;

import org.objectweb.joram.mom.messages.MessageView;

import com.scalagent.appli.shared.MessageWTO;

/**
 * @author Yohann CINTRE
 */
public class MessageWTOConverter {

	/**
	 * 
	 * @param msg
	 *            a MessageView containing the message info
	 * @return a MessageWTO object created from the MessageView object
	 */
	public static MessageWTO getMessageWTO(MessageView msg) {

		MessageWTO result = new MessageWTO(msg.getId(), msg.getExpiration(),
				msg.getTimestamp(), msg.getDeliveryCount(), msg.getPriority(),
				msg.getText(), msg.getType(), msg.getProperties());

		return result;
	}

	/**
	 * 
	 * @param msgs
	 *            a List of MessageView
	 * @return an array of MessageWTO
	 */
	public static MessageWTO[] getMessageWTOArray(List<MessageView> msgs) {

		try {
			MessageWTO[] newMessagesWTO = new MessageWTO[msgs.size()];

			int i = 0;
			for (MessageView itemMsg : msgs) {
				newMessagesWTO[i] = MessageWTOConverter.getMessageWTO(itemMsg);
				i++;
			}
			return newMessagesWTO;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}