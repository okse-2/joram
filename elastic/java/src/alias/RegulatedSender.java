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
package alias;

import javax.jms.*;
import javax.naming.*;

public class RegulatedSender {
	
	static Context ictx = null;

	public static int computeLoad(int round) {
		/*int unit = 90; 
		if (round < 100)
			return unit;
		if (round < 200)
			return unit*2;
		if (round < 300)
			return unit*3;
		if (round < 400)
			return unit*2;
		
		return unit;*/
		if (round < 600)
			return round / 2;
		if (round < 800)
			return 300;
		
		return 300 - (round - 800) / 2; 
	}
	
	public static void main(String argv[]) throws Exception {
		
		System.out.println("[RegulatedSender]\tStarted...");
		
		ictx = new InitialContext();
		ConnectionFactory cnxF = (ConnectionFactory) ictx.lookup("cf0");
		Queue dest = (Queue) ictx.lookup("alias");
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
		for(int i = 0; i < 1400; i++) {
			load = computeLoad(i);
			for(int j = 0; j < load; j++) {
				sender.send(message);
				if ((j % 10) == 9)
					session.commit();
			}
			System.out.println("[RegulatedSender]\t" + i);
			wait = start + Constants.TIME_UNIT*i - System.currentTimeMillis();
			if (wait > 0)
				Thread.sleep(wait);
		}
		cnx.close();
		System.out.println("[RegulatedSender]\tDone.");
		
	}
}
