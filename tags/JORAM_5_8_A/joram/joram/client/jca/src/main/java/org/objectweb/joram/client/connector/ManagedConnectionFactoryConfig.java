/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2012 ScalAgent Distributed Technologies
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
package org.objectweb.joram.client.connector;

import java.util.Properties;

import org.objectweb.joram.client.jms.FactoryParameters;
import org.objectweb.joram.shared.security.SimpleIdentity;


/**
 * A <code>ManagedConnectionFactoryConfig</code> store configuration of manages
 * outbound connectivity to a given JORAM server.
 */
public class ManagedConnectionFactoryConfig implements java.io.Serializable {
  private static final long serialVersionUID = 1L;

	/** <code>true</code> for collocated outbound connectivity. */
  private boolean collocated = false;

  /** Underlying JORAM server host name. */
  private String hostName = "localhost";
  /** Underlying JORAM server port number. -1 if collocated */
  private int serverPort = -1;

  /** Default user identification. */
  private String userName = "anonymous";
  /** Default user password. */
  private String password = "anonymous";
  /** Default identityClass*/
  private String identityClass = SimpleIdentity.class.getName();

  /**
   * Duration in seconds during which connecting is attempted (connecting
   * might take time if the server is temporarily not reachable); the 0 value
   * is set for connecting only once and aborting if connecting failed.
   */
  private int connectingTimer = 0;

  public Integer getConnectingTimer() {
    return new Integer(connectingTimer);
  }

  public void setConnectingTimer(Integer connectingTimer) {
    this.connectingTimer = connectingTimer.intValue();
  }

  /**
   * Duration in seconds during which a JMS transacted (non XA) session might
   * be pending; above that duration the session is rolled back and closed;
   * the 0 value means "no timer".
   */
  private int txPendingTimer = 0;

  public Integer getTxPendingTimer() {
    return new Integer(txPendingTimer);
  }
  
  public void setTxPendingTimer(Integer txPendingTimer) {
    this.txPendingTimer = txPendingTimer.intValue();
  }

  /**
   * Period in milliseconds between two ping requests sent by the client
   * connection to the server; if the server does not receive any ping
   * request during more than 2 * cnxPendingTimer, the connection is
   * considered as dead and processed as required.
   */
  private int cnxPendingTimer = 0;

  public Integer getCnxPendingTimer() {
    return new Integer(cnxPendingTimer);
  }

  public void setCnxPendingTimer(Integer cnxPendingTimer) {
    this.cnxPendingTimer = cnxPendingTimer.intValue();
  }

  /**
   * Determines whether the produced messages are asynchronously
   * sent or not (without or with acknowledgement)
   * Default is false (with ack).
   */
  public boolean asyncSend = false;

  public Boolean getAsyncSend() {
    return new Boolean(asyncSend);
  }

  public void setAsyncSend(Boolean asyncSend) {
    this.asyncSend = asyncSend.booleanValue();
  }
  
  public boolean isAsyncSend() {
    return asyncSend;
  }

  /**
   * Determines whether client threads
   * which are using the same connection
   * are synchronized in order to group
   * together the requests they send.
   * Default is false.
   */
  private boolean multiThreadSync = false;

  public Boolean getMultiThreadSync() {
    return new Boolean(multiThreadSync);
  }

  public void setMultiThreadSync(Boolean multiThreadSync) {
    this.multiThreadSync = multiThreadSync.booleanValue();
  }
  
  public boolean isMultiThreadSync() {
    return multiThreadSync;
  }

  /**
   * The maximum time the threads hang if 'multiThreadSync' is true.
   * Either they wake up (wait time out) or they are notified (by the
   * first woken up thread).
   * <p>
   * Default is 1 ms.
   */
  private int multiThreadSyncDelay = 1;

  public Integer getMultiThreadSyncDelay() {
    return new Integer(multiThreadSyncDelay);
  }

