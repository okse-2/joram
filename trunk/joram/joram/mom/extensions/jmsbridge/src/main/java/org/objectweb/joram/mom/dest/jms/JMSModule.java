/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2010 - 2013 ScalAgent Distributed Technologies
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
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.JMSSecurityException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.objectweb.joram.mom.dest.jms.JMSAcquisition.JmsListener;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.common.Daemon;
import fr.dyade.aaa.common.Debug;
import fr.dyade.aaa.util.management.MXWrapper;

public class JMSModule implements ExceptionListener, Serializable, JMSModuleMBean {

  private static final Logger logger = Debug.getLogger(JMSModule.class.getName());

  /** <code>true</code> if the module is fully usable. */
  protected transient boolean notUsable = false;

  /** Message explaining why the module is not usable. */
  protected transient String notUsableMessage;

  /** Daemon used for the reconnection process. */
  protected transient ReconnectionDaemon reconnectionDaemon;

  /** Connection to the foreign JMS server. */
  protected transient volatile Connection cnx;

  /**
   * @return the cnx
   */
  public Connection getCnx() {
    return cnx;
  }

  /** the name identifying the remote JMS provider */
  private String name;
  
  public String getName() {
    return name;
  }
  
  /** User identification for connecting to the foreign JMS server. */
  protected String userName = null;

  /**
   * @return the user name
   */
  public String getUserName() {
    return userName;
  }

  /** User password for connecting to the foreign JMS server. */
  protected String password = null;

  /** Name of the JNDI factory class to use. */
  protected String jndiFactory = null;
  
  public String getNamingFactory() {
    return jndiFactory;
  }

  /** JNDI URL. */
  protected String jndiUrl = null;
  
  public String getNamingURL() {
    return jndiUrl;
  }

  /** ConnectionFactory JNDI name. */
  protected String cnxFactName;

  /**
   * @return the cnxFactName
   */
  public String getCnxFactName() {
    return cnxFactName;
  }

  /** JMS clientID field. */
  protected String clientID = null;
  
  public String getClientID() {
    return clientID;
  }

  /** Connection factory object for connecting to the foreign JMS server. */
  protected transient ConnectionFactory cnxFact = null;

