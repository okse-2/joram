package elasticity.eval;

class Constants {
	/** 
	 * Size of the produced messages (in bytes).
	 */
	public static final int MSG_SIZE = 1000;
	
	/**
	 * Maximum number a worker can consume per TIME_UNIT. 
	 */
	public static final int WORKER_MAX = 500;
	
	/**
	 * period of time between two rounds (in ms).
	 */
	public static final int TIME_UNIT = 1000;
	
	/**
	 * Number of producers.
	 */
	public static final int NB_OF_PRODUCERS = 2;
}
