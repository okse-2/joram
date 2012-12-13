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
 * A sender client.
 * Sends rounds of messages to a queue.
 */
public class RoundSender 
{
	static Context ictx = null;

	/**
	 * @param argv if argv[0] equals "alias" connects to the AliasQueue, 
	 * 		  else connects to the messages queue directly.
	 * @throws Exception
	 */
	public static void main(String argv[]) throws Exception {
		
		System.out.println("[RoundSender]\tStarted...");
		
		boolean alias = true;
		if (argv.length == 1) {
			int arg = Integer.parseInt(argv[0]);
			alias = (arg == 0);
		}
		
	    byte[] content = new byte[Constants.MSG_SIZE];
	    for (int i = 0; i< Constants.MSG_SIZE; i++)
	    	content[i] = (byte) i;

		ictx = new InitialContext();
		
		ConnectionFactory cnxF = (ConnectionFactory) ictx.lookup("cf0");
		
		Queue dest = null;
		if (alias)
			dest = (Queue) ictx.lookup("alias0");
		else
			dest = (Queue) ictx.lookup("remote1");
		
		Queue ack = (Queue) ictx.lookup("ack");
		ictx.close();

		Connection cnx = cnxF.createConnection();
		
		Session session = cnx.createSession(true,Session.SESSION_TRANSACTED);
		
		MessageConsumer acker = session.createConsumer(ack);
		MessageProducer sender = session.createProducer(dest);

		BytesMessage message = session.createBytesMessage();
		message.writeBytes(content);
		
		cnx.start();
		
		long start = System.currentTimeMillis();
		long sum = 0;
		long rstart,round;
		
		rstart = System.currentTimeMillis();
		for(int j = 0; j < Constants.MSG_PER_ROUND; j++) {
			sender.send(message);
			if ((j % 10) == 9)
				session.commit();
		}
		round = System.currentTimeMillis() - rstart;
		System.out.println("[RoundSender]\t0\t" + round);
		sum += round;
		
		for(int i = 1; i < Constants.NB_OF_ROUNDS; i++) {
			rstart = System.currentTimeMillis();
			for(int j = 0; j < Constants.MSG_PER_ROUND; j++) {
				sender.send(message);
				if ((j % 10) == 9)
					session.commit();
			}
			round = System.currentTimeMillis() - rstart;
			System.out.println("[RoundSender]\t" + i + "\t" + round);
			sum += round;
			acker.receive();
			//acker.receive();

		}
		

		
		long end = System.currentTimeMillis();
		
		cnx.close();

		String version = "D";
		if (alias)
			version = "A";
		
		System.out.println("[RoundSender]\tAVG\t" +
				sum/Constants.NB_OF_ROUNDS);
		System.out.println("[RoundSender]\tMps(" + version + ")\t" +
				(Constants.NB_OF_ROUNDS*Constants.MSG_PER_ROUND)*1000/(end-start));
		System.out.println("[RoundSender]\tDone.");
	}
}
