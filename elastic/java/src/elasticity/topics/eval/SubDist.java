/*
a *  JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2013 - 2014 ScalAgent Distributed Technologies
 * Copyright (C) 2013 - 2014 Université Joseph Fourier
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
 * Initial developer(s): Université Joseph Fourier
 * Contributor(s): ScalAgent Distributed Technologies
 */

package elasticity.topics.eval;

import java.util.ArrayList;
import java.util.Random;

import javax.jms.ConnectionFactory;
import javax.jms.Topic;
import javax.naming.InitialContext;

import elasticity.topics.client.SubscriberWrapper;

public class SubDist {
	private static Topic topic;
	private static ConnectionFactory cf;
	private static ArrayList<SubscriberWrapper> subs = new ArrayList<SubscriberWrapper>();

	private static int waitStart = 5000;
	private static int waitRound = 2000;

	public static void addSub() {
		SubscriberWrapper sw = new SubscriberWrapper(topic,cf,new Listener());
		sw.start();
		subs.add(sw);
	}

	public static void remSub() {
		Random gen = new Random();
		int r = gen.nextInt(subs.size());
		SubscriberWrapper sw = subs.remove(r);
		sw.stop();
	}

	public static void main(String[] args) throws Exception {
		long start = System.currentTimeMillis();
		int id = Integer.parseInt(args[0]);
		int size = Integer.parseInt(args[1]);
		int pike = Integer.parseInt(args[2]);

		InitialContext jndiCtx = new InitialContext();
		topic = (Topic) jndiCtx.lookup("t0");
		cf = (ConnectionFactory) jndiCtx.lookup("cf0");
		jndiCtx.close();

		try {
			Thread.sleep(5000 * (size - id - 1) + 2000 * id - (System.currentTimeMillis() - start));
		} catch (InterruptedException e) {}

		// Adding
		for (int i = 0; i < pike / size; i++) {
			try {
				Thread.sleep(2000 * size);
			} catch (InterruptedException e) {}

			addSub();
		}

		// Removing
		for (int i = 0; i < pike / size; i++) {
			try {
				Thread.sleep(2000 * size);
			} catch (InterruptedException e) {}

			remSub();
		}
	}
}
