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