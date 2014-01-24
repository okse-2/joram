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

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.Topic;
import javax.naming.Context;
import javax.naming.InitialContext;

/**
 * Creates subscribers on specified topic.
 * 
 * @author Ahmed El Rheddane
 *
 */
public class Sub {
	
	private static ConnectionFactory cf;
	private static Topic topic;
	
	private static void createConsumer() throws Exception {
		Connection cnx = cf.createConnection();
		Session sess = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
		MessageConsumer c = sess.createConsumer(topic);
		c.setMessageListener(new Listener());
		cnx.start();
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("[Sub] Started...");
		
		int tid = Integer.parseInt(args[0]);
		int nbr = Integer.parseInt(args[1]);
		
		Context ictx = new InitialContext();
	    topic = (Topic) ictx.lookup("t" + tid);
	    cf = (ConnectionFactory) ictx.lookup("cf" + tid);
	    ictx.close();

	    for (int i = 0; i < nbr; i++) {
	    	createConsumer();
	    }
	    
	    //System.out.println("[Sub] 'Enter' to exit..");
	    while(true) {
	    	Thread.sleep(300000);
	    }
	    //System.out.println("[Sub] Done.");
	}
}