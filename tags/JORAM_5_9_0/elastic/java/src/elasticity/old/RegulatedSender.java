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

public class RegulatedSender {

	/**
	 * Computes weights for 
	 * MAX_WORKER = 100 and
	 * TIME_UNIT = 1000. 
	 * 
	 * @param round
	 * @return
	 */
	public static int computeLoad(int round) {
		if (round < 10)
			return round * Constants.MSG_LOAD / 20;
		return Constants.MSG_LOAD / 2;
	}

	public static void main(String argv[]) throws Exception {
		int number = Integer.parseInt(argv[0]);
		System.out.println("[RegulatedSender " + number + "]\tStarted...");
		
		Context ictx = new InitialContext();
		ConnectionFactory cnxF = (ConnectionFactory) ictx.lookup("cf" + number);
		Queue dest = (Queue) ictx.lookup("alias" + number);
		ictx.close();
		
		System.out.println("[RegulatedSender " + number + "]\tRetrieved context..");
		
		Connection cnx = cnxF.createConnection();
		Session session = cnx.createSession(true,Session.SESSION_TRANSACTED);
		MessageProducer sender = session.createProducer(dest);
		
		byte[] content = new byte[Constants.MSG_SIZE];
	    for (int i = 0; i< Constants.MSG_SIZE; i++)
	    	content[i] = (byte) i;
		
		BytesMessage message = session.createBytesMessage();
		message.writeBytes(content);
		
		long start, wait;
		int load;
		cnx.start();
		
		System.out.println("[RegulatedSender " + number + "]\tReady to send..");
		
		start = System.currentTimeMillis();
		
		for (int i = 0; i < Constants.REG_ROUNDS; i++) {
			load = computeLoad(i);
			for (int j = 0; j < load; j++) {
				sender.send(message);
				if ((j % 10) == 9)
					session.commit();
			}
			
			wait = start + Constants.TIME_UNIT*(i+1) - System.currentTimeMillis();
			if (wait > 0)
				Thread.sleep(wait);
			System.out.println("[RegulatedSender " + number + "]\t" + i + "\t" + load);
		}
		//cnx.close();
		//System.out.println("[RegulatedSender]\tDone.");
	}
}
