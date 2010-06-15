/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2010 ScalAgent Distributed Technologies
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

import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.JMSSecurityException;
import javax.jms.Session;
import javax.jms.XAConnection;
import javax.jms.XAConnectionFactory;
import javax.jms.XASession;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.objectweb.joram.client.jms.XidImpl;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Daemon;
import fr.dyade.aaa.common.Debug;

public class JMSModule implements ExceptionListener {

  private static final Logger logger = Debug.getLogger(JMSDistribution.class.getName());

  /** <code>true</code> if the module is fully usable. */
  protected boolean usable = true;

  /** Message explaining why the module is not usable. */
  protected String notUsableMessage;

  /** Daemon used for the reconnection process. */
  protected ReconnectionDaemon reconnectionDaemon;

  /** serializable object for synchronization */
  protected Object lock = new String();

  /** Indicates to use an XAConnection. Default is false. */
  protected boolean isXA = false;

  /** Connection to the foreign JMS server. */
  protected Connection cnx;

  /** Session with the foreign JMS destination. */
  protected Session session;

  /** XAResource */
  protected XAResource xaRes = null;

  /** User identification for connecting to the foreign JMS server. */
  protected String userName = null;

  /** User password for connecting to the foreign JMS server. */
  protected String password = null;

  /** Name of the JNDI factory class to use. */
  protected String jndiFactory = null;

  /** JNDI URL. */
  protected String jndiUrl = null;

  /** ConnectionFactory JNDI name. */
  protected String cnxFactName;

  /** Destination JNDI name. */
  protected String destName;

  /** Foreign JMS destination object. */
  protected Destination dest = null;

  /** JMS clientID field. */
  protected String clientID = null;

  /** Connection factory object for connecting to the foreign JMS server. */
  protected ConnectionFactory cnxFact = null;

