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

package elasticity.eval;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;

/**
 * A worker java client.
 * 
 * @author Ahmed El Rheddane
 */
public class Worker {
	
	static int count, load;
	
	static class ReceiveRound extends Thread {
		public Exception e;
		private MessageConsumer receiver;
		
		public ReceiveRound(Connection cnx, Queue dest) {
			try {
				Session session = cnx.createSession(false,Session.AUTO_ACKNOWLEDGE);
				receiver = session.createConsumer(dest);
			} catch (JMSException e) {
				this.e = e;
			}
		}

		public void run() {
			try {
				for(count  = 0; count < load; count++)
					receiver.receive();
			} catch (Exception e) {
				this.e = e;
			}
		}
	}
	
	public static void main (String argv[]) throws Exception {
		int number = Integer.parseInt(argv[0]);
		System.out.println("[Worker " + number + "]\tStarted...");

		Context ictx = new InitialContext();
		ConnectionFactory cnxF = (ConnectionFactory) ictx.lookup("cfw" + number);
		Queue dest = (Queue) ictx.lookup("worker" + number);
		ictx.close();

		Connection cnx = cnxF.createConnection();
		ReceiveRound rr = new ReceiveRound(cnx,dest);
		load = Constants.WORKER_MAX;
		
		
		long wait, rstart;
		cnx.start();
		while(true) {
			rstart = System.currentTimeMillis();
			rr.start();
			rr.join(Constants.WORKER_PERIOD);
			rr.stop();
			if (rr.e != null) {
				rr.e.printStackTrace(System.out);
				return;
			}
			rr = new ReceiveRound(cnx,dest);
			System.out.println("[Worker " + number + "]\t" + count);		
			wait = rstart + Constants.WORKER_PERIOD - System.currentTimeMillis();
			if (wait > 0)
				Thread.sleep(wait);
		}
	}
}
