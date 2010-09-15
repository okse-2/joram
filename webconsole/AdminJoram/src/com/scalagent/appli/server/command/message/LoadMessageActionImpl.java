/**
 * (c)2010 Scalagent Distributed Technologies
 */

package com.scalagent.appli.server.command.message;

import java.util.List;

import com.scalagent.appli.client.command.message.LoadMessageAction;
import com.scalagent.appli.client.command.message.LoadMessageResponse;
import com.scalagent.appli.server.RPCServiceCache;
import com.scalagent.appli.shared.MessageWTO;
import com.scalagent.engine.server.command.ActionImpl;

/**
 * @author Yohann CINTRE
 */
public class LoadMessageActionImpl 
extends ActionImpl<LoadMessageResponse, LoadMessageAction, RPCServiceCache>{


	@Override
	public LoadMessageResponse execute(RPCServiceCache cache, LoadMessageAction action) throws Exception {

		List<MessageWTO> messages;
		try {
			messages = cache.getMessages(this.getHttpSession(), action.getQueueName());
		} catch (Exception e) {
			return new LoadMessageResponse(null, action.getQueueName(), false);
		}
		return new LoadMessageResponse(messages, action.getQueueName(), true);
	}

}
