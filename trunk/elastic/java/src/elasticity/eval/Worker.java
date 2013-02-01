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
	
	static int count, load;
	
	static class ReceiveRound extends Thread {
		private MessageConsumer receiver;
		
		public ReceiveRound(Connection cnx, Queue dest) {
			try {
				Session session = cnx.createSession(false,Session.AUTO_ACKNOWLEDGE);
				receiver = session.createConsumer(dest);
			} catch (JMSException e) {
				e.printStackTrace(System.out);
			}
		}

		public void run() {
			try {
				for(count  = 0; count < load; count++)
					receiver.receive();
			} catch (Exception e) {
				e.printStackTrace(System.out);
			}
		}
	}
	
	public static void main (String argv[]) throws Exception {
		int number = Integer.parseInt(argv[0]);
		System.out.println("[Worker " + number + "]\tStarted...");

		Context ictx = new InitialContext();
		ConnectionFactory cnxF = (ConnectionFactory) ictx.lookup("cfw" + number);
		Queue dest = (Queue) ictx.lookup("worker" + number);
		ictx.close();

		Connection cnx = cnxF.createConnection();
		ReceiveRound rr = new ReceiveRound(cnx,dest);
		load = Constants.WORKER_MAX;
		
		
		long wait, rstart;
		cnx.start();
		while(true) {
			rstart = System.currentTimeMillis();
			rr.start();
			rr.join(Constants.WORKER_PERIOD);
			rr.stop();
			rr = new ReceiveRound(cnx,dest);
			System.out.println("[Worker " + number + "]\t" + count);		
			wait = rstart + Constants.WORKER_PERIOD - System.currentTimeMillis();
			if (wait > 0)
				Thread.sleep(wait);
		}
	}
}
