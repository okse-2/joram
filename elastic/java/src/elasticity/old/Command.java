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

package elasticity.old;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminModule;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;



/**
 * Changes the remote destination of an AliasInQueue.
 */
public class Command {

	public static void main(String args[]) throws Exception {
		String cmd = args[0];
		
		System.out.println("[Command] Executing " + args[0] + "...");
		
		AdminModule.connect("root", "root", 60);

		Context ictx = new InitialContext();
		Queue aq = (Queue) ictx.lookup("alias");
		ConnectionFactory cnxF = (ConnectionFactory) ictx.lookup("cf0");
		
		
		if (cmd.equals("add")) {
			aq.addRemoteDestination(((Queue) ictx.lookup("remote" + args[1])).getName());
		} else if (cmd.equals("del")) {
			aq.delRemoteDestination(((Queue) ictx.lookup("remote" + args[1])).getName());
		} else if (cmd.equals("send")) {
			Connection cnx = cnxF.createConnection();
			Session session = cnx.createSession(false,Session.AUTO_ACKNOWLEDGE);
			MessageProducer sender = session.createProducer(aq);
			TextMessage message = session.createTextMessage();
			cnx.start();
			for(int i = 0; i < Integer.parseInt(args[1]); i++) {
				message.setText("Message number " + i);
				sender.send(message);
			}
			cnx.close();
		} else if (cmd.equals("stop")) {
			try {
				AdminModule.stopServer(Integer.parseInt(args[1]));
			} catch (Exception e) {
				System.out.println("Puroburemu -_-'");
			}
		}
		
		
		ictx.close();
		AdminModule.disconnect();
		System.out.println("[Command] Done.");
	}
}