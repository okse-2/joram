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

package elasticity.topics.eval;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Topic;
import javax.naming.Context;
import javax.naming.InitialContext;

/** 
 * Publishes messages on specified topic.
 * 
 * @author Ahmed El Rheddane
 *
 */
public class Pub {
	public static final int MSG_SIZE = 1000;
	
	public static void main(String[] args) throws Exception {
		System.out.println("[Pub] Started...");
		
		int tid = args.length > 0 ? Integer.parseInt(args[0]) : 0;
		int nbr = args.length > 1 ? Integer.parseInt(args[1]) : 100;
		int prd = args.length > 2 ? Integer.parseInt(args[2]) : 1000;
		
		Context ictx = new InitialContext();
	    Topic topic = (Topic) ictx.lookup("t" + tid);
	    ConnectionFactory cf = (ConnectionFactory) ictx.lookup("cf" + tid);
	    ictx.close();
	    
	    Connection cnx = cf.createConnection();
	    Session sess = cnx.createSession(true, 0);
	    MessageProducer p = sess.createProducer(topic);
	    
	    byte[] content = new byte[MSG_SIZE];
		for (int i = 0; i < MSG_SIZE; i++) {
			content[i] = (byte) i;
		}
		BytesMessage msg = sess.createBytesMessage();
		msg.writeBytes(content);
	    
		cnx.start();
		for (int i = 0; i < nbr; i++) {
			long start = System.currentTimeMillis();
			p.send(msg);
			sess.commit();
			
			System.out.println("Sent message #" + i);
			Thread.sleep(prd - (System.currentTimeMillis() - start));
		}
		cnx.close();
		
	    System.out.println("[Pub] Done.");
	}
}
