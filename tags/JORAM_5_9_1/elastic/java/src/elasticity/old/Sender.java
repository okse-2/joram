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
 * periodically sends a message to an alias queue.
 */
public class Sender 
{
	static Context ictx = null;

	public static void main(String argv[]) throws Exception {

			
		int number = Integer.parseInt(argv[0]);
		System.out.println("[Sender " + number + "] Started...");
		
		ictx = new InitialContext();
		ConnectionFactory cnxF = (ConnectionFactory) ictx.lookup("cf" + number);
		Queue dest = (Queue) ictx.lookup("remote" + number);
		ictx.close();

		Connection cnx = cnxF.createConnection();
		Session session = cnx.createSession(false,Session.AUTO_ACKNOWLEDGE);
		MessageProducer sender = session.createProducer(dest);

		TextMessage message = session.createTextMessage();
		message.setText("This is one hell of a message !");
		
		cnx.start();
		sender.send(message);
		cnx.close();
		
		System.out.println("[Sender " + number + "] Done.");
	}
}
