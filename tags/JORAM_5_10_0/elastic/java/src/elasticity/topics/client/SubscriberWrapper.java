package elasticity.topics.client;

import javax.jms.ConnectionFactory;
import javax.jms.MessageListener;
import javax.jms.Topic;

/**
 * Wraps JMS subscriber to handle reconnection.
 * 
 * @author Ahmed El Rheddane
 *
 */
public class SubscriberWrapper {
	Topic topic;
	ConnectionFactory cf;
	
	/**
	 * Listener specified by the user.
	 */
	MessageListener listener;
	
	SubscriberThread t0;
	SubscriberThread t1;
	
	public SubscriberWrapper(Topic topic, ConnectionFactory cf, MessageListener listener) {
		this.topic = topic;
		this.cf = cf;
		this.listener = listener;
	}
	
	public void start() {
		this.t0 = new SubscriberThread(topic,cf,this);
		t0.start();
	}
	
	public void stop() {
		if (t0 != null) {
			t0.terminate();
		}
		
		if (t1 != null) {
			t1.terminate();
		}
	}
	
	void reconnect(Topic topic, ConnectionFactory cf) {
		t1 = t0;
		t0 = new SubscriberThread(topic,cf,this);
		t0.start();
	}
	
	void reconnected() {
		if (t1 != null) {
			t1.terminate();
		}
	}
}