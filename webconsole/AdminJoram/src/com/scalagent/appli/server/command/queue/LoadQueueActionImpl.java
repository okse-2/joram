/**
 * (c)2010 Scalagent Distributed Technologies
 */

package com.scalagent.appli.server.command.queue;

import java.util.List;

import com.scalagent.appli.client.command.queue.LoadQueueAction;
import com.scalagent.appli.client.command.queue.LoadQueueResponse;
import com.scalagent.appli.server.RPCServiceCache;
import com.scalagent.appli.shared.QueueWTO;
import com.scalagent.engine.server.command.ActionImpl;

/**
 * @author Yohann CINTRE
 */
public class LoadQueueActionImpl 
extends ActionImpl<LoadQueueResponse, LoadQueueAction, RPCServiceCache> {

	@Override
	public LoadQueueResponse execute(RPCServiceCache cache, LoadQueueAction action) {

		List<QueueWTO> queues = cache.getQueues(this.getHttpSession(), action.isRetrieveAll(), action.isforceUpdate());
		return new LoadQueueResponse(queues);
	}
}