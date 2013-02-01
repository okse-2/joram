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
package elasticity.old;

import javax.jms.*;
import javax.naming.*;

/**
 * A receiver client.
 * Receives rounds of messages from a given queue.
 */
public class RegulatedReceiver {

	static class ReceiveRound extends Thread {
		private MessageConsumer receiver;
		
		public ReceiveRound(Connection cnx, Queue dest) {
			try {
				Session session = cnx.createSession(false,Session.AUTO_ACKNOWLEDGE);
				receiver = session.createConsumer(dest);
			} catch (JMSException e) {
				e.printStackTrace(System.out);
			}
		}

		public void run() {
			try {
				for(count  = 0; count < load; count++)
					receiver.receive();
			} catch (Exception e) {
				e.printStackTrace(System.out);
			}
		}
	}

	static int count;
	static int number;
	static int load;

	public static void main(String argv[]) throws Exception {
		number = Integer.parseInt(argv[0]);
		System.out.println("[RegulatedReceiver " + number + "]\tStarted...");
		System.out.println("[RegulatedReceiver " + number + "]\tIter\tCount\tRTime\tTTime");
		
		Context ictx = new InitialContext();
		ConnectionFactory cnxF = (ConnectionFactory) ictx.lookup("cf" + number);
		Queue dest = (Queue) ictx.lookup("remote" + number);
		ictx.close();

		Connection cnx = cnxF.createConnection();
		cnx.start();
		
		if (number == 1)
			load = Constants.MSG_LOAD * 7 / 10;
		else
			load = Constants.MSG_LOAD * 3 / 10;
		
		ReceiveRound rr = new ReceiveRound(cnx,dest);
		
		long wait, start, time, rstart, rtime;
		start = System.currentTimeMillis();
		for (int i = 0; true; i++) {
			rstart = System.currentTimeMillis();
			rr.start();
			rr.join(Constants.TIME_UNIT);
			rr.stop();
			rr = new ReceiveRound(cnx,dest);
			
			if (i == 500) {
				if (number == 2)
					load = Constants.MSG_LOAD * 7 / 10;
				else
					load = Constants.MSG_LOAD * 3 / 10;
			}
			
			rtime = System.currentTimeMillis() - rstart;
			time = System.currentTimeMillis() - start;
			System.out.println("[RegulatedReceiver " + number + "]\t"+ i + "\t" + count + "\t" + rtime + "\t" + time);

			wait = rstart + Constants.TIME_UNIT - System.currentTimeMillis();
			if (wait > 0)
				Thread.sleep(wait);
		}
	}
}