  public void setMultiThreadSyncDelay(Integer multiThreadSyncDelay) {
    this.multiThreadSyncDelay = multiThreadSyncDelay.intValue();
  }


  /**
   * This is the local IP address on which the TCP connection is activated. The
   * value can either be a machine name, such as "java.sun.com", or a textual
   * representation of its IP address.
   */
  private String outLocalAddress = null;

  /**
   * This is the local IP address port on which the TCP connection is activated
   */
  private int outLocalPort = 0;
  
  /**
   * Comma separated list of IN interceptors.
   */
  private String inInterceptors = null;

  /**
   * Comma separated list of OUT interceptors.
   */
  private String outInterceptors = null;

  private String mode = "Unified"; // Unified | PTP | PubSub
  
  private String name = null;

  /**
   * Constructs a <code>ManagedConnectionFactoryConfig</code> instance.
   */
  public ManagedConnectionFactoryConfig()
  {}

  protected void setParameters(Object factory) {
  	FactoryParameters fp = null;
  	if (factory instanceof org.objectweb.joram.client.jms.ConnectionFactory) {
  		org.objectweb.joram.client.jms.ConnectionFactory f =
  			(org.objectweb.joram.client.jms.ConnectionFactory) factory;
  		fp = f.getParameters();
  	} else if (factory instanceof org.objectweb.joram.client.jms.XAConnectionFactory) {
  		org.objectweb.joram.client.jms.XAConnectionFactory f =
  			(org.objectweb.joram.client.jms.XAConnectionFactory) factory;
  		fp = f.getParameters();
  	}
  	if (fp != null) {
  		fp.connectingTimer = connectingTimer;
  		fp.cnxPendingTimer = cnxPendingTimer;
  		fp.txPendingTimer = txPendingTimer;
  		if (asyncSend) {
  			fp.asyncSend = asyncSend;
  		}
  		if (multiThreadSync) {
  			fp.multiThreadSync = multiThreadSync;
  		}
  		if (multiThreadSyncDelay > 0) {
  			fp.multiThreadSyncDelay = multiThreadSyncDelay;
  		}
  		if (outLocalPort > 0) {
  			fp.outLocalPort = outLocalPort;
  		}
  		if (outLocalAddress != null) {
  			fp.outLocalAddress = outLocalAddress;
  		}
  		if (inInterceptors != null) {
  			String[] interceptorArray = inInterceptors.split(",");
  			if (interceptorArray != null) {
  				for (String interceptorClassName : interceptorArray) {
  					String interceptorName = interceptorClassName.trim();
  					if (interceptorName.length() > 0) {
  						fp.addInInterceptor(interceptorName);
  					}
  				}
  			}
  		}
  		if (outInterceptors != null) {
  			String[] interceptorArray = outInterceptors.split(",");
  			if (interceptorArray != null) {
  				for (String interceptorClassName : interceptorArray) {
  					String interceptorName = interceptorClassName.trim();
  					if (interceptorName.length() > 0) {
  						fp.addOutInterceptor(interceptorName);
  					}
  				}
  			}
  		}      

  	}
  }
 
  public void setCollocated(Boolean collocated) {
    this.collocated = collocated.booleanValue();
  }

  public void setHostName(String hostName) {
    this.hostName = hostName;
  }

