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
public class RegulatedReceiverBis {

	public static void main (String argv[]) throws Exception {
		int number = Integer.parseInt(argv[0]);
		System.out.println("[RegulatedReceiver " + number + "]\tStarted...");

		Context ictx = new InitialContext();
		ConnectionFactory cnxF = (ConnectionFactory) ictx.lookup("cf" + number);
		Queue dest = (Queue) ictx.lookup("remote" + number);
		ictx.close();

		Connection cnx = cnxF.createConnection();

		Session session = cnx.createSession(false,Session.AUTO_ACKNOWLEDGE);
		MessageConsumer receiver = session.createConsumer(dest);

		cnx.start();
		
		long wait;
		Message msg = null;
		long start = System.currentTimeMillis();
		
		for(int i = 1; true; i++) {
			for( int j = 0; j < Constants.MSG_PER_ROUND; j++) {
				msg = receiver.receive();
			}
			
			System.out.println("[RegulatedReceiver " + number + "] Last message's latency (ms): " + (msg.getJMSTimestamp() - System.currentTimeMillis()));
			wait = start + Constants.TIME_UNIT*(i+1) - System.currentTimeMillis();
			if (wait > 0)
				Thread.sleep(wait);
		}

		/*
		long duration = System.currentTimeMillis() - Constants.TIMEOUT - start;
		System.out.println("[RegulatedReceiver " + number + "]\tT\t" + duration);
		cnx.close();
		System.out.println("[RegulatedReceiver " + number + "]\tDone.");
		 */
	}

}