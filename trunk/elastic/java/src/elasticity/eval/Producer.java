package elasticity.eval;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;

/**
 * A producer java client.
 * 
 * @author Ahmed El Rheddane
 */
public class Producer {

	/*
	 * Comuptes the load/s for each round.
	 * considering WORKER_MAX = 100.
	 * 
	 * @param round Current round.
	 * @return Computed load.
	 *
	public static int computeLoad(int round) {
		if (round < 1100)
			return round / 2;
		if (round < 1400)
			return 550;
		if (round < 1600)
			return 550 + (round - 1400) / 2;
		if (round < 1900)
			return 650;
		if (round < 2100)
			return 650 - (round - 1900) / 2;
		if (round < 2400)
			return 550;
		if (round < 2600)
			return 550 - (round - 2400) / 2;
		if (round < 2900)
			return 450;
		if (round < 3100)
			return 450 + (round - 2900) / 2;
		if (round < 3400)
			return 550;
		if (round < 4500)
			return 550 - (round - 3400) / 2;

		return 0;
	}*/
	
	public static int computeLoad(int round) {
		if (round < 1800)
			return round / 25;
		if (round < 2200)
			return 72;
		if (round < 4000)
			return 72 - (round - 2200) / 25;
		
		return 0;
	}

	public static void main(String argv[]) throws Exception {

		int number = Integer.parseInt(argv[0]);
		System.out.println("[Producer " + number + "]\tStarted...");

		Context ictx = new InitialContext();
		ConnectionFactory cnxF = (ConnectionFactory) ictx.lookup("cfp" + number);
		Queue dest = (Queue) ictx.lookup("producer" + number);
		ictx.close();

		Connection cnx = cnxF.createConnection();
		Session session = cnx.createSession(true,Session.SESSION_TRANSACTED);
		MessageProducer sender = session.createProducer(dest);

		byte[] content = new byte[Constants.MSG_SIZE];
		for (int i = 0; i< Constants.MSG_SIZE; i++)
			content[i] = (byte) i;

		BytesMessage message = session.createBytesMessage();
		message.writeBytes(content);

		long start, wait;
		int load;
		cnx.start();

		start = System.currentTimeMillis();
		for(int i = 0;true; i++) {
			load = computeLoad(i) / 2;
			load += (number == 1) ? computeLoad(i) % 2 : 0;
			for(int j = 0; j < load; j++)
				sender.send(message);
			session.commit();
			
			if (i % 10 == 0)
				System.out.println("[Producer " + number + "]\t" + i / 10 + "\t" + computeLoad(i)*10);
			
			wait = start + Constants.PRODUCER_PERIOD * (i+1) - System.currentTimeMillis();
			if (wait > 0)
				Thread.sleep(wait);
		}
	}
}
