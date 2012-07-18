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
	
	static Context ictx = null;

	/**
	 * Computes weights for 
	 * MAX_WORKER = 100 and
	 * TIME_UNIT = 1000. 
	 * 
	 * @param round
	 * @return
	 */
	public static int computeLoad(int round) {
		if (round < 1100)
			return round / 2;
		if (round < 1400)
			return 550;
		if (round < 1600)
			return 550 + (round - 1400) / 2;
		if (round < 1900)
			return 650;
		if (round < 2100)
			return 650 - (round - 1900) / 2;
		if (round < 2400)
			return 550;
		if (round < 2600)
			return 550 - (round - 2400) / 2;
		if (round < 2900)
			return 450;
		if (round < 3100)
			return 450 + (round - 2900) / 2;
		if (round < 3400)
			return 550;
		if (round < 4500)
			return 550 - (round - 3400) / 2;
		
		return 0;
	}

	public static void main(String argv[]) throws Exception {
		
		int number = Integer.parseInt(argv[0]);
		System.out.println("[RegulatedSender " + number + "]\tStarted...");
		
		ictx = new InitialContext();
		ConnectionFactory cnxF = (ConnectionFactory) ictx.lookup("cf" + number);
		Queue dest = (Queue) ictx.lookup("alias" + number);
		ictx.close();

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
		start = System.currentTimeMillis();
		int rounds = 4600 * 1000 / Constants.TIME_UNIT;
		for(int i = 0; i < 4600; i++) {
			load = computeLoad(i) * 5 / 2;
			for(int j = 0; j < load; j++) {
				sender.send(message);
				if ((j % 10) == 9)
					session.commit();
			}
			System.out.println("[RegulatedSender " + number + "]\t" + i + "\t" + load);
			wait = start + Constants.TIME_UNIT*(i+1) - System.currentTimeMillis();
			if (wait > 0)
				Thread.sleep(wait);
		}
		cnx.close();
		System.out.println("[RegulatedSender]\tDone.");
		
	}
}
