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
package org.objectweb.joram.mom.dest.amqp;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import com.rabbitmq.client.ConnectionFactory;

import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.common.Debug;
import fr.dyade.aaa.util.management.MXWrapper;

public class AmqpConnectionService {

  private static final Logger logger = Debug.getLogger(AmqpConnectionService.class.getName());

  private static AmqpConnections singleton;

  public synchronized static AmqpConnections getInstance() {
    if (singleton == null) {
      singleton = new AmqpConnections();
      try {
        MXWrapper.registerMBean(singleton, "AMQP#" + AgentServer.getServerId(), "type=Connections");
      } catch (Exception e) {
        logger.log(BasicLevel.DEBUG, "registerMBean", e);
      }
    }
    return singleton;
  }

  /**
   * Adds an AMQP server and starts a live connection with it, accessible via
   * the host and port provided. A server is uniquely identified by the given
   * name. Adding an existing server won't do anything.
   * 
   * @param name the name identifying the server
   * @param host host of the added server
   * @param port port of the added server
   */
  public static void addServer(String name, String host, int port) {
    getInstance().addServer(name, host, port);
  }

  /**
   * Adds an AMQP server and starts a live connection with it, accessible via
   * the host and port provided. A server is uniquely identified by the given
   * name. Adding an existing server won't do anything.
   * 
   * @param name the name identifying the server
   * @param host host of the added server
   * @param port port of the added server
   * @param user user name
   * @param pass user password
   */
  public static void addServer(String name, String host, int port, String user, String pass) {
    getInstance().addServer(name, host, port, user, pass);
  }
  
  /**
   * Adds an AMQP server and starts a live connection with it, accessible via
   * the host and port provided. A server is uniquely identified by the given
   * name. Adding an existing server won't do anything.
   * 
   * @param urls the amqp url list identifying the servers separate by space.
   * ex: amqp://user:pass@localhost:5672/?name=serv1 amqp://user:pass@localhost:5678/?name=serv2
   * serv1 and serv2 are the name identifying the server.
   */
  public static void addServer(String urls) {
  	if (logger.isLoggable(BasicLevel.DEBUG)) {
			logger.log(BasicLevel.DEBUG, "AmqpConnectionService.addServer(" + urls + ')');
		}
  	
  	StringTokenizer tk = new StringTokenizer(urls);
  	while (tk.hasMoreTokens()) {
  		URL url = null;
  		try {
  			url = new URL(null, tk.nextToken(), new MyURLStreamHandler());
  		} catch (Exception e) {
  			if (logger.isLoggable(BasicLevel.ERROR)) {
    			logger.log(BasicLevel.ERROR, "AmqpConnectionService.addServer : Exception ", e);
    		}
				continue;
			}
  		String host = null;
      int port = -1;
      String name = "default";
      String userName = null;
    	String userPass = null;
    	
  		String userInfo = url.getUserInfo();
  		if (userInfo != null) {
  			StringTokenizer token = new StringTokenizer(userInfo, ":");
  			if (token.hasMoreTokens())
  				userName = token.nextToken();
  			if (token.hasMoreTokens())
  				userPass = token.nextToken();
  		}
  		
  		host = url.getHost();
  		if (host == null)
  			host = ConnectionFactory.DEFAULT_HOST;
  		
  		port = url.getPort();
  		if (port < 0)
  			port = ConnectionFactory.DEFAULT_AMQP_PORT;

  		String query = url.getQuery();
  		if (query != null) {
  			int index = query.indexOf('=');
  			if (index > 0)
  				name = query.substring(index+1, query.length());
  		}

  		if (logger.isLoggable(BasicLevel.DEBUG)) {
  			logger.log(BasicLevel.DEBUG, "AmqpConnectionService.addServer(" + name + ", " + host + ", " + port + ", " + userName + ')');
  		}
  		getInstance().addServer(name, host, port, userName, userPass);
  	}
  }

  /**
   * Removes the live connection to the specified AMQP server.
   * 
   * @param names the name identifying the server or list of name separate by space
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
   * Initializes the service. Starts a connection with one server.
   */
  public static void init(String args, boolean firstTime) throws Exception {
  	if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "AmqpConnectionService.init(" + args + ", " + firstTime + ')');
    }
  	
    if (firstTime) {
      if (args != null && args.length() > 0) {
      	addServer(args);
      }
    } else {
      getInstance().readSavedConf();
    }

  }
  
  /**
   * Stops all connections to AMQP servers.
   */
  public static void stopService() {
    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "Stopping AmqpConnectionHandler service.");
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
