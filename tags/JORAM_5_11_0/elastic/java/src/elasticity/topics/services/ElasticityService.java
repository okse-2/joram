/*
 *  JORAM: Java(TM) Open Reliable Asynchronous Messaging
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

package elasticity.topics.services;

import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;

import javax.jms.ConnectionFactory;
import javax.naming.InitialContext;

import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import elasticity.interfaces.Service;

/**
 * Topics' elasticity operations.
 * 
 * @author Ahmed El Rheddane
 *
 */
public class ElasticityService extends Service {

	/** A link to the Joram service. */
	private JoramService js;

	/** max number of subscribers per topic. */
	private int max;

	/** min number of topics. */
	private int min;

	/** Elastic topic, to which admin requests are sent. */
	private Topic et;

	/** List of topics linked to root. */
	private ArrayList<Topic> topics = new ArrayList<Topic>();

	/** Last monitored number of subscribers per topic. */
	private ArrayList<Integer> subs = new ArrayList<Integer>();

	/** Last monitored total number of subscribers. */
	private int sum;

	@Override
	protected void initService(Properties props) throws Exception {
		logger.log(Level.FINE, "Started Initialization..");
		//Setting the admin connection once and for all.
		ConnectionFactory cfa = TcpConnectionFactory.create("localhost",16000);
		AdminModule.connect(cfa,"root","root");

		//Initializes the service beneath.
		js = new JoramService();
		try {
			js.init(props);
		} catch (Exception e) {
			e.printStackTrace();
			logger.log(Level.SEVERE,"Error while initializing Joram Service!");
			throw e;
		}

		//Get the properties
		min = Integer.parseInt(props.getProperty("init_topics"));
		max = Integer.parseInt(props.getProperty("max_sub_topic"));

		//Set initial topics
		InitialContext jndiCtx = new InitialContext();
		et = (Topic) jndiCtx.lookup("t0");
		for (int i = 1; i <= min; i++) {
			topics.add((Topic) jndiCtx.lookup("t" + i));
			subs.add(0);
		}
		jndiCtx.close();

		logger.log(Level.INFO,"Initialization completed.");
	}

	/**
	 * Monitors the number of client subscriptions per topic.
	 * Updates both 'subs' and 'sum'.
	 * 
	 * @throws Exception
	 */
	public void monitorTopics() throws Exception {
		sum = 0;
		String str = "";
		for (int i = 0; i < topics.size(); i++) {
			int s = topics.get(i).getSubscriptions();
			subs.set(i,s);
			sum += s;
			str = str + s + ";";
		}
		str = "Subs;" + System.currentTimeMillis() + ";" + str;
		logger.log(Level.INFO,str);
	}

	/**
	 * Balances subscribers over all topics except topic of index s.
	 * If index is -1, uses all topics.
	 * 
	 * @param s topic's index.
	 * 
	 * @throws Exception
	 */
	private void balanceSubscribers(int s) throws Exception {
		if (topics.isEmpty()) {
			return;
		}

		int size = topics.size();
		if (s != -1) {
			size--;
		}

		//Compute the overload and underload of each topic.
		int avg = sum / size;
		ArrayList<Integer> more = new ArrayList<Integer>();
		ArrayList<Integer> even = new ArrayList<Integer>();
		ArrayList<Integer> less = new ArrayList<Integer>();
		for (int i = 0; i < topics.size(); i++) {
			if (i == s) {
				more.add(i);
			} else if (subs.get(i) > avg) {
				more.add(i);
				subs.set(i, subs.get(i) - avg);
			} else if (subs.get(i) == avg) {
				even.add(i);
				subs.set(i,0);
			} else {
				less.add(i);
				subs.set(i, avg - subs.get(i));
			}
		}

		//Take the extra 'mod' subs into account.
		int mod = sum % size;
		for (int i = 0; mod > 0 && i < more.size(); i++) {
			int a = more.get(i);
			int x = subs.get(a);
			if (a != s) {
				subs.set(a, x - 1);
				mod--;
			}
		}

		less.addAll(even);
		for (int i = 0; mod > 0 && i < less.size(); i++) {
			int b = less.get(i);
			int y = subs.get(b);
			subs.set(b, y + 1);
			mod--;
		}

		//Distribute extra subscribers over underloaded topics.
		for (int i = 0, j = 0; i < more.size(); i++) {
			int a = more.get(i);
			int x = subs.get(a);

			String param = a + ":";
			while (x > 0) {
				int b = less.get(j);
				int y = subs.get(b);
				if (y > x) {
					param = param + b + ";" + x + ";";
					subs.set(b, y - x);
					x = 0;
				} else {
					param = param + b + ";" + y + ";";
					x -= y;
					j += 1;
				}
			}
			if (!param.equals( a + ":")) {
				logger.log(Level.FINE,param);
				et.scale(0, param);
			}
		}
	}

	/**
	 * Balances subscribers evenly over all topics.
	 * 
	 * @throws Exception
	 */
	public void balanceSubscribers() throws Exception {
		balanceSubscribers(-1);
	}

	/**
	 * Adds one topic, if necessary.
	 * 
	 * @return true, if and only if there has been a scale out.
	 */
	public boolean testScaleOut() throws Exception {
		if (sum <= max * topics.size())
			return false;

		logger.log(Level.INFO,"Adding new topic..");
		try {
			Topic t = js.addTopic();
			topics.add(t);
			subs.add(0);
		} catch (Exception e) {
			logger.log(Level.SEVERE,"Error while trying to add a topic!");
			throw e;
		}

		balanceSubscribers(-1);

		logger.log(Level.INFO,"New topic added successfully.");
		return true;
	}

	/**
	 * Removes the last added topic, if possible.
	 * 
	 * 
	 * @return true, if and only if there has been a scale in.
	 */
	public boolean testScaleIn() throws Exception {
		if (sum > max * (topics.size() - 1) || topics.size() == min)
			return false;

		logger.log(Level.INFO,"Removing extra topic..");
		//Move last topics' subscribers
		balanceSubscribers(topics.size() - 1);

		try {
			topics.remove(topics.size() - 1);
			subs.remove(topics.size() - 1);
			js.removeTopic();

		} catch (Exception e) {
			logger.log(Level.SEVERE,"Error while removing topic!");
			throw e;
		}

		logger.log(Level.INFO,"Removed last added topic!");
		return true;
	}
}