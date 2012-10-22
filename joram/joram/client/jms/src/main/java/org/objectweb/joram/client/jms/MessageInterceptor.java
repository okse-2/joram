/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2009 ScalAgent Distributed Technologies
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
 * Initial developer(s): Abdenbi Benammour
 * Contributor(s): ScalAgent Distributed Technologies
 */
package org.objectweb.joram.client.jms;

import javax.jms.Message;
import javax.jms.Session;

/**
 * Session level message interceptor interface.
 * The <code>MessageInterceptor</code> should be implemented by any class whose
 * instances are intended to intercept either or both following operations within
 * a {@link Session JMS Session}:
 * <ul>
 * <li>{@link MessageProducer sending a message}</li>
 * <li>{@link MessageConsumer receiving a message}</li>
 * </ul>
 * The <code>interceptor</code> can be attached to a {@link Session JMS Session}
 * through configuration (see <code>joramAdmin.xml</code>) as <code>IN</code> 
 * when consuming a message or <code>OUT</code> when producing a message)
 * interceptor.
 * 
 * @author benammoura
 */
public interface MessageInterceptor {
	/**
	 * Handles a message before proceeding.
	 * <p>
	 * By convention, the implementation can modify the original message or the current
	 * runtime context, and return no <code>out</code> value. It also avoids to throw any
	 * exception within this method.
	 * 
	 * @param message the message to handle.
	 * @param session the current session of the JMS interaction
	 */
	public void handle(Message message, Session session);
}
