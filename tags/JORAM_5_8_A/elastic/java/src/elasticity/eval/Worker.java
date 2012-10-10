package elasticity.eval;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;

/**
 * A worker java client.
 * 
 * @author Ahmed El Rheddane
 */
public class Worker {
	
	public static void main (String argv[]) throws Exception {
		int number = Integer.parseInt(argv[0]);
		System.out.println("[Worker " + number + "]\tStarted...");

		Context ictx = new InitialContext();
		ConnectionFactory cnxF = (ConnectionFactory) ictx.lookup("cfw" + number);
		Queue dest = (Queue) ictx.lookup("worker" + number);
		ictx.close();

		Connection cnx = cnxF.createConnection();
		Session session = cnx.createSession(false,Session.AUTO_ACKNOWLEDGE);
		MessageConsumer receiver = session.createConsumer(dest);

		long wait, rstart;
		cnx.start();
		while(true) {
			rstart = System.currentTimeMillis();
			ReceiveRound rr = new ReceiveRound(receiver,number);
			rr.start();
			rr.join(Constants.TIME_UNIT);

			wait = rstart + Constants.TIME_UNIT - System.currentTimeMillis();
			if (wait > 0)
				Thread.sleep(wait);
		}
	}
}

/**
 * A class used to receive a round of messages.
 */
class ReceiveRound extends Thread {

	private MessageConsumer receiver;
	private int number;

	public ReceiveRound(MessageConsumer receiver, int number) {
		this.receiver = receiver;
		this.number = number;
	}

	public void run() {
		int count = 0;
		for(int j = 0; j < Constants.WORKER_MAX; j++) {
			try {
				receiver.receive();
				count++;
			} catch (JMSException e) {}
		}
		System.out.println("[Worker " + number + "]\t" + count);
	}
}
