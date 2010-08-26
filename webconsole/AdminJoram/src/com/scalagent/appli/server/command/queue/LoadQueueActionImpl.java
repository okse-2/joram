/**
 * (c)2010 Scalagent Distributed Technologies
 * @author Yohann CINTRE
 */

package com.scalagent.appli.server.command.queue;

import java.util.List;

import com.scalagent.appli.client.command.queue.LoadQueueAction;
import com.scalagent.appli.client.command.queue.LoadQueueResponse;
import com.scalagent.appli.server.RPCServiceCache;
import com.scalagent.appli.shared.QueueWTO;
import com.scalagent.engine.server.command.ActionImpl;


/**
 *
 * @author sgonzalez
 */
public class LoadQueueActionImpl 
extends ActionImpl<LoadQueueResponse, LoadQueueAction, RPCServiceCache> {

	@Override
	public LoadQueueResponse execute(RPCServiceCache cache, LoadQueueAction action) {

		List<QueueWTO> queues = cache.getQueues(this.getHttpSession(), action.isRetrieveAll(), action.isforceUpdate());
		System.out.println("### engine.server.command.queue.LoadQueueActionImpl.execute : "+queues.size()+" queues recupérés sur le serveur");
		return new LoadQueueResponse(queues);
	}
}