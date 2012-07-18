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
public class ElasticityLoopV2 {
	
	/** Logger object. */
	private final static Logger logger = Logger.getLogger(ElasticityLoop.class.getName());

	/** Log file. */
	private final static String loggerFile = ElasticityLoop.class.getName() + ".log"; 

	/** Limit over which we add new workers. */
	public static final int maxCapThreshold = 2000;

	/** Limit under which we remove unnecessary workers. */ 
	public static final int minCapThreshold = 500;

	/** Period of our elasticity loop in milliseconds. */
	public static final Integer loopPeriod = 10000;

	/** Rate at which reception rates are decreased (a percentage) */
	private static int downRate = 20;

	/** The list of producers */
	private static List<Queue> producers = null;

	/** The list of workers connected to producers, cannot be empty. */
	private static List<Queue> workers = null;

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

	/** Loop continues while stop is false. */
	private static boolean stopLoop = false;
	
	private static List<Integer> lastMaxRates;
	private static List<Boolean> prevOverloaded;
	
	

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
		workers = new ArrayList<Queue>();
		delivered = new ArrayList<Integer>();
		lastMaxRates = new ArrayList<Integer>();
		prevOverloaded = new ArrayList<Boolean>();
		rates = new ArrayList<Integer>();
		loads = new ArrayList<Integer>();

		toRemove = -1;

		InitialContext ictx = new InitialContext();
		producers.add((Queue) ictx.lookup("alias0"));
		producers.add((Queue) ictx.lookup("alias1"));
		workers.add((Queue) ictx.lookup("remote2"));
		ictx.close();
		
		delivered.add(0);
		rates.add(0);
		loads.add(0);
		
		lastMaxRates.add(-1);
		prevOverloaded.add(false);
		
		JoramManager.init(0,logger);
	}

	/** 
	 * Updates the values of monitoring variables.
	 */
	private static void monitorWorkers() {
		underloaded = 0;
		overloaded = 0;

		try {
			for(int i = 0; i < workers.size(); i++) {
				Queue worker = workers.get(i);
				int newDelivered = worker.getDeliveredMessages();
				rates.set(i, newDelivered - delivered.get(i));
				delivered.set(i,newDelivered);
				
				loads.set(i, worker.getPendingMessages());
				if (loads.get(i) < minCapThreshold) {
					underloaded++;
				} else if (loads.get(i) > maxCapThreshold) {
					overloaded++;
					if (!prevOverloaded.get(i)) {
						lastMaxRates.set(i,rates.get(i));
						prevOverloaded.set(i, true);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.log(Level.SEVERE, "Couldn't monitor the workers.");
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
	 * @return the index of a worker from activeWorkers to be potentially removed.
	 */
	private static int electWorkerToRemove() {
		return workers.size()-1;
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
		if (workers.size() > 1 && toRemove < 0 && underloaded == workers.size()) {
			toRemove = electWorkerToRemove();
			toRemoveAge = 0;
			toRemoveRate = rates.get(toRemove);
			logger.log(Level.INFO,"Elected worker to remove: " + toRemove);
		}

		//Plan can continue.
		if (toRemove >= 0) {
			if (++toRemoveAge > (100/downRate)) {
				logger.log(Level.INFO,"Removing worker:" + toRemove);
				Queue worker = workers.remove(toRemove);
				rates.remove(toRemove);
				loads.remove(toRemove);
				delivered.remove(toRemove);
				
				lastMaxRates.remove(toRemove);
				prevOverloaded.remove(toRemove);

				//Notify producers.
				for(int i = 0; i < producers.size(); i++)
					try {
						producers.get(i).delRemoteDestination(worker.getName());
					} catch (Exception e) {
						e.printStackTrace();
						logger.log(Level.SEVERE, "Couldn't delete the remote destination from producers.");
					}
				toRemove = -1;
				try {
					JoramManager.delWorker();
				} catch (Exception e) {
					e.printStackTrace();
					logger.log(Level.SEVERE, "Problem while trying to remove the corresponding Joram Server.");
				}
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
	private static boolean testScaleUp() throws Exception {
		if (overloaded < workers.size())
			return false;

		sendWeights(true);
		
		logger.log(Level.INFO,"Started adding a new worker...");
		Queue worker = JoramManager.addWorker();
		logger.log(Level.INFO,"JoramManager is done..");
		workers.add(worker);
		delivered.add(0);
		rates.add(0);
		loads.add(0);
		
		lastMaxRates.add(-1);
		prevOverloaded.add(false);
		
		for(Queue producer : producers)
			try {
				producer.addRemoteDestination(worker.getName());
				logger.log(Level.INFO,"Sent to producer " + producer.getName());
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
		for(int i = 0; i < workers.size(); i++) {
			if (loads.get(i) > maxCapThreshold)
				rates.set(i,rates.get(i)*(100-downRate)/100);
				
			rateStr = rateStr + " " + rates.get(i);
		}
		logger.log(Level.FINE,"Regulated rates:" + rateStr);
	}

	/**
	 * Computes weights and sends them to producers.
	 */
	private static void sendWeights(boolean scalingUp) {
		int[] weights = new int[workers.size()];
		
		if (scalingUp) {
			for(int i = 0; i < workers.size(); i++) {
				rates.set(i, lastMaxRates.get(i));
			}
		}

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

		AdminModule.connect("root", "root", 60);
		//Elasticity loop
		do {
			Thread.sleep(loopPeriod);
			if (stopLoop)
				break;

			monitorWorkers();
			
			if(testScaleDown()) {
				sendWeights(false);
				continue;
			}
			
			if(testScaleUp()) {
				//Skip non-relevant values.
				monitorWorkers();
				continue;
			}
			
			regulateRates();
			sendWeights(false);

		} while (!stopLoop);
		AdminModule.disconnect();
	}
}
