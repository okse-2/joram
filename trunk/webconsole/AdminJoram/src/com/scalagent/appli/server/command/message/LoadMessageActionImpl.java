/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.server.command.message;


import java.util.List;

import com.google.gwt.core.ext.typeinfo.NotFoundException;
import com.scalagent.appli.client.command.message.LoadMessageAction;
import com.scalagent.appli.client.command.message.LoadMessageResponse;
import com.scalagent.appli.server.RPCServiceCache;
import com.scalagent.appli.shared.MessageWTO;
import com.scalagent.engine.server.command.ActionImpl;


public class LoadMessageActionImpl 
extends ActionImpl<LoadMessageResponse, LoadMessageAction, RPCServiceCache>{


	@Override
	public LoadMessageResponse execute(RPCServiceCache cache, LoadMessageAction action) throws Exception {

		List<MessageWTO> messages;
		try {
			messages = cache.getMessages(this.getHttpSession(), action.getQueueName());
		} catch (NotFoundException e) {
			System.out.println("### engine.server.command.queue.LoadMessageActionImpl.execute : aucun messages recupérés sur le serveur (queue not found)");
			return new LoadMessageResponse(null, action.getQueueName(), false);
		}
		System.out.println("### engine.server.command.queue.LoadMessageActionImpl.execute : "+messages.size()+" messages recupérés sur le serveur");
		return new LoadMessageResponse(messages, action.getQueueName(), true);
	}

}
