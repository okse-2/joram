/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2010 - 2013 ScalAgent Distributed Technologies
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA.
 *
 * Initial developer(s): ScalAgent Distributed Technologies
 */
package org.objectweb.joram.mom.util;

import java.util.Properties;

import org.objectweb.joram.shared.messages.Message;

public interface MessageInterceptor {

  /**
	 * initialize this interceptor
	 * 
	 * @param agentId the string representation of agentId
   * @param agentName the agent name
	 * @param properties the properties come from the client
	 */
	void init(String agentId, String agentName, Properties properties);
	
	/**
	 * Implement this method to intercept message on server side.
	 *  
	 * @param msg  the message 
	 * @param key the connection key, on destination key=-1
	 * @return true if continue with the next interceptor, 
	 *         false send the message in DMQ.
	 */
	boolean handle(Message msg, int key);

}
