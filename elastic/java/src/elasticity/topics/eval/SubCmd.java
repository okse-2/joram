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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;

import javax.jms.ConnectionFactory;
import javax.jms.Topic;
import javax.naming.InitialContext;

import elasticity.topics.client.SubscriberWrapper;


/**
 * Small shell to change to manage subscribers.
 * 
 * @author Ahmed El Rheddane
 *
 */
public class SubCmd {
	private static Topic topic;
	private static ConnectionFactory cf;
	private static ArrayList<SubscriberWrapper> subs;

	public static void add(int n) {
		for (int i = 0; i < n; i++) {
			SubscriberWrapper sw = new SubscriberWrapper(topic,cf,new Listener());
			sw.start();
			subs.add(sw);

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {}
		}
	}

	public static void del(int n) {
		Random gen = new Random();
		for (int i = 0; i < n && !subs.isEmpty(); i++) {
			int r = gen.nextInt(subs.size());
			SubscriberWrapper sw = subs.remove(r);
			sw.stop();
		}
	}

	public static void main(String[] args) throws Exception {
		InitialContext jndiCtx = new InitialContext();
		topic = (Topic) jndiCtx.lookup("t0");
		cf = (ConnectionFactory) jndiCtx.lookup("cf0");
		jndiCtx.close();

		subs = new ArrayList<SubscriberWrapper>();
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		while(true) {
			String[] cmd = (br.readLine()).split(" ");
			if (cmd[0].equals("quit")) {
				return;
			} else if (cmd[0].equals("add")) {
				int n = Integer.parseInt(cmd[1]);
				add(n);
			} else if (cmd[0].equals("del")) {
				int n = Integer.parseInt(cmd[1]);
				del(n);
			} else if (cmd[0].equals("set")) {
				int n = Integer.parseInt(cmd[1]);
				if (n > subs.size()) {
					add(n - subs.size());
				} else {
					del(subs.size() - n);
				}
			}
		}
	}
}
