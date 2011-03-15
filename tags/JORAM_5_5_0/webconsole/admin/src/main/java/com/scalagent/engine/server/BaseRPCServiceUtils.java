package com.scalagent.engine.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

import com.scalagent.engine.shared.BaseWTO;

public class BaseRPCServiceUtils {
  
    private static HashMap<Locale, ResourceBundle> bundles = new HashMap<Locale, ResourceBundle>();
    private static String bundleBasename;
    private static Locale locale;

	/**
	 * Returns all entities stored in the hashmap given in params with their dbStatus set to NEW.
	 * Useful to handle refresh of pages with retrieveAll param.
	 * 
	 * @param <W> Entities class
	 * @param entities List of entities to return
	 * @return List of entities to return to the client 
	 * 
	 */
	public static <W extends BaseWTO> List<W> retrieveAll(HashMap<String, W> entities) {

		List<W> toReturn = new ArrayList<W>();
		Set<String> keys = entities.keySet();
		Iterator<String> iterator = keys.iterator();
		while (iterator.hasNext()) {
			String key = iterator.next();
			W entity = entities.get(key);
			entity.setDbChangeStatus(BaseWTO.NEW);
			toReturn.add(entity);
		}
		return toReturn;

	}

	/**
	 * Compares the list of entities retrieved from the server
	 * to the one stored in the session, in this class...
	 * Not that if the application is multiuser, already sent entities must be stored in the session
	 * because this list may be (temporarily) different for each user.
	 */	
	public static <W extends BaseWTO> List<W> compareEntities(W[] newEntities, HashMap<String, W> entities) {

		// new or updated entities
		// =======================

		List<W> toReturn = new ArrayList<W>();
		for (int i=0;i<newEntities.length;i++) {
			
			W newEntity = newEntities[i];
			

			// is already in the list?
			if (entities.containsKey(newEntity.getId())) {

				BaseWTO entity = entities.get(newEntity.getId());
				if (!entity.equalsContent(newEntity)) {
					// updated device
					newEntity.setDbChangeStatus(BaseWTO.UPDATED);
					
					toReturn.add(newEntity);
					// all entities stored in cache have a dbstatus set to NEW
					entities.put(newEntity.getId(), newEntity);
				}

			} else {
				newEntity.setDbChangeStatus(BaseWTO.NEW);
				entities.put(newEntity.getId(), newEntity);
				toReturn.add(newEntity);
			}

		}

		// Deleted entities
		// ================

		Set<String> keys = entities.keySet();
		Iterator<String> iterator = keys.iterator();
		ArrayList<String> idsToRemove = new ArrayList<String>();
		while (iterator.hasNext()) {

			String id = iterator.next();
			boolean found = false;
			int i = 0;
			while ((i<newEntities.length) && (!found)) {
				if (newEntities[i].getId().equals(id)) {
					found = true;
				}
				i++;
			}

			if (!found) {
				idsToRemove.add(id);
			}

		}

		for (int j=0;j<idsToRemove.size();j++) {
			W entity = entities.remove(idsToRemove.get(j));
			entity.setDbChangeStatus(BaseWTO.DELETED);
			toReturn.add(entity);
		}
		return toReturn;
	}
	
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
        BaseRPCServiceUtils.bundleBasename = bundleBasename;
    }

    /**
     * Sets the locale used in the application.
     * This method is automatically called by the method execute of BaseRPCServiceImpl or BaseRPCServiceFakeImpl.
     * 
     * @param locale locale to use
     */
    protected static void setLocale(Locale locale) {
        BaseRPCServiceUtils.locale = locale;
    }
	
}