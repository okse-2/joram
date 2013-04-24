package elasticity.services;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

import javax.naming.InitialContext;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminModule;

import elasticity.interfaces.Service;

/**
 * Provides the scaling down/up, monitoring and updating methods
 * for an elasticity control loop.
 * 
 * @author Ahmed El Rheddane
 */
public class ElasticityService extends Service {

	final static String DELIVERED = "DeliveredMessageCount";
	final static String PENDING = "PendingMessageCount";
	
	/** A link to the Joram service. */
	private JoramService js;

	//Involved JMS objects.
	/** The list of producers */
	private List<Queue> producers;

	/** The list of workers connected to producers, cannot be empty. */
	private List<Queue> workers;

	//Monitoring values
	/** Number of delivered messages per active worker, since their creation. */
	private List<Integer> delivered;

	/** Number of delivered messages per active worker, during the last period. */
	private List<Integer> rates;

	/** Number of pending messages per worker. */
	private List<Integer> loads;

	/** Average load of the monitored workers. */
	private int averageLoad;

	//Decision-related values
	private int scaleIn;
	private int downIn;
	private int upIn;

	private int downLimit;
	private int upLimit;

	private int downRate;

	@Override
	public void initService(Properties props) throws Exception {
		logger.log(Level.FINE, "Started Initialization..");
		//Setting the admin connection once and for all.
		AdminModule.connect("localhost",16101,"root","root", 60);
		
		//Initializes the service beneath.
		js = new JoramService();
		try {
			js.init(props);
		} catch (Exception e) {
			e.printStackTrace();
			logger.log(Level.SEVERE,"Error while initializing Amazon Service!");
			throw e;
		}

		//Get the properties..
		scaleIn = Integer.valueOf(props.getProperty("scale_in"));
		upLimit = Integer.valueOf(props.getProperty("up_limit"));
		downLimit = Integer.valueOf(props.getProperty("down_limit"));

		//Initializing the fields..
		producers = new ArrayList<Queue>();
		workers = new ArrayList<Queue>();
		delivered = new ArrayList<Integer>();

		rates = new ArrayList<Integer>();
		loads = new ArrayList<Integer>();

		//Hard-coded setup of initial configuration.
		InitialContext ictx = new InitialContext();
		producers.add((Queue) ictx.lookup("producer1"));
		producers.add((Queue) ictx.lookup("producer2"));
		workers.add((Queue) ictx.lookup("worker1"));
		ictx.close();

		delivered.add(0);
		rates.add(0);
		loads.add(0);

		downIn=-1;
		upIn=0;

		logger.log(Level.INFO,"Initialization completed.");
	}

