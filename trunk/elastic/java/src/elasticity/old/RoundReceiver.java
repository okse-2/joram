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
public class RoundReceiver {
	
	static Context ictx = null;

	public static void main (String argv[]) throws Exception {
		
		System.out.println("[RoundReceiver]\tStarted...");
		
		//int arg = Integer.parseInt(argv[0]);
		
		ictx = new InitialContext();
		ConnectionFactory cnxF0 = (ConnectionFactory) ictx.lookup("cf0");
		ConnectionFactory cnxF1 = (ConnectionFactory) ictx.lookup("cf" + argv[0]);
		Queue dest = (Queue) ictx.lookup("remote" + argv[0]);
		Queue ack = (Queue) ictx.lookup("ack");
		ictx.close();
		
		Connection cnx1 = cnxF1.createConnection();
		Session session1 = cnx1.createSession(false,Session.AUTO_ACKNOWLEDGE);
		MessageConsumer receiver = session1.createConsumer(dest);
		
		Connection cnx0 = cnxF0.createConnection();
		Session session0 = cnx0.createSession(false,Session.AUTO_ACKNOWLEDGE);
		MessageProducer acker = session0.createProducer(ack);
		
		Message ackmsg = session0.createMessage();
		
		cnx0.start();
		cnx1.start();	
		
		long sum = 0;
		long rstart,round;
		for(int i = 0; i < Constants.NB_OF_ROUNDS; i++) {
			rstart = System.currentTimeMillis();
			for(int j = 0; j < Constants.MSG_PER_ROUND/2; j++) {
				receiver.receive();
			}
			round = System.currentTimeMillis() - rstart;
			sum += round;
			
			acker.send(ackmsg);
			
			
			System.out.println("[RoundReceiver]\t" + i + "\t" + round);
		}
		
		cnx0.close();
		cnx1.close();
		
		System.out.println("[RoundReceiver]\tAVG\t" +
				sum/Constants.NB_OF_ROUNDS);
		System.out.println("[RoundReceiver]\tDone.");
	}
		
}
