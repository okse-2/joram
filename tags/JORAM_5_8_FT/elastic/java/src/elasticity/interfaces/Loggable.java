package elasticity.interfaces;

import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public abstract class Loggable {
	protected Logger logger;

	/**
	 * Initializes the logger Object.
	 * 
	 * @throws Exception
	 */
	protected void initLogger() throws Exception {
		//Initializes the logger.
		String currentClassName = this.getClass().getName();
		FileHandler fh = new FileHandler(currentClassName+".log", false);
		SimpleFormatter formatter = new SimpleFormatter();
		fh.setFormatter(formatter);
		logger = Logger.getLogger(currentClassName);
		logger.addHandler(fh);
		logger.setLevel(Level.ALL);

	}
}
