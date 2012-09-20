/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2011 - 2012 ScalAgent Distributed Technologies
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA.
 *
 * Initial developer(s): ScalAgent Distributed Technologies
 * Contributor(s): 
 */
package org.objectweb.joram.mom.dest.jms;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.common.Debug;
import fr.dyade.aaa.util.management.MXWrapper;

public class JMSConnectionService {

  private static final Logger logger = Debug.getLogger(JMSConnectionService.class.getName());

  private static JMSConnections singleton;

  public synchronized static JMSConnections getInstance() {
    if (singleton == null) {
      singleton = new JMSConnections();
      try {
        MXWrapper.registerMBean(singleton, "JMS#" + AgentServer.getServerId(), "type=Connections");
      } catch (Exception e) {
        logger.log(BasicLevel.DEBUG, "registerMBean", e);
      }
    }
    return singleton;
  }

  /**
   * @param urls The url list separate by space
   * urls = "jndi_url/?name=cnx1&cf=cfName&jndiFactoryClass=com.xxx.yyy&user=user1&pass=pass1&clientID=clientID jndi_url/?name=cnx2&cf=cfName2&jndiFactoryClass=com.xxx.zzz&user=user1&pass=pass2&clientID=clientID2"
   * in url, use %26 instead of & if needed.
   */
  public static void addServer(String urls) {
  	String name = null;
  	String cf = null;
    String jndiFactoryClass = null;
    String jndiUrl = null;
    String user = null;
    String password = null;
    String clientID = null;
    URL url = null;
    if (urls != null && urls.length() > 0) {
    	StringTokenizer st = new StringTokenizer(urls);
    	while (st.hasMoreTokens()) {
    		try {
    			url = new URL(null, st.nextToken(), new MyURLStreamHandler());
    		} catch (Exception e) {
    			if (logger.isLoggable(BasicLevel.ERROR)) {
      			logger.log(BasicLevel.ERROR, "JMSConnectionService.addServer : Exception ", e);
      		}
  				continue;
  			}
    		String query = url.getQuery();
    		String[] tab = query.split("&|%26");
    		Properties prop = new Properties();
    		for (int i = 0; i < tab.length; i++) {
    			String[] split = tab[i].split("=");
    			prop.put(split[0],split[1]);
    		}
    		
    		name = prop.getProperty("name");
    		cf = prop.getProperty("cf");
    		jndiFactoryClass = prop.getProperty("jndiFactoryClass");
    		user =  prop.getProperty("user");
    		password =  prop.getProperty("pass");
    		clientID =  prop.getProperty("clientID");
    		jndiUrl = url.toString().substring(0, url.toString().indexOf('?'));
    		
    		getInstance().addServer(name, cf, jndiFactoryClass, jndiUrl, user, password, clientID);
    	}
    }
  }

  public static void addServer(String name, String cnxFactoryName, String jndiFactoryClass, String jndiUrl) {
    getInstance().addServer(name, cnxFactoryName, jndiFactoryClass, jndiUrl);
  }

  public static void addServer(String name, String cnxFactoryName, String jndiFactoryClass, String jndiUrl, String user,
      String password) {
    getInstance().addServer(name, cnxFactoryName, jndiFactoryClass, jndiUrl, user, password);
  }

  public static void addServer(String name, String cnxFactoryName, String jndiFactoryClass, String jndiUrl, String user,
      String password, String clientID) {
    getInstance().addServer(name, cnxFactoryName, jndiFactoryClass, jndiUrl, user, password, clientID);
  }

  /**
   * Removes the live connection to the specified JMS server.
   * 
   * @param name the name identifying the server or list of name separate by space
   */
  public static void deleteServer(String names) {
  	StringTokenizer token = new StringTokenizer(names, " ");
  	while (token.hasMoreTokens()) {
  		getInstance().deleteServer(token.nextToken());
  	}
  }

  /**
   * Gets the list of known servers.
   */
  public static String[] getServerNames() {
    return getInstance().getServerNames();
  }

  /**
   * Initializes the service. Starts a connection with servers if urls set.
   */
  public static void init(String args, boolean firstTime) throws Exception {
  	if (firstTime) {
  		if (args != null && args.length() > 0) {
  			addServer(args);
  		}
  	} else {
  		getInstance().readSavedConf();
  	}
  }

  /**
   * Stops all connections to JMSBridge servers.
   */
  public static void stopService() {
    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "Stopping JMSConnectionService service.");
    }
    getInstance().stop();
  }

  public static List<String> convertToList(final String value) {
    String[] values = value.split(",");
    List<String> injection = null;

    // We should have at least 1 real value
    if (!((values.length == 1) && ("".equals(values[0])))) {
      injection = new ArrayList<String>();
      for (int i = 0; i < values.length; i++) {
        String part = values[i];
        injection.add(part.trim());
      }
    }
    return injection;
  }

  static class MyURLStreamHandler extends URLStreamHandler {
		@Override
		protected URLConnection openConnection(URL u) throws IOException {
			throw new IOException("sorry, not possible to openConnection with this URL (juste use to parse) " + u);
		}
	}
}
