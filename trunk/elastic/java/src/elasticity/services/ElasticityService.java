package elasticity.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

import javax.jms.ConnectionFactory;
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

	/** A link to the Joram service. */
	private JoramService js;

	//Properties.
	/** The value beyond which a worker is considered overloaded. */ 
	private int maxLoadLimit;

	/** The value beneath which a worker is considered underloaded. */
	private int minLoadLimit;

	/** 
	 * Rate by which consumption rates are decreased (a percentage).
	 * It is used (1) when regulating overloaded queues' rates;
	 * and (2), when scaling down, which takes 100/downRate rounds. 
	 */
	private int downRate;

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

	/** Number of monitored underloaded workers. */
	private int underloaded;

	/** Number of monitored overloaded workers. */
	private int overloaded;

	/**
	 * Stores for how many rounds a scaling down plan has been on-going.
	 * If < 0, no scaling down plan is being carried on.
	 */
	private int scalingDownAge;

	/**
	 * Stores the rate of the worker to remove, just before initiating
	 * the scaling down plan.
	 */
	private int scalingDownRate;

	@Override
	public void initService(Properties props) throws Exception {
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
		maxLoadLimit = Integer.valueOf(props.getProperty("max_load_limit"));
		minLoadLimit = Integer.valueOf(props.getProperty("min_load_limit"));
		downRate = Integer.valueOf(props.getProperty("down_rate"));

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

		scalingDownAge = -1;

		//Setting the admin connection once and for all.
		AdminModule.connect("10.0.0.2",16101,"root","root", 60);
		
		logger.log(Level.INFO,"Initialization completed.");
	}

	/** 
	 * Updates the values of monitoring variables.
	 */
	public void monitorWorkers() throws Exception {
		underloaded = 0;
		overloaded = 0;

		try {
			for(int i = 0; i < workers.size(); i++) {
				Queue worker = workers.get(i);

				int newDelivered = worker.getDeliveredMessages();
				rates.set(i, newDelivered - delivered.get(i));
				delivered.set(i,newDelivered);

				loads.set(i, worker.getPendingMessages());
				if (loads.get(i) < minLoadLimit)
					underloaded++;
				else if (loads.get(i) > maxLoadLimit)
					overloaded++;
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.log(Level.SEVERE, "Couldn't monitor the workers!");
			throw e;
		}

		String loadStr = "";
		String rateStr = "";
		for(int i = 0; i < workers.size(); i++) {
			loadStr = loadStr + " " + loads.get(i);
			rateStr = rateStr + " " + rates.get(i);
		}

		logger.log(Level.FINE,"Monitored loads:" + loadStr);
		logger.log(Level.FINE,"Monitored rates:" + rateStr);
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
		int last = workers.size()-1;

		//Plan should be cancelled.
		if (overloaded > 0) {
			if (scalingDownAge >= 0) {
				rates.set(last, scalingDownRate);
				logger.log(Level.INFO,"Cancelled scaling down plan.");
			}
			return false;
		}

		//Should initiate plan.
		if (workers.size() > 1 && scalingDownAge < 0 && underloaded == workers.size()) {
			scalingDownRate = rates.get(last);
			scalingDownAge = 0;
		}

		//Plan can continue.
		if (scalingDownAge >= 0) {
			if (++scalingDownAge > (100/downRate)) {
				//Worker should effectively be removed.
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

				scalingDownAge = -1;

				logger.log(Level.INFO, "Removed extra worker successfully.");
				return false;

			} else {
				//Gradually decrease the rate of the worker to remove (will affect weights computation).
				rates.set(last, scalingDownRate*(100-downRate*scalingDownAge)/100);
				logger.log(Level.INFO,"Trying to remove extra worker, " + scalingDownAge + "..");
				return true;
			}
		}

		//No scaling down plan started or continued.
		return false;
	}

	/**
	 * Adds a new worker to the active workers list,
	 * if all workers are overloaded
	 * 
	 * @return true if scaling up is achieved.
	 */
	public boolean testScaleUp() throws Exception {
		if (overloaded < workers.size())
			return false;

		//Update weights based on new values.
		updateWeights();

		//Adding the worker to the Joram infrastructure
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
		for(Queue producer : producers)
			try {
				producer.addRemoteDestination(worker.getName());
			} catch (Exception e) {
				e.printStackTrace();
				logger.log(Level.SEVERE, "Couldn't send weights to a producer!");
				throw e;
			}

		//Skip values computed during worker's creation
		monitorWorkers();

		logger.log(Level.INFO,"Added new worker successfully.");
		return true;
	}

	/**
	 * Decreases the monitored rates of overloaded workers.
	 */
	private void regulateRates() {
		//Return, if no need for regulation.
		if (overloaded == 0 || overloaded == workers.size())
			return;

		String rateStr = "";
		for(int i = 0; i < workers.size(); i++) {
			if (loads.get(i) > maxLoadLimit)
				rates.set(i,rates.get(i)*(100-downRate)/100);

			rateStr = rateStr + " " + rates.get(i);
		}
		logger.log(Level.FINE,"Regulated rates:" + rateStr);
	}

	/**
	 * Computes weights and sends them to producers.
	 * Calls regulateRates.
	 */
	public void updateWeights() throws Exception {

		//If necessary..
		regulateRates();

		//Compute weights.
		int[] weights = new int[workers.size()];
		int maxRate = Integer.MIN_VALUE;
		for(int i = 0; i < workers.size(); i++)
			if (rates.get(i) > maxRate)
				maxRate = rates.get(i);

		if (maxRate < 1)
			maxRate = 1;

		int base  = (int)Math.pow(10.0,Math.floor(Math.log10(maxRate))-1);

		String weightStr = "";
		for (int i = 0; i < workers.size(); i++) { 	
			int weight = (int)Math.round((double)rates.get(i)/(double)base);
			int wb = (int)Math.pow(10.0,Math.floor(Math.log10(weight)));

			if (weight < 1) //Weight should NEVER be negative
				weight = 1;

			weights[i] = weight;
			weightStr = weightStr + " " + weight;
		}
		logger.log(Level.FINE,"Computed weights:" + weightStr);

		//Notify producers.
		for(int i = 0; i < producers.size(); i++)
			try {
				producers.get(i).sendDestinationsWeights(weights);

			} catch (Exception e) {
				e.printStackTrace();
				logger.log(Level.SEVERE, "Couldn't send weights to a producer!");
				throw e;
			}
	}
}
