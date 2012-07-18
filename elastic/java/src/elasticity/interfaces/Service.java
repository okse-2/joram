package elasticity.interfaces;

import java.util.Properties;

/**
 * Defines what would a service layer be.
 * 
 * @author Ahmed El Rheddane
 */
public abstract class Service extends Loggable {
	/**
	 * Should be called before using the service.
	 * Starts by calling initLogger.
	 * 
	 * @param props the set of properties needed to initialize the service.
	 * @throws Exception
	 */
	public void init(Properties props) throws Exception {
		initLogger();
		initService(props);
	}
	
	/**
	 * Initializes the specific server.
	 * 
	 * @param props the set of properties needed to initialize the service.
	 * @throws Exception
	 */
	protected abstract void initService(Properties props) throws Exception;
}
