package elasticity.loop;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import elasticity.services.ElasticityService;

/**
 * The elasticity control loop.
 * 
 * @author Ahmed El Rheddane
 */
public class ControlLoop {

	private static final String propFile = "elasticity.properties";

	private static int period;

	public static void main(String args[]) {
		System.out.println("[ControlLoop]\tStarted..");
		
		//Read properties file.
		Properties props = new Properties();
		InputStream reader;
		try {
			reader = new FileInputStream(propFile);
			props.load(reader);
			reader.close();
			
			period = Integer.valueOf(props.getProperty("control_loop_period"));
		} catch (Exception e) {
			System.out.println("ERROR while reading properties file.");
			return;
		}

		
		
		System.out.println("[ControlLoop]\tFetched Properties..");

		//Initialize elasticity server.
		ElasticityService es = new ElasticityService();
		try {
			es.init(props);
		} catch (Exception e) {
			System.out.println("ERROR: couldn't init elasticity service!");
			return;
		}
		
		System.out.println("[ControlLoop]\tInitialized ES..");

		//Begin loop..
		while(true) {
			try {
				Thread.sleep(period);
			} catch (Exception e) {
				System.out.println("ERROR: while sleeping..");
				return;
			}

			try {
				es.monitorWorkers();

				if(es.testScaleDown())
					continue;

				if(es.testScaleUp())
					continue;

				es.updateWeights();
			} catch (Exception e) {
				System.out.println("ERROR: see Elasticity Service log.");
				return;
			}
		}
	}	
}