	/** 
	 * Updates the values of monitoring variables.
	 */
	public void monitorWorkers() throws Exception {
		logger.log(Level.FINE,"Started monitoring..");
		averageLoad = 0;
		try {
			for(int i = 0; i < workers.size(); i++) {
				Queue worker = workers.get(i);
				Hashtable<String,Integer> ht = worker.getStatistics(DELIVERED + "," + PENDING);

				int newDelivered = ht.get(DELIVERED);
				rates.set(i, newDelivered - delivered.get(i));
				delivered.set(i,newDelivered);

				loads.set(i, ht.get(PENDING));
				averageLoad += loads.get(i);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.log(Level.SEVERE, "Couldn't monitor the workers!");
			throw e;
		}

		averageLoad /= workers.size();

		String loadStr = "";
		String rateStr = "";
		for(int i = 0; i < workers.size(); i++) {
			loadStr = loadStr + "\t" + loads.get(i);
			rateStr = rateStr + "\t" + rates.get(i);
		}

		logger.log(Level.INFO,"Monitored loads:" + loadStr + "\t(avg. " + averageLoad  + ")");
		logger.log(Level.INFO,"Monitored rates:" + rateStr);
	}

	/**
	 * Starts, continues or aborts scaling down process based 
	 * on the monitored values.
	 * 
	 * A scaling down plan goes on for 100/downRate rounds before
	 * deleting the last added worker.
	 * 
	 * @return true if scaling down is started or on-going.
	 */
	public boolean testScaleDown() throws Exception {
		logger.log(Level.FINE, "Testing whether to scale down..");
		int last = workers.size()-1;

		//Plan should be cancelled.
		if (averageLoad > downLimit) {
			if (downIn > -1) {
				rates.set(last, downRate);
				downIn = -1;
				//nextScaleUp = minScaleUp;
				logger.log(Level.INFO,"Cancelled scaling down plan.");
			}

			return false;
		}
		
		if (workers.size() <= 1) {
			return false;
		}

		//Should initiate plan.
		if (downIn < 0) {
			downRate = rates.get(last);
			downIn = scaleIn;
		}

		
		//Plan can continue.
		if (downIn-- == 0) {
			//Worker should effectively be removed.
			logger.log(Level.FINE, "Removing extra worker..");

			Queue worker = workers.remove(last);
			rates.remove(last);
			loads.remove(last);
			delivered.remove(last);

			//Notify producers.
			for(Queue producer : producers)
				try {
					producer.delRemoteDestination(worker.getName());
				} catch (Exception e) {
					e.printStackTrace();
					logger.log(Level.SEVERE, "Couldn't delete the remote destination from producers!");
					throw e;
				}

			//Remove from Joram infrastructure.
			try {
				js.delWorker();
			} catch (Exception e) {
				e.printStackTrace();
				logger.log(Level.SEVERE, "Problem while trying to remove the corresponding Joram Server!");
				throw e;
			}

			logger.log(Level.INFO, "Removed extra worker successfully.");
			return false;

		} else {
			//Gradually decrease the rate of the worker to remove (will affect weights computation).
			rates.set(last, downRate * downIn / scaleIn);
			logger.log(Level.INFO,"Removing extra worker in " + (downIn + 1) + "..");
			return true;
		}
	}

	/**
	 * Adds a new worker to the active workers list,
	 * if all workers are overloaded.
	 * 
	 * @return true if scaling up is achieved.
	 */
	public boolean testScaleUp() throws Exception {
		logger.log(Level.FINE,"Testing whether to scale up..");
		if (upIn > 0) {
			upIn--;
			logger.log(Level.FINE,"Too soon to scale up.");
			return false;
		}

		if (averageLoad < upLimit) {
			logger.log(Level.FINE,"Scaling up not needed.");
			return false;
		}

		//Update weights based on new values.
		updateWeights();

		//Adding the worker to the Joram infrastructure
		logger.log(Level.INFO,"Adding a new worker..");

		Queue worker;
		try {
			worker = js.addWorker();
		} catch (Exception e) {
			e.printStackTrace();
			logger.log(Level.SEVERE, "Couldn't create the new Joram server!");
			throw e;
		}

		//Adding it to our system view
		workers.add(worker);
		delivered.add(0);
		rates.add(0);
		loads.add(0);

		/* 
		 * Notify producers, not that initial weight = 
		 * max weights of the workers, which is done 
		 * by addRemoteDestination on producer's side.
		 */
		for(Queue producer : producers) {
			try {
				producer.addRemoteDestination(worker.getName());
			} catch (Exception e) {
				e.printStackTrace();
				logger.log(Level.SEVERE, "Couldn't send weights to a producer!");
				throw e;
			}
		}

		upIn = scaleIn;
		logger.log(Level.INFO,"Added new worker successfully.");

		//Skip values computed during worker's creation
		monitorWorkers();

		return true;
	}
	
	private void regulateRates() {
		logger.log(Level.FINE,"Regulating rates..");
		
		if (downIn > -1) {
			logger.log(Level.FINE,"On going scaling down plan, skip.");
			return;
		}
		
		String rateStr = "";
		for(int i = 0; i < workers.size(); i++) {			
			int dif = (loads.get(i) - averageLoad) / 2;
			int val = Math.max(rates.get(i) - dif,0);
			rates.set(i,val);
			rateStr = rateStr + "\t" + val;
		}
		
		logger.log(Level.INFO,"Regulated rates:" + rateStr);
	}

	public void updateWeights() throws Exception {
		logger.log(Level.FINE,"Updating weights..");
		
		regulateRates();

		//Compute weights.
		int[] weights = new int[workers.size()];
		int maxRate = Integer.MIN_VALUE;
		for (int i = 0; i < workers.size(); i++) {
			if (rates.get(i) > maxRate)
				maxRate = rates.get(i);
		}

		if (maxRate < 1) {
			maxRate = 1;
		}

		//We guarantee that max weight is 20.
		double factor = 10.0 / maxRate;

		String weightStr = "";
		for (int i = 0; i < workers.size(); i++) {
			weights[i] = (int) Math.round(rates.get(i) * factor);
			if (weights[i] == 0) {
				weights[i] = 1;
			}

			weightStr += "\t" + weights[i];
		}

		logger.log(Level.INFO,"Updated weights:" + weightStr);
		                       
		//Notify producers.
		for(int i = 0; i < producers.size(); i++) {
			try {
				producers.get(i).sendDestinationsWeights(weights);

			} catch (Exception e) {
				e.printStackTrace();
				logger.log(Level.SEVERE, "Couldn't send weights to a producer!");
				throw e;
			}
		}

		logger.log(Level.FINE,"Done updating weights.");
	}
}
