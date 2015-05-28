/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
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
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s): Ahmed El Rheddane (INRIA)
 */

package elasticity.topics.client;

import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

/**
 * Wraps the standard JMS listener to handle reconnection messages.
 * 
 * @author Ahmed El Rheddane
 *
 */
public class ListenerWrapper implements MessageListener {

	/** Related Subscriber Wrapper. */
	private SubscriberThread st;

	public ListenerWrapper(SubscriberThread st) {
		this.st = st;
	}

	@Override
	public void onMessage(Message m) {
		try {
			if (st.end) {
				// Ignore message (avoids doubles)
				return;
			} else if (m.propertyExists("reconnect")) {
				String tid = m.getStringProperty("reconnect");
				String server = m.getStringProperty("server");
				int port = m.getIntProperty("port");

				Topic topic = Topic.createTopic(tid,null);

				ConnectionFactory cf = TcpConnectionFactory.create(server, port);
				//System.out.println("Should reconnect to:" + cf);
				st.sw.reconnect(topic,cf);
			} else {
				st.sw.listener.onMessage(m);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

}