  public void init(Properties properties) {
    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "<init>(" + properties + ')');
    }

    jndiFactory = properties.getProperty("jndiFactory");
    jndiUrl = properties.getProperty("jndiUrl");

    cnxFactName = properties.getProperty("connectionFactoryName");
    if (cnxFactName == null) {
      throw new IllegalArgumentException("Missing ConnectionFactory JNDI name.");
    }

    destName = properties.getProperty("destinationName");
    if (destName == null) {
      throw new IllegalArgumentException("Missing Destination JNDI name.");
    }

    String userName = properties.getProperty("userName");
    String password = properties.getProperty("password");

    if (userName != null && password != null) {
      this.userName = userName;
      this.password = password;
    }

    clientID = properties.getProperty("clientId");

    isXA = Boolean.valueOf(properties.getProperty("useXAConnection", "false")).booleanValue();

    try {
      connect();
    } catch (JMSException exc) {
      if (logger.isLoggable(BasicLevel.ERROR)) {
        logger.log(BasicLevel.ERROR, "Not usable: ", exc);
      }
    }
  }

  public void close() {
    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "close()");
    }

    try {
      cnx.setExceptionListener(null);
    } catch (JMSException exc1) {
      logger.log(BasicLevel.ERROR, "", exc1);
    }

    try {
      cnx.stop();
    } catch (JMSException exc) {
    }

    try {
      reconnectionDaemon.stop();
    } catch (Exception exc) {
    }

    try {
      cnx.close();
    } catch (JMSException exc) {
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
  private void connect() throws JMSException {
    if (!usable) {
      throw new IllegalStateException(notUsableMessage);
    }

    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "connect()");
    }

    // Creating the module's daemons.
    reconnectionDaemon = new ReconnectionDaemon();

    StartupDaemon startup = new StartupDaemon();
    startup.start();

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
    cnx.setExceptionListener(this);

    if (clientID != null) {
      cnx.setClientID(clientID);
    }

    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "doConnect: cnx=" + cnx);
    }

    session = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
  }

  /**
   * Opens a XA connection with the foreign JMS server and creates the XA JMS
   * resources for interacting with the foreign JMS destination.
   * 
   * @exception JMSException
   *              If the needed JMS resources could not be created.
   */
  protected void doXAConnect() throws JMSException {
    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "doXAConnect()");
    }

    if (userName != null && password != null) {
      cnx = ((XAConnectionFactory) cnxFact).createXAConnection(userName, password);
    } else {
      cnx = ((XAConnectionFactory) cnxFact).createXAConnection();
    }
    cnx.setExceptionListener(this);

    if (clientID != null) {
      cnx.setClientID(clientID);
    }

    cnx.start();
    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "doXAConnect: cnx=" + cnx);
    }

    session = ((XAConnection) cnx).createXASession();

    xaRes = ((XASession) session).getXAResource();

    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "doXAConnect: res=" + xaRes);
    }

    // Recover if needed.
    new XARecoverDaemon(xaRes).start();
  }

  /**
   * Implements the <code>javax.jms.ExceptionListener</code> interface for
   * catching the failures of the connection to the remote JMS server.
   * <p>
   * Reacts by launching a reconnection process.
   */
  public void onException(JMSException exc) {
    if (logger.isLoggable(BasicLevel.WARN)) {
      logger.log(BasicLevel.WARN, "onException(" + exc + ')');
    }
    reconnectionDaemon.reconnect();
  }

  protected void connectionDone() {
    // Nothing to do;
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

      javax.naming.Context jndiCtx = null;
      ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
      try {
        canStop = true;

        // Administered objects still to be retrieved: getting them from
        // JNDI.
        if (cnxFact == null || dest == null) {

          Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());

          if (jndiFactory == null || jndiUrl == null) {
            jndiCtx = new javax.naming.InitialContext();
          } else {
            java.util.Hashtable env = new java.util.Hashtable();
            env.put(javax.naming.Context.INITIAL_CONTEXT_FACTORY, jndiFactory);
            env.put(javax.naming.Context.PROVIDER_URL, jndiUrl);
            jndiCtx = new javax.naming.InitialContext(env);
          }
          cnxFact = (ConnectionFactory) jndiCtx.lookup(cnxFactName);
          dest = (Destination) jndiCtx.lookup(destName);

          if (logmon.isLoggable(BasicLevel.DEBUG)) {
            logmon.log(BasicLevel.DEBUG, "run: factory=" + cnxFact + ", destination=" + dest);
          }

        }
        try {
          if (isXA) {
            doXAConnect();
          } else {
            doConnect();
          }
          if (usable) {
            connectionDone();
          }

        } catch (AbstractMethodError exc) {
          usable = false;
          notUsableMessage = "Retrieved administered objects types not "
              + "compatible with the 'unified' communication " + " mode: " + exc;
          if (logmon.isLoggable(BasicLevel.DEBUG)) {
            logmon.log(BasicLevel.DEBUG, "Exception:: notUsableMessage=" + notUsableMessage, exc);
          }
        } catch (ClassCastException exc) {
          usable = false;
          notUsableMessage = "Retrieved administered objects types not "
              + "compatible with the chosen communication mode: " + exc;
          if (logmon.isLoggable(BasicLevel.DEBUG)) {
            logmon.log(BasicLevel.DEBUG, "Exception:: notUsableMessage=" + notUsableMessage, exc);
          }
        } catch (JMSSecurityException exc) {
          usable = false;
          notUsableMessage = "Provided user identification does not allow "
              + "to connect to the foreign JMS server: " + exc;
          if (logmon.isLoggable(BasicLevel.DEBUG)) {
            logmon.log(BasicLevel.DEBUG, "Exception:: notUsableMessage=" + notUsableMessage, exc);
          }
        } catch (JMSException exc) {
          if (logmon.isLoggable(BasicLevel.DEBUG)) {
            logmon.log(BasicLevel.DEBUG, "Exception:: ", exc);
          }
          reconnectionDaemon.reconnect();
        } catch (Throwable exc) {
          usable = false;
          notUsableMessage = "" + exc;
          if (logmon.isLoggable(BasicLevel.DEBUG)) {
            logmon.log(BasicLevel.DEBUG, "Exception:: notUsableMessage=" + notUsableMessage, exc);
          }
        }
      } catch (javax.naming.NameNotFoundException exc) {
        usable = false;
        if (cnxFact == null) {
          notUsableMessage = "Could not retrieve ConnectionFactory [" + cnxFactName + "] from JNDI: " + exc;
        } else if (dest == null) {
          notUsableMessage = "Could not retrieve Destination [" + destName + "] from JNDI: " + exc;
        }
        if (logmon.isLoggable(BasicLevel.DEBUG)) {
          logmon.log(BasicLevel.DEBUG, "Exception:: notUsableMessage=" + notUsableMessage, exc);
        }
      } catch (javax.naming.NamingException exc) {
        usable = false;
        notUsableMessage = "Could not access JNDI: " + exc;
        if (logmon.isLoggable(BasicLevel.DEBUG)) {
          logmon.log(BasicLevel.DEBUG, "Exception:: notUsableMessage=" + notUsableMessage, exc);
        }
      } catch (ClassCastException exc) {
        usable = false;
        notUsableMessage = "Error while retrieving administered objects "
            + "through JNDI possibly because of missing " + "foreign JMS client libraries in classpath: "
            + exc;
        if (logmon.isLoggable(BasicLevel.DEBUG)) {
          logmon.log(BasicLevel.DEBUG, "Exception:: notUsableMessage=" + notUsableMessage, exc);
        }
      } catch (Exception exc) {
        usable = false;
        notUsableMessage = "Error while retrieving administered objects " + "through JNDI: " + exc;
        if (logmon.isLoggable(BasicLevel.DEBUG)) {
          logmon.log(BasicLevel.DEBUG, "Exception:: notUsableMessage=" + notUsableMessage, exc);
        }
      } finally {
        Thread.currentThread().setContextClassLoader(oldClassLoader);
        // Closing the JNDI context.
        try {
          jndiCtx.close();
        } catch (Exception exc) {
        }

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

    /** Notifies the daemon to start reconnecting. */
    protected void reconnect() {
      if (logmon.isLoggable(BasicLevel.DEBUG)) {
        logmon.log(BasicLevel.DEBUG, "reconnect() running=" + running);
      }

      if (running) {
        return;
      }

      start();
    }

    /** The daemon's loop. */
    public void run() {
      if (logmon.isLoggable(BasicLevel.DEBUG)) {
        logmon.log(BasicLevel.DEBUG, "run()");
      }

      int attempts = 0;
      long interval;

      try {
        while (running) {
          canStop = true;

          attempts++;

          if (attempts <= attempts1) {
            interval = interval1;
          } else if (attempts <= attempts2) {
            interval = interval2;
          } else {
            interval = interval3;
          }

          try {
            Thread.sleep(interval);
            if (isXA) {
              doXAConnect();
            } else {
              doConnect();
            }
            if (usable) {
              connectionDone();
            }

          } catch (Exception exc) {
            continue;
          }
          canStop = false;
          break;
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

  protected class XARecoverDaemon extends Daemon {
    private XAResource resource = null;

    /** Constructs a <code>XARecoverDaemon</code> thread. */
    protected XARecoverDaemon(XAResource resource) {
      super("XARecoverDaemon", logger);
      this.resource = resource;
      if (logmon.isLoggable(BasicLevel.DEBUG)) {
        logmon.log(BasicLevel.DEBUG, "XARecoverDaemon<init>");
      }
    }

    /** Releases the daemon's resources. */
    protected void close() {
    }

    /** Shuts the daemon down. */
    protected void shutdown() {
    }

    /** The daemon's loop. */
    public void run() {
      if (logmon.isLoggable(BasicLevel.DEBUG)) {
        logmon.log(BasicLevel.DEBUG, "run()");
      }

      synchronized (lock) {
        Xid xid = new XidImpl(new byte[0], 1, Long.toString(System.currentTimeMillis()).getBytes());
        if (logmon.isLoggable(BasicLevel.DEBUG)) {
          logmon.log(BasicLevel.DEBUG, "run: xid = " + xid);
        }

        try {
          resource.start(xid, XAResource.TMNOFLAGS);
        } catch (XAException exc) {
          if (logmon.isLoggable(BasicLevel.WARN)) {
            logmon.log(BasicLevel.WARN, "Exception:: XA can't start resource : " + resource, exc);
          }
        }

        try {
          Xid[] xids = resource.recover(XAResource.TMNOFLAGS);
          if (logmon.isLoggable(BasicLevel.DEBUG)) {
            logmon.log(BasicLevel.DEBUG, "run: XA xid.length=" + xids.length);
          }
          // if needed recover this resource, and commit.
          for (int i = 0; i < xids.length; i++) {
            if (logmon.isLoggable(BasicLevel.INFO)) {
              logmon
                  .log(BasicLevel.INFO, "XARecoverDaemon : commit this " + xids[i].getGlobalTransactionId());
            }
            resource.commit(xids[i], false);
            if (logmon.isLoggable(BasicLevel.DEBUG)) {
              logmon.log(BasicLevel.DEBUG, "run: XA commit xid=" + xids[i]);
            }
          }

          // ended the recover.
          resource.end(xid, XAResource.TMSUCCESS);
        } catch (XAException e) {
          if (logmon.isLoggable(BasicLevel.DEBUG)) {
            logmon.log(BasicLevel.DEBUG, "Exception:: run", e);
          }
        }
      }
    }
  }

}
