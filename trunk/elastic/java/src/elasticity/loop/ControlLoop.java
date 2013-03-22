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
			System.out.println("ERROR while reading properties file:");
			e.printStackTrace(System.out);
			return;
		}

		
		
		System.out.println("[ControlLoop]\tFetched Properties..");

		//Initialize elasticity server.
		ElasticityService es = new ElasticityService();
		try {
			es.init(props);
		} catch (Exception e) {
			System.out.println("ERROR: couldn't init elasticity service!");
			e.printStackTrace(System.out);
			return;
		}
		
		System.out.println("[ControlLoop]\tInitialized ES..");

		long start,wait;
		long fix = 0;
		
		//Begin loop..
		while(true) {
			try {
				wait = period - fix;
				if (wait > 0) {
					Thread.sleep(wait);
				} else {
					Thread.sleep(period);
				}
			} catch (Exception e) {
				System.out.println("ERROR: while sleeping..");
				return;
			}
			
			start = System.currentTimeMillis();
			try {
				
				es.monitorWorkers();

				if(!es.testScaleDown())
					if(es.testScaleUp())
						continue;
				
				es.updateWeights();
			} catch (Exception e) {
				System.out.println("ERROR: see Elasticity loop log..");
				e.printStackTrace(System.out);
				return;
			}
			fix = System.currentTimeMillis() - start;
			
			System.out.println("INFO: " + fix);
			
		}
	}	
}
