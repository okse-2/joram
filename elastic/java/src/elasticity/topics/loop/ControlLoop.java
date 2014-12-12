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

package elasticity.topics.loop;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import elasticity.topics.services.ElasticityService;

/**
 * The topics' elasticity control loop.
 * 
 * @author Ahmed El Rheddane
 */
public class ControlLoop {

	private static final String propFile = "elasticity.properties";

	private static int period;

	public static void main(String args[]) {
		System.out.println("[ControlLoop]\tStarted..");

		//Read properties file.
		Properties props = new Properties();
		InputStream reader;
		try {
			reader = new FileInputStream(propFile);
			props.load(reader);
			reader.close();

			period = Integer.valueOf(props.getProperty("control_loop_period"));
		} catch (Exception e) {
			System.out.println("ERROR while reading properties file:");
			e.printStackTrace(System.out);
			return;
		}

		System.out.println("[ControlLoop]\tFetched Properties..");

		//Initialize elasticity service.
		ElasticityService es = new ElasticityService();
		try {
			es.init(props);
		} catch (Exception e) {
			System.out.println("ERROR: couldn't init elasticity service!");
			e.printStackTrace(System.out);
			return;
		}

		long start,wait;
		long fix = 0;

		//Begin loop..
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		while(true) {
			/*try {
				String cmd = br.readLine();
				if (cmd.equals("monitor")) {
					es.monitorTopics();
				} else if (cmd.equals("scalein")) {
					es.testScaleIn();
				} else if (cmd.equals("scaleout")) {
					es.testScaleOut();
				} else if (cmd.equals("balance")) {
					es.balanceSubscribers();
				}
			} catch (Exception e) {
				e.printStackTrace(System.out);
			}*/

			try {
				wait = period - fix;
				if (wait > 0) {
					Thread.sleep(wait);
				} else {
					Thread.sleep(period);
				}
			} catch (Exception e) {
				System.out.println("ERROR: while sleeping..");
				return;
			}

			start = System.currentTimeMillis();
			try {
				es.monitorTopics();
				if (es.testScaleIn())
					continue;

				if (es.testScaleOut())
					continue;

				es.balanceSubscribers();

			} catch (Exception e) {
				System.out.println("ERROR: see Elasticity loop log..");
				e.printStackTrace(System.out);
				return;
			}
			fix = System.currentTimeMillis() - start;

			System.out.println("INFO: " + fix);
		}
	}
}
