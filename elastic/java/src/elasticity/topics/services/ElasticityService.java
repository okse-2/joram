package elasticity.topics.services;

import java.util.ArrayList;
import java.util.Properties;

import javax.jms.ConnectionFactory;
import javax.naming.InitialContext;

import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import elasticity.interfaces.Service;

public class ElasticityService extends Service {
	
	private Topic root;
	private ArrayList<Topic> topics = new ArrayList<Topic>();
	private ArrayList<Integer> subs = new ArrayList<Integer>();

	@Override
	protected void initService(Properties props) throws Exception {
		//Setting the admin connection once and for all.
		ConnectionFactory cfa = TcpConnectionFactory.create("localhost",16000);
		AdminModule.connect(cfa,"root","root");
		
		// TEST
		InitialContext jndiCtx = new InitialContext();
		root = (Topic) jndiCtx.lookup("t0");
		for (int i = 1; i <= 3; i++) {
			topics.add((Topic) jndiCtx.lookup("t" + i));
			subs.add(0);
		}
		jndiCtx.close();
	}
	
	public void monitorTopics() throws Exception {
		for (int i = 0; i < topics.size(); i++) {
			int s = topics.get(i).getSubscriptions();
			subs.set(i,s);
			System.out.println("Topic #" + i + " has " + s + " subscribers.");
		}
	}
	
	public void balanceSubscribers() throws Exception {
		int sum = 0;
		for (int i = 0; i < topics.size(); i++) {
			sum += subs.get(i);
		}
		
		int avg = sum / topics.size();
		ArrayList<Integer> more = new ArrayList<Integer>();
		ArrayList<Integer> even = new ArrayList<Integer>();
		ArrayList<Integer> less = new ArrayList<Integer>();
		for (int i = 0; i < topics.size(); i++) {
			if (subs.get(i) > avg) {
				more.add(i);
				subs.set(i, subs.get(i) - avg);
			} else if (subs.get(i) == avg) {
				even.add(i);
				subs.set(i,0);
			} else {
				less.add(i);
				subs.set(i, avg - subs.get(i));
			}
		}
		
		System.out.println("more: " + more);
		System.out.println("even: " + even);
		System.out.println("less: " + less);
		
		less.addAll(even);
		
		int mod = sum % topics.size(); 
		if (mod > more.size()) {
			for (int i = 0; i < mod - more.size(); i++) {
				subs.set(less.get(i), subs.get(less.get(i)) + 1);
			}
			mod = more.size();
		}
		
		for (int i = 0, j = 0; i < more.size(); i++) {
			int a = more.get(i);
			int x = subs.get(a);
			if (mod > 0) {
				x--;
				mod--;
			}
			
			String param = a + ":";
			for (; x > 0; j++) {
				int b = less.get(j);
				int y = subs.get(b);
				if (y > x) {
					param = param + b + ";" + x + ";";
					subs.set(b, y - x);
					x = 0;
				} else {
					param = param + b + ";" + y + ";";
					x -= y;
				}
			}
			if (!param.equals("")) {
				root.scale(0, param);
				System.out.println("Topic " + a + ": " + param);
			}
		}
	}
}