package elasticity.old;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.naming.*;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminModule;

/**
 * A class to add elasticity to the Alias Queue behavior.
 * 
 * @author Ahmed El Rheddane
 *
 */
public class ElasticityLoop {

	/** Logger object. */
	private final static Logger logger = Logger.getLogger(ElasticityLoop.class.getName());

	/** Log file. */
	private final static String loggerFile = ElasticityLoop.class.getName() + ".log"; 

	/** Limit over which we add new workers. */
	public static final int maxCapThreshold = 500;

	/** Limit under which we remove unnecessary workers. */ 
	public static final int minCapThreshold = 50;

	/** Period of our elasticity loop in milliseconds. */
	public static final Integer loopPeriod = 5000;

	/** Rate at which reception rates are decreased (a percentage) */
	private static int downRate = 20;

	/** The list of producers */
	private static List<Queue> producers = null;

	/** The list of workers connected to producers, cannot be empty. */
	private static List<Queue> activeWorkers = null;

	/** The list of available workers not connected to producers. */
	private static List<Queue> idleWorkers = null;

	/** Number of delivered messages per active worker, since their creation. */
	private static List<Integer> delivered;

	/** Number of delivered messages per active worker, during the last period. */
	private static List<Integer> rates;


	/** Number of pending messages per worker. */
	private static List<Integer> loads;

	/** Number of monitored underloaded workers. */
	private static int underloaded;

	/** Number of monitored overloaded workers. */
	private static int overloaded;

	/** Index of the candidate to remove in an on-going scaling down process. */
	private static int toRemove;

	/** Reception rate of the candidate to remove when elected. */
	private static int toRemoveRate;

	/** Number of iterations since toRemove has been elected. */
	private static int toRemoveAge;

	/** Minimum value of current rates. */
	private static int minRate;

	/** Maximum value of current rates. */
	private static int maxRate;

	/** Loop continues while stop is false. */
	private static boolean stopLoop = false;



	/** initializes the logger. */
	public static void initLogger() {
		try {
			FileHandler fh = new FileHandler(loggerFile, false);
			logger.addHandler(fh);
			logger.setLevel(Level.ALL);
			SimpleFormatter formatter = new SimpleFormatter();
			fh.setFormatter(formatter);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Logger couldn't be initialized.");
		}
	}

	/** Sets up the producers and workers. */
	public static void initLoop() throws Exception {
		producers = new ArrayList<Queue>();
		activeWorkers = new ArrayList<Queue>();
		idleWorkers = new ArrayList<Queue>();
		delivered = new ArrayList<Integer>();
		rates = new ArrayList<Integer>();
		loads = new ArrayList<Integer>();

		toRemove = -1;

		InitialContext ictx = new InitialContext();
		producers.add((Queue) ictx.lookup("alias"));
		activeWorkers.add((Queue) ictx.lookup("remote1"));
		delivered.add(0);
		rates.add(0);
		loads.add(0);
		for(int i = 2; i <= 4; i++)
			idleWorkers.add((Queue) ictx.lookup("remote" + i));
		ictx.close();

	}

	/** 
	 * Updates the values of monitoring variables.
	 */
	private static void monitorWorkers() {
		underloaded = 0;
		overloaded = 0;
		minRate = Integer.MAX_VALUE;
		maxRate = Integer.MIN_VALUE;

		try {
			for(int i = 0; i < activeWorkers.size(); i++) {
				Queue worker = activeWorkers.get(i);
				loads.set(i, worker.getPendingMessages());
				if (loads.get(i) < minCapThreshold)
					underloaded++;
				else if (loads.get(i) > maxCapThreshold)
					overloaded++;

				int newDelivered = worker.getDeliveredMessages();
				rates.set(i, newDelivered - delivered.get(i));
				delivered.set(i,newDelivered);
				if (rates.get(i) < minRate)
					minRate = rates.get(i);
				if (rates.get(i) > maxRate)
					maxRate = rates.get(i);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.log(Level.SEVERE, "Couldn't monitor the workers.");
		}

		String loadStr = "";
		String rateStr = "";
		for(int i = 0; i < activeWorkers.size(); i++) {
			loadStr = loadStr + " " + loads.get(i);
			rateStr = rateStr + " " + rates.get(i);
		}

		logger.log(Level.FINE,"Monitored loads:" + loadStr);
		logger.log(Level.FINE,"Monitored rates:" + rateStr);
	}

	/**
	 * @return the index of a worker from idleWorkers to be added to activeWorkers. 
	 */
	private static int electWorkerToAdd() {
		return 0;
	}

	/**
	 * @return the index of a worker from activeWorkers to be potentially removed.
	 */
	private static int electWorkerToRemove() {
		return activeWorkers.size()-1;
	}

	/**
	 * Starts, continues or aborts scaling down process based 
	 * on the monitored values.
	 * @return true if scaling down is on-going.
	 */
	private static boolean testScaleDown() {
		//Plan should be cancelled.
		if (overloaded > 0) {
			if (toRemove >= 0) {
				rates.set(toRemove, toRemoveRate);
				toRemove = -1;
				logger.log(Level.INFO,"Cancelled scaling down plan.");
			}
			return false;
		}

		//Should initiate plan.
		if (activeWorkers.size() > 1 && toRemove < 0 && underloaded == activeWorkers.size()) {
			toRemove = electWorkerToRemove();
			toRemoveAge = 0;
			toRemoveRate = rates.get(toRemove);
			logger.log(Level.INFO,"Elected worker to remove: " + toRemove);
		}

		//Plan can continue.
		if (toRemove >= 0) {
			if (++toRemoveAge > (100/downRate)) {
				Queue worker = activeWorkers.remove(toRemove);
				idleWorkers.add(0,worker);
				rates.remove(toRemove);
				loads.remove(toRemove);
				delivered.remove(toRemove);

				//Notify producers.
				for(int i = 0; i < producers.size(); i++)
					try {
						producers.get(i).delRemoteDestination(worker.getName());
					} catch (Exception e) {
						e.printStackTrace();
						logger.log(Level.SEVERE, "Couldn't delete a remote destination from producers.");
					}
				toRemove = -1;
				logger.log(Level.INFO, "Removed extra worker successfully.");

			} else {
				rates.set(toRemove, toRemoveRate*(100-downRate*toRemoveAge)/100);
				logger.log(Level.INFO,"Trying to remove extra worker, " + toRemoveAge);
			}
			return true;	
		}

		return false;
	}

	/**
	 * Adds a new worker to the active workers list, if needed.
	 * @return true if scaling up is achieved.
	 */
	private static boolean testScaleUp() {
		if (overloaded < activeWorkers.size() || idleWorkers.size() == 0)
			return false;

		
		int toAdd = electWorkerToAdd();
		Queue worker = idleWorkers.remove(toAdd);
		activeWorkers.add(worker);
		rates.add(maxRate);
		loads.add(0);
		try {
			delivered.add(worker.getDeliveredMessages());
		} catch (Exception e) {
			e.printStackTrace();
			logger.log(Level.SEVERE, "Couldn't get new worker's delivered messages.");
		}

		//Notify producers.
		for(int i = 0; i < producers.size(); i++)
			try {
				producers.get(i).addRemoteDestination(worker.getName());
			} catch (Exception e) {
				e.printStackTrace();
				logger.log(Level.SEVERE, "Couldn't send weights to a producer.");
			}

		logger.log(Level.INFO,"Added new worker successfully.");
		return true;
	}

	/**
	 * Decreases the monitored rates of overloaded workers.
	 */
	private static void regulateRates() {
		if (overloaded == 0)
			return;
		
		String rateStr = ""; 
		for(int i = 0; i < activeWorkers.size(); i++) {
			if (loads.get(i) > maxCapThreshold) {
				rates.set(i,rates.get(i)*(100-downRate)/100);
				if (rates.get(i) < minRate)
					minRate = rates.get(i);
			}
			rateStr = rateStr + " " + rates.get(i);
		}
		logger.log(Level.FINE,"Regulated rates:" + rateStr);
	}

	/**
	 * Computes weights and sends them to producers.
	 */
	private static void sendWeights() {
		int[] weights = new int[activeWorkers.size()];

		if (minRate <= 0)
			minRate = 1;

		int base  = (int)Math.pow(10.0,Math.floor(Math.log10(minRate)));

		String weightStr = "";
		for (int i = 0; i < activeWorkers.size(); i++) { 	
			int weight = (int)Math.round((double)rates.get(i)/(double)base);

			if (weight <= 0)
				weight = 1;

			weights[i] = weight;
			weightStr = weightStr + " " + weight;
		}

		logger.log(Level.FINE,"Computed weights:" + weightStr);

		//Notify producers.
		try {
			for(int i = 0; i < producers.size(); i++)

				producers.get(i).sendDestinationsWeights(weights);
		} catch (Exception e) {
			e.printStackTrace();
			logger.log(Level.SEVERE, "Couldn't send weights to a producer.");
		}
	}

	public static void main(String args[]) throws Exception {
		initLogger();
		initLoop();

		//Elasticity loop
		long correction = 0l;
		do {
			Thread.sleep(loopPeriod - correction);
			if (stopLoop)
				break;

			long startTime = System.currentTimeMillis();
			AdminModule.connect("root", "root", 60);

			monitorWorkers();

			if(!testScaleDown())
				if(!testScaleUp())
					regulateRates();

			sendWeights();

			AdminModule.disconnect();
			correction = System.currentTimeMillis() - startTime;
			logger.log(Level.FINE,"Iteration took: " + correction);
		} while (!stopLoop);
	}
}
