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

/**
 * A receiver client.
 * Periodically reads messages from the corresponding queue.
 */
public class Receiver
{
	static Context ictx = null;
	
	public static boolean loop = true;

	public static void main(String argv[]) throws Exception
	{
		
		int number = Integer.parseInt(argv[0]);
		System.out.println("\n[Receiver " + number + "] Started...");
		
		ictx = new InitialContext();
		ConnectionFactory cnxF = (ConnectionFactory) ictx.lookup("cf" + number);
		Queue dest = (Queue) ictx.lookup("remote" + number);
		ictx.close();

		Connection cnx = cnxF.createConnection();
		Session session = cnx.createSession(false,Session.AUTO_ACKNOWLEDGE);
		MessageConsumer rec = session.createConsumer(dest);
		
		TextMessage msg;

		cnx.start();
		while(loop) {
			msg = (TextMessage) rec.receive();
			System.out.println("[Receiver " + number + "] Received: " + msg.getText());	
		}
		
		cnx.close();
	}
}
