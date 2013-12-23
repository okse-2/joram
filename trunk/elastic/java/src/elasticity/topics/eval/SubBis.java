package elasticity.topics.eval;

import javax.jms.ConnectionFactory;
import javax.jms.Topic;
import javax.naming.InitialContext;

import elasticity.topics.client.SubscriberWrapper;

public class SubBis {
	public static void main(String[] args) throws Exception {
		int tid = Integer.parseInt(args[0]);
		InitialContext jndiCtx = new InitialContext();
		Topic topic = (Topic) jndiCtx.lookup("t" + tid);
		ConnectionFactory cf = (ConnectionFactory) jndiCtx.lookup("cf" + tid);
		jndiCtx.close();
		SubscriberWrapper sub = new SubscriberWrapper(topic,cf,new Listener());
		sub.start();
		while(true) {
			Thread.sleep(1000);
		}
	}
}
