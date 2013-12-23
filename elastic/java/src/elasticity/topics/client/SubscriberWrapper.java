package elasticity.topics.client;

import javax.jms.ConnectionFactory;
import javax.jms.MessageListener;
import javax.jms.Topic;

/**
 * Creates subscribers on specified topic.
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
	boolean reconnecting;
	
	SubscriberThread t0;
	SubscriberThread t1;
	
	public SubscriberWrapper(Topic topic, ConnectionFactory cf, MessageListener listener) {
		this.topic = topic;
		this.cf = cf;
		this.listener = listener;
		this.reconnecting = false;
		
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
		
		reconnecting = false;
	}
	
	void reconnect(Topic topic, ConnectionFactory cf) {
		reconnecting = true;
		
		t1 = t0;
		t0 = new SubscriberThread(topic,cf,this);
		t0.start();
	}
	
	void reconnected() {
		reconnecting = false;
		
		if (t1 != null) {
			t1.terminate();
		}
	}
}