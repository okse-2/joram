package elasticity.eval;

class Constants {
	/** 
	 * Size of the produced messages (in bytes).
	 */
	public static final int MSG_SIZE = 1000;
	
	/**
	 * Maximum number a worker can consume per WORKER_PERIOD. 
	 */
	public static final int WORKER_MAX = 100;
	
	/**
	 * Period of time between two worker rounds (in ms).
	 */
	public static final int WORKER_PERIOD = 1000;
	
	/**
	 * Number of producers.
	 */
	public static final int NB_OF_PRODUCERS = 2;
	
	/**
	 * Period of time between two producer rounds (in ms).
	 */
	public static final int PRODUCER_PERIOD = 100;
}
