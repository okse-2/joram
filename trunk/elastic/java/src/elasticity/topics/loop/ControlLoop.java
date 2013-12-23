package elasticity.topics.loop;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import elasticity.topics.services.ElasticityService;

public class ControlLoop {

	public static void main(String[] args) throws Exception {
		ElasticityService es = new ElasticityService();
		es.init(null);

		BufferedReader br = 
				new BufferedReader(new InputStreamReader(System.in));
		
		while(true){
			String cmd = br.readLine();
			System.out.println("#" + cmd + "#");
			if (cmd.equals("monitor")) {
				es.monitorTopics();
			} else if (cmd.equals("balance")) {
				es.balanceSubscribers();
			} else if (cmd.equals("exit")) {
				break;
			}
		}
	}

}