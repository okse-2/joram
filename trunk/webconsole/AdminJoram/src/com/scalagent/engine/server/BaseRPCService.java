package com.scalagent.engine.server;

import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.scalagent.engine.client.command.Action;
import com.scalagent.engine.client.command.Response;


public abstract class BaseRPCService extends RemoteServiceServlet {

	private static final long serialVersionUID = 8980983186019611263L;
	private static HashMap<Locale, ResourceBundle> bundles = new HashMap<Locale, ResourceBundle>();
	private static String bundleBasename;
	private static Locale locale;

	/**
	 * This hashmap stores information on opened sessions.
	 * The key is the session id, the value is the timestamp of the last
	 * command received via this session.
	 * A timer (BaseRPCServiceTimer) checks periodically if the client
	 * still sends requests:
	 *    - if yes, nothing is done
	 *    - if no, the method onClientDisconnected(String sessionId, Long lastTimepstamp) is called. 
	 */
	private HashMap<String, Long> sessions = new HashMap<String, Long>();
	
	/**
	 * Return the localized string corresponding to the key given in param.
	 * If the key is not available in the bundle, returns the key itself.
	 * 
	 * @param key key to retrieve in the bundle
	 * @return the localized string or the key itself if it doesn't exist in the bundle.
	 */
	public static String getString(String key) {

		try {
			String value = getBundle().getString(key);
			return value; 
		} catch (Exception e) {
			return key;
		}

	}

	/**
	 * Retrieve the bundle corresponding to the current locale.
	 * If the bundle is not available, try to load it.
	 * If the bundle is not available, return null.
	 * 
	 * @return the correct bundle.
	 */
	private static ResourceBundle getBundle() {
		if (locale == null) {
			return null;
		}

		ResourceBundle bundle = bundles.get(locale);
		if (bundle == null) {

			try {
				bundle = ResourceBundle.getBundle(bundleBasename, locale);
				// store it
				bundles.put(locale, bundle);
				return bundle;
			} catch (Exception e) {
				return null;
			}
		} else {
			return bundle; 
		}

	}

	/**
	 * Sets the ResourceBundle base name.
	 * It has to be called in the initialization of the service.
	 * 
	 * @param bundleBasename bundle basename
	 */
	protected static void setBundleBasename(String bundleBasename) {
		BaseRPCService.bundleBasename = bundleBasename;
	}

	/**
	 * Sets the locale used in the application.
	 * This method is automatically called by the method execute of BaseRPCServiceImpl or BaseRPCServiceFakeImpl.
	 * 
	 * @param locale locale to use
	 */
	protected static void setLocale(Locale locale) {
		BaseRPCService.locale = locale;
	}


	/**
	 * This method stores session information in the sessions hashmap.
	 *
	 * @param sessionId session id to store
	 * @param timestamp creation timestamp
	 */
	public synchronized void setSession(String sessionId, long timestamp) {
		
		sessions.put(sessionId, new Long(timestamp));
		
	}
	
	/**
	 * This method updates session information stored in the sessions hashmap.
	 * It's called for each new request received from a client which has already sent an SetSessionAction,
	 * and thus, created an entry in sessions hashmap.
	 * If not, nothing is done. 
	 * 
	 * @param sessionId session to update
	 * @param timestamp new timestamp
	 */
	public synchronized void updateSessionInformation(String sessionId, long timestamp) {
		
		if (sessions.containsKey(sessionId)) {
			sessions.put(sessionId, new Long(timestamp));
		}
	}
	
	/**
	 * This method removes session from the sessions hashmap.
	 *
	 * @param sessionId session id to remove
	 */
	public synchronized void removeSession(String sessionId) {
		
		sessions.remove(sessionId);
	}
	
	@SuppressWarnings("unused") 
	protected void onInvalidSession(String sessionId, Long timestamp) {
	}
	
	@SuppressWarnings("unchecked")
	protected HashMap<String, Long> getSessionsInformation() {
		return (HashMap<String, Long>) sessions.clone();

	}
	
	public abstract <R extends Response> R execute(Action<R> action);
	
}
