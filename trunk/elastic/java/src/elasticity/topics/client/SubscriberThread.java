package elasticity.topics.client;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.Topic;


public class SubscriberThread extends Thread {
	Topic topic;
	ConnectionFactory cf;
	SubscriberWrapper sw;
	
	boolean end;

	public SubscriberThread(Topic topic, ConnectionFactory cf, SubscriberWrapper sw) {
		this.topic = topic;
		this.cf = cf;
		this.sw = sw;
		
		end = false;
	}

	@Override
	public void run() {
		try {
			Connection cnx = cf.createConnection();
			Session sess = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
			MessageConsumer c = sess.createConsumer(topic);
			c.setMessageListener(new ListenerWrapper(sw));
			cnx.start();
			
			System.out.println("Matrix Reloaded.");
			sw.reconnected();
			
			while (!end) {
				Thread.sleep(1000);
			}
			
			cnx.close();
			System.out.println("This is the end, my only friend.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected void terminate() {
		end = true;
	}

}
