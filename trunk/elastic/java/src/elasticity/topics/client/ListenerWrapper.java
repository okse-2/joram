package elasticity.topics.client;

import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;



public class ListenerWrapper implements MessageListener {
	
	/** Related Subscriber Wrapper. */
	private SubscriberWrapper sw;
	
	public ListenerWrapper(SubscriberWrapper sw) {
		this.sw = sw;
	}
	
	@Override
	public void onMessage(Message m) {
		try {
			if (m.propertyExists("reconnect")) {
				System.out.println("HEY HEY HEY!!");
				String tid = m.getStringProperty("reconnect");
				String server = m.getStringProperty("server");
				int port = m.getIntProperty("port");
				
				Topic topic = Topic.createTopic(tid,null);
				ConnectionFactory cf = TcpConnectionFactory.create(server, port);
				sw.reconnect(topic,cf);
				
			} else if (sw.reconnecting) {
				// Should make sure that each message is 
				// processed only once.
			} else {
				sw.listener.onMessage(m);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

}
