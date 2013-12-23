package elasticity.topics.eval;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

/**
 * On reception, prints message latency.
 * 
 * @author Ahmed El Rheddane
 *
 */
public class Listener implements MessageListener {

	@Override
	public void onMessage(Message msg) {
		try {
			System.out.println(System.currentTimeMillis() - msg.getJMSTimestamp());
		} catch (JMSException e) {}
	}
}