  public void setServerPort(Integer serverPort) {
    this.serverPort = serverPort.intValue();
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public void setIdentityClass(java.lang.String identityClass) {
    this.identityClass = identityClass;  
  }
  
  public Boolean isCollocated() {
    return new Boolean(collocated);
  }

  public String getHostName() {
    return hostName;
  }

  public Integer getServerPort() {
    return new Integer(serverPort);
  }

  public String getUserName() {
    return userName;
  }

  public String getPassword() {
    return password;
  }
  
  public java.lang.String getIdentityClass() {
    return identityClass;
  }
  
  public java.lang.String getOutLocalAddress() {
    return outLocalAddress;
  }

  public Integer getOutLocalPort() {
    return new Integer(outLocalPort);
  }

  public void setOutLocalAddress(String outLocalAddress) {
    this.outLocalAddress = null;
    if ((outLocalAddress != null) && (outLocalAddress.length() > 0))
      this.outLocalAddress = outLocalAddress;
  }

  public void setOutLocalPort(Integer outLocalPort) {
    this.outLocalPort = outLocalPort.intValue();
  }

  public void setOutInterceptors(String outInterceptors) {
    this.outInterceptors = outInterceptors;
  }

  public void setInInterceptors(String inInterceptors) {
    this.inInterceptors = inInterceptors;
  }

	/**
   * @return the mode
   */
  public String getMode() {
  	return mode;
  }

	/**
   * @param mode the mode to set
   */
  public void setMode(String mode) {
  	this.mode = mode;
  }
  
  /**
   * @return the name
   */
  public String getName() {
  	return name;
  }

	/**
   * @param name the name to set
   */
  public void setName(String name) {
  	this.name = name;
  }

	public void setManagedConnectionFactoryConfig(Properties props) {
  	name = props.getProperty("name");
  	hostName = props.getProperty("HostName", "localhost");
  	serverPort = new Integer(props.getProperty("ServerPort", "-1")).intValue();

  	userName = props.getProperty("UserName", "anonymous");
  	password = props.getProperty("Password", "anonymous");
  	identityClass = props.getProperty("IdentityClass", "org.objectweb.joram.shared.security.SimpleIdentity");
  	collocated = new Boolean(props.getProperty("Collocated", "true")).booleanValue();
  	outInterceptors = props.getProperty("OutInterceptors");
  	inInterceptors = props.getProperty("InInterceptors");

  	// Empty value corresponds to INADDRANY (wildcard address)
  	outLocalAddress = props.getProperty("outLocalAddress");
  	outLocalPort = new Integer(props.getProperty("outLocalPort", "0")).intValue();

  	connectingTimer = new Integer(props.getProperty("ConnectingTimer", "0")).intValue();
  	cnxPendingTimer = new Integer(props.getProperty("CnxPendingTimer", "0")).intValue();
  	txPendingTimer = new Integer(props.getProperty("TxPendingTimer", "0")).intValue();
  	multiThreadSync = new Boolean(props.getProperty("multiThreadSync", "false")).booleanValue();
  	multiThreadSyncDelay = new Integer(props.getProperty("multiThreadSyncDelay", "1")).intValue();

  	mode = props.getProperty("mode", "Unified"); //"Unified | PTP | PubSub"
  	// TODO: unused by JOnAS
// <!--Unified-->
// <!--ConnectionFactoryInterface = javax.jms.ConnectionFactory-->
// <!--ConnectionFactoryImpl = org.objectweb.joram.client.connector.OutboundConnectionFactory-->
// <!--ConnectionInterface = javax.jms.Connection-->
// <!--ConnectionImpl = org.objectweb.joram.client.connector.OutboundConnection-->
// <!--PTP-->
// <!--ConnectionFactoryInterface = javax.jms.QueueConnectionFactory-->
// <!--ConnectionFactoryImpl = org.objectweb.joram.client.connector.OutboundQueueConnectionFactory-->
// <!--ConnectionInterface = javax.jms.QueueConnection-->
// <!--ConnectionImpl = org.objectweb.joram.client.connector.OutboundQueueConnection-->
// <!--PubSub-->
// <!--ConnectionFactoryInterface = javax.jms.TopicConnectionFactory-->
// <!--ConnectionFactoryImpl = org.objectweb.joram.client.connector.OutboundTopicConnectionFactory-->
// <!--ConnectionInterface = javax.jms.TopicConnection-->
// <!--ConnectionImpl = org.objectweb.joram.client.connector.OutboundTopicConnection-->
  }
}