  public JMSModule(String name, String cnxFactoryName, String jndiFactoryClass, String jndiUrl,
                   String user, String password, String clientID) {
    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "JMSModule.<init>");
    }

    this.name = name;
    
    this.jndiFactory = jndiFactoryClass;
    this.jndiUrl = jndiUrl;

    this.cnxFactName = cnxFactoryName;
    if (cnxFactName == null) {
      throw new IllegalArgumentException("Missing ConnectionFactory JNDI name.");
    }

    this.userName = user;
    this.password = password;

    this.clientID = clientID;

  }

  public void stopLiveConnection() {
    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "JMSModule.stopLiveConnection()");
    }

    if (cnx != null) {
      try {
        cnx.setExceptionListener(null);
      } catch (JMSException exc) {
        logger.log(BasicLevel.WARN, "JMSModule.stopLiveConnection", exc);
      }
    }

    if (cnx != null) {
      try {
        cnx.stop();
      } catch (JMSException exc) {
        logger.log(BasicLevel.WARN, "JMSModule.stopLiveConnection", exc);
      }
    }

    if (reconnectionDaemon != null) {
      try {
        reconnectionDaemon.stop();
      } catch (Exception exc) {
        logger.log(BasicLevel.WARN, "JMSModule.stopLiveConnection", exc);
      }
    }

    if (cnx != null) {
      try {
        cnx.close();
      } catch (JMSException exc) {
        logger.log(BasicLevel.WARN, "JMSModule.stopLiveConnection", exc);
      }
    }

    try {
      MXWrapper.unregisterMBean(getMBeanName());
    } catch (Exception exc) {
      if (logger.isLoggable(BasicLevel.DEBUG)) {
        logger.log(BasicLevel.DEBUG, "unregisterMBean", exc);
      }
    }
  }

  /**
   * Launches the connection process to the foreign JMS server.
   * 
   * @exception javax.jms.IllegalStateException
   *              If the module can't access the foreign JMS server.
   * @exception javax.jms.JMSException
   *              If the needed JMS resources can't be created.
   */
  public void startLiveConnection() {
    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "JMSModule.startLiveConnection()");
    }
    if (notUsable) {
      if (logger.isLoggable(BasicLevel.ERROR)) {
        logger.log(BasicLevel.ERROR, "JMSModule.startLiveConnection, not usable: " + notUsableMessage);
      }
      return;
    }

    // Creating the module's daemons.
    reconnectionDaemon = new ReconnectionDaemon();

    StartupDaemon startup = new StartupDaemon();
    startup.start();

    try {
      MXWrapper.registerMBean(this, getMBeanName());
    } catch (Exception e) {
      if (logger.isLoggable(BasicLevel.DEBUG)) {
        logger.log(BasicLevel.DEBUG, "registerMBean", e);
      }
    }

  }

  protected Object retrieveJndiObject(String jndiName) throws Exception {

    Context jndiCtx = null;
    ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();

    try {
      jndiCtx = getInitialContext();
      return jndiCtx.lookup(jndiName);

    } catch (Exception exc) {
      throw exc;
    } finally {
      // Closing the JNDI context.
      if (jndiCtx != null) {
        jndiCtx.close();
      }
      Thread.currentThread().setContextClassLoader(oldClassLoader);
    }
  }

  protected Context getInitialContext() throws IOException, NamingException {
    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "getInitialContext() - Load jndi.properties file");
    }
    Context jndiCtx;
    Properties props = new Properties();
    InputStream in = Class.class.getResourceAsStream("/jndi.properties");
    
    if (in == null) {
      if (logger.isLoggable(BasicLevel.DEBUG)) {
        logger.log(BasicLevel.DEBUG, "jndi.properties not found.");
      }
    } else {
      props.load(in);
    }

    // Override jndi.properties with properties given at initialization if present
    if (jndiFactory != null) {
      props.setProperty(Context.INITIAL_CONTEXT_FACTORY, jndiFactory);
    }
    if (jndiUrl != null) {
      props.setProperty(Context.PROVIDER_URL, jndiUrl);
    }

    Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
    jndiCtx = new InitialContext(props);
    return jndiCtx;
  }

  /**
   * Opens a connection with the foreign JMS server and creates the JMS
   * resources for interacting with the foreign JMS destination.
   * 
   * @exception JMSException
   *              If the needed JMS resources could not be created.
   */
  protected void doConnect() throws JMSException {
    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "doConnect()");
    }

    if (userName != null && password != null) {
      cnx = cnxFact.createConnection(userName, password);
    } else {
      cnx = cnxFact.createConnection();
    }

    if (clientID == null) {
      cnx.setClientID("joramBridge_" + name + "_" + AgentServer.getServerId());
    } else {
      cnx.setClientID(clientID);
    }
    
    cnx.setExceptionListener(this);

    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "doConnect: cnx=" + cnx);
    }
  }

  /**
   * Implements the <code>javax.jms.ExceptionListener</code> interface for
   * catching the failures of the connection to the remote JMS server.
   * <p>
   * Reacts by launching a reconnection process.
   */
  public void onException(JMSException exc) {
  	if (logger.isLoggable(BasicLevel.WARN)) {
  		logger.log(BasicLevel.WARN, "JMSModule.onException(" + exc + ')');
  	}

  	if (listeners != null) {
  		for (Iterator<JmsListener> listener = listeners.iterator(); listener.hasNext();) {
  			JmsListener type = listener.next();
  			type.onException(exc);
  		}
  		listeners.clear();
  	}
  	reconnectionDaemon.start();
  }

  public boolean isConnectionOpen() {
    return cnx != null && !reconnectionDaemon.isRunning();
  }

  public String getState() {
    if (isConnectionOpen()) {
      return "OK";
    }
    return "FAILING";
  }

  private String getMBeanName() {
    StringBuilder strbuf = new StringBuilder();

    strbuf.append(JMSConnectionService.JMXBaseName).append(AgentServer.getServerId());
    strbuf.append(':');
    strbuf.append("type=Connections,name=").append(name);

    return strbuf.toString();
  }

  /**
   * The <code>StartupDaemon</code> thread is responsible for retrieving the
   * needed JMS administered objects from the JNDI server.
   */
  protected class StartupDaemon extends Daemon {
    /** Constructs a <code>StartupDaemon</code> thread. */
    protected StartupDaemon() {
      super("StartupDaemon", logger);
      setDaemon(false);
      if (logmon.isLoggable(BasicLevel.DEBUG)) {
        logmon.log(BasicLevel.DEBUG, "StartupDaemon<init>");
      }
    }

    /** The daemon's loop. */
    public void run() {
      if (logmon.isLoggable(BasicLevel.DEBUG)) {
        logmon.log(BasicLevel.DEBUG, "run()");
      }
      try {
        canStop = true;

        if (cnxFact == null) {
          // Administered objects still to be retrieved: getting them from JNDI.
          cnxFact = (ConnectionFactory) retrieveJndiObject(cnxFactName);
        }
        try {
          doConnect();
        } catch (AbstractMethodError exc) {
          notUsable = true;
          notUsableMessage = "Retrieved administered objects types not "
              + "compatible with the 'unified' communication " + " mode: " + exc;
          if (logmon.isLoggable(BasicLevel.DEBUG)) {
            logmon.log(BasicLevel.DEBUG, "Exception:: notUsableMessage=" + notUsableMessage, exc);
          }
        } catch (ClassCastException exc) {
          notUsable = true;
          notUsableMessage = "Retrieved administered objects types not "
              + "compatible with the chosen communication mode: " + exc;
          if (logmon.isLoggable(BasicLevel.DEBUG)) {
            logmon.log(BasicLevel.DEBUG, "Exception:: notUsableMessage=" + notUsableMessage, exc);
          }
        } catch (JMSSecurityException exc) {
          notUsable = true;
          notUsableMessage = "Provided user identification does not allow "
              + "to connect to the foreign JMS server: " + exc;
          if (logmon.isLoggable(BasicLevel.DEBUG)) {
            logmon.log(BasicLevel.DEBUG, "Exception:: notUsableMessage=" + notUsableMessage, exc);
          }
        } catch (JMSException exc) {
          if (logmon.isLoggable(BasicLevel.DEBUG)) {
            logmon.log(BasicLevel.DEBUG, "Exception:: ", exc);
          }
          reconnectionDaemon.start();
        } catch (Throwable exc) {
          notUsable = true;
          notUsableMessage = "" + exc;
          if (logmon.isLoggable(BasicLevel.DEBUG)) {
            logmon.log(BasicLevel.DEBUG, "Exception:: notUsableMessage=" + notUsableMessage, exc);
          }
        }
      } catch (NameNotFoundException exc) {
        if (cnxFact == null) {
          notUsableMessage = "Could not retrieve ConnectionFactory [" + cnxFactName + "] from JNDI: " + exc;
        }
        reconnectionDaemon.start();
        if (logmon.isLoggable(BasicLevel.DEBUG)) {
          logmon.log(BasicLevel.DEBUG, "Exception:: notUsableMessage=" + notUsableMessage, exc);
        }
      } catch (javax.naming.NoInitialContextException exc) {
        notUsable = true;
        notUsableMessage = "Initial context not available: " + exc;
        if (logmon.isLoggable(BasicLevel.DEBUG)) {
          logmon.log(BasicLevel.DEBUG, "Exception:: notUsableMessage=" + notUsableMessage, exc);
        }
      } catch (javax.naming.NamingException exc) {
        notUsableMessage = "Could not access JNDI: " + exc;
        if (logmon.isLoggable(BasicLevel.DEBUG)) {
          logmon.log(BasicLevel.DEBUG, "Exception:: notUsableMessage=" + notUsableMessage, exc);
        }
        reconnectionDaemon.start();
        if (logmon.isLoggable(BasicLevel.DEBUG)) {
          logmon.log(BasicLevel.DEBUG, "Exception:: notUsableMessage=" + notUsableMessage, exc);
        }
      } catch (ClassCastException exc) {
        notUsable = true;
        notUsableMessage = "Error while retrieving administered objects "
            + "through JNDI possibly because of missing " + "foreign JMS client libraries in classpath: "
            + exc;
        if (logmon.isLoggable(BasicLevel.DEBUG)) {
          logmon.log(BasicLevel.DEBUG, "Exception:: notUsableMessage=" + notUsableMessage, exc);
        }
      } catch (Exception exc) {
        notUsable = true;
        notUsableMessage = "Error while retrieving administered objects " + "through JNDI: " + exc;
        if (logmon.isLoggable(BasicLevel.DEBUG)) {
          logmon.log(BasicLevel.DEBUG, "Exception:: notUsableMessage=" + notUsableMessage, exc);
        }
      } finally {
        finish();
      }
    }

    /** Shuts the daemon down. */
    public void shutdown() {
    }

    /** Releases the daemon's resources. */
    public void close() {
    }
  }

  /**
   * The <code>ReconnectionDaemon</code> thread is responsible for reconnecting
   * the bridge module with the foreign JMS server in case of disconnection.
   */
  protected class ReconnectionDaemon extends Daemon {
    /** Number of reconnection trials of the first step. */
    private int attempts1 = 30;

    /** Retry interval (in milliseconds) of the first step. */
    private long interval1 = 1000L;

    /** Number of reconnection trials of the second step. */
    private int attempts2 = 55;

    /** Retry interval (in milliseconds) of the second step. */
    private long interval2 = 5000L;

    /** Retry interval (in milliseconds) of the third step. */
    private long interval3 = 60000L;

    /** Constructs a <code>ReconnectionDaemon</code> thread. */
    protected ReconnectionDaemon() {
      super("ReconnectionDaemon", logger);
      setDaemon(false);
      if (logmon.isLoggable(BasicLevel.DEBUG)) {
        logmon.log(BasicLevel.DEBUG, "ReconnectionDaemon<init>");
      }
    }

    /** The daemon's loop. */
    public void run() {
      if (logmon.isLoggable(BasicLevel.DEBUG)) {
        logmon.log(BasicLevel.DEBUG, "ReconnectionDaemon.run()");
      }

      int attempts = 0;
      long interval;

      try {
        while (running) {
          attempts++;

          if (attempts <= attempts1) {
            interval = interval1;
          } else if (attempts <= attempts2) {
            interval = interval2;
          } else {
            interval = interval3;
          }

          canStop = true;
          try {
            if (logmon.isLoggable(BasicLevel.DEBUG)) {
              logmon.log(BasicLevel.DEBUG, "ReconnectionDaemon: attempt " + attempts + ", wait=" + interval);
            }
            Thread.sleep(interval);

            if (logmon.isLoggable(BasicLevel.DEBUG)) {
              logmon.log(BasicLevel.DEBUG, "ReconnectionDaemon: connect...");
            }
            canStop = false;

            if (cnxFact == null) {
              // Administered objects still to be retrieved: getting them from JNDI.
              cnxFact = (ConnectionFactory) retrieveJndiObject(cnxFactName);
            }
            doConnect();

          } catch (Exception exc) {
            if (logmon.isLoggable(BasicLevel.DEBUG)) {
              logmon.log(BasicLevel.DEBUG, "ReconnectionDaemon: connection failed, continue... " + exc.getMessage());
            }
            continue;
          }

          if (logmon.isLoggable(BasicLevel.DEBUG)) {
            logmon.log(BasicLevel.DEBUG, "ReconnectionDaemon: Connected using " + cnxFactName + " connection factory.");
          }
          break;
        }
      } finally {
        finish();
      }
    }

    /** Shuts the daemon down. */
    public void shutdown() {
      interrupt();
    }

    /** Releases the daemon's resources. */
    public void close() {
    }
  }

  private transient List<JmsListener> listeners;

  void removeExceptionListener(JmsListener listener) {
    if ((listeners != null) && (listener != null)) {
      listeners.remove(listener);
    }
  }
  
  void addExceptionListener(JmsListener listener) {
    if (listeners == null) {
      listeners = new ArrayList<JmsListener>();
    }
    listeners.add(listener);
  }

}
