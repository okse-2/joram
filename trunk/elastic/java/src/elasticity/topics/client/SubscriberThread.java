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

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.Topic;

/**
 * Defines a JMS subscriber's execution.
 * 
 * @author Ahmed El Rheddane
 *
 */
public class SubscriberThread extends Thread {
	Topic topic;
	ConnectionFactory cf;
	SubscriberWrapper sw;
	ListenerWrapper lw;

	boolean end;

	public SubscriberThread(Topic topic, ConnectionFactory cf, SubscriberWrapper sw) {
		this.topic = topic;
		this.cf = cf;
		this.sw = sw;
		this.lw = new ListenerWrapper(this);
		end = false;
	}

	@Override
	public void run() {
		try {
			Connection cnx = cf.createConnection();
			Session sess = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
			MessageConsumer c = sess.createConsumer(topic);
			c.setMessageListener(lw);
			cnx.start();

			//System.out.println("(Re)connected to: " + cf);
			sw.reconnected();

			while (!end) {
				Thread.sleep(1000);
			}

			cnx.close();
			//System.out.println("Disconnected from: " + cf);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void terminate() {
		end = true;
	}
}
