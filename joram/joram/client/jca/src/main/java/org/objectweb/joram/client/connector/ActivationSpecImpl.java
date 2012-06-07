/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - 208 Bull SA
 * Copyright (C) 2008 - 2012 ScalAgent Distributed Technologies
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
 * Initial developer(s): Frederic Maistre (Bull SA)
 * Contributor(s): Nicolas Tachker (Bull SA)
 */
package org.objectweb.joram.client.connector;

import java.util.Properties;

import javax.resource.ResourceException;
import javax.resource.spi.IllegalStateException;
import javax.resource.spi.InvalidPropertyException;
import javax.resource.spi.ResourceAdapter;

import org.objectweb.joram.shared.security.SimpleIdentity;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Debug;

/**
 * An <code>ActivationSpecImpl</code> instance holds configuration information
 * related to an endpoint deployment.
 */
public class ActivationSpecImpl
       implements javax.resource.spi.ActivationSpec,
                  javax.resource.spi.ResourceAdapterAssociation,
                  java.io.Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public static Logger logger = Debug.getLogger(ActivationSpecImpl.class.getName());
  
  /**
   * Value for the property <code>acknowledgeMode</code>
   * defined in the MDB deployment descriptor.
   */
  public static final String AUTO_ACKNOWLEDGE = "Auto-acknowledge";
  
  /**
   * Value for the property <code>acknowledgeMode</code>
   * defined in the MDB deployment descriptor.
   */
  public static final String DUPS_OK_ACKNOWLEDGE = "Dups-ok-acknowledge";
  
  /** The type of the destination to get messages from. */
  private String destinationType;
  /** The name of the destination to get messages from. */
  private String destination;

  /** User identification. */
  private String userName = "anonymous";
  /** User password. */
  private String password = "anonymous";
  /** identity class name. */
  private String identityClass = SimpleIdentity.class.getName();

  /** Message selector. */
  private String messageSelector = null;
  /** Subscription durability. */
  private String subscriptionDurability = null;
  /** Durable subscription name, if any. */
  private String subscriptionName;

  /** Acknowledgement mode. */
  private String acknowledgeMode = AUTO_ACKNOWLEDGE;

  /** Maximum number of work instances to be submitted (0 for infinite). */
  private String maxNumberOfWorks = "10";
  
  /**
   * The maximum number of messages that can be assigned 
   * to a server session at one time
   * Default is 10.
   */
  private String maxMessages = "10";
  
  /**
   * Determine whether durable subscription must be deleted or not
   * at close time of the InboundConsumer.
   * <p>
   * Default is false.
   */
  private Boolean deleteDurableSubscription = false;

  /** Resource adapter central authority. */
  private transient JoramAdapter ra = null;

  /** <code>true</code> if the underlying JORAM server is collocated. */
  private Boolean collocated = false;

  public void setCollocated(String collocatedServer) {
  	if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "ActivationSpecImpl.setCollocated(" + collocatedServer + ')');
    collocated = new Boolean(collocatedServer);
  }

  public Boolean getCollocated() {
  	return new Boolean(collocated);
  }

  /** Host name or IP of the underlying JORAM server. */
  private String hostName = null;

  public String getHostName() {
    return hostName;
  }

  public void setHostName(String hostName) {
  	if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "ActivationSpecImpl.setHostName(" + hostName + ')');
    this.hostName = hostName;
  }

  /** Port number of the underlying JORAM server. */
  private int serverPort = -2;

  public Integer getServerPort() {
    return new Integer(serverPort);
  }

  public void setServerPort(String serverPort) {
  	if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "ActivationSpecImpl.setServerPort(" + serverPort + ')');
    this.serverPort = new Integer(serverPort).intValue();
  }

  /** URL hajoram (for collocated mode). */
  private String haURL = null;

  public String getHAURL() {
  	if (haURL == null)
  		if (ra != null)
  			haURL = ra.getHAURL();
    return haURL;
  }

  public void setHAURL(String haURL) {
    this.haURL = haURL;
  }
  
  /** <code>true</code> if the underlying a JORAM HA server is defined */
  private Boolean isHA = false;
  
  public void setIsHA(String isHA) {
    this.isHA = new Boolean(isHA);
  }
  
  public Boolean isHA() {
    return new Boolean(isHA);
  }
  
  /**
   * Duration in seconds during which connecting is attempted (connecting
   * might take time if the server is temporarily not reachable); the 0 value
   * is set for connecting only once and aborting if connecting failed.
   */
  private int connectingTimer = 0;

  public Integer getConnectingTimer() {
    return new Integer(connectingTimer);
  }

  public void setConnectingTimer(String connectingTimer) {
    this.connectingTimer = new Integer(connectingTimer).intValue();
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
  
  public void setTxPendingTimer(String txPendingTimer) {
    this.txPendingTimer = new Integer(txPendingTimer).intValue();
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

  public void setCnxPendingTimer(String cnxPendingTimer) {
    this.cnxPendingTimer = new Integer(cnxPendingTimer).intValue();
  }
  
  /**
   * The maximum number of messages that can be
   * read at once from a queue.
   *
   * Default value is 2 in order to compensate
   * the former subscription mechanism.
   */
  private int queueMessageReadMax = 2;

  public Integer getQueueMessageReadMax() {
    return new Integer(queueMessageReadMax);
  }

  public void setQueueMessageReadMax(String queueMessageReadMax) {
    this.queueMessageReadMax = new Integer(queueMessageReadMax).intValue();
  }

  /**
   * The maximum number of acknowledgements
   * that can be buffered in
   * Session.DUPS_OK_ACKNOWLEDGE mode when listening to a topic.
   * Default is 0.
   */
  private int topicAckBufferMax = 0;

  public Integer getTopicAckBufferMax() {
    return new Integer(topicAckBufferMax);
  }

  public void setTopicAckBufferMax(String topicAckBufferMax) {
    this.topicAckBufferMax = new Integer(topicAckBufferMax).intValue();
  }

  /**
   * This threshold is the maximum messages
   * number over
   * which the subscription is passivated.
   * Default is Integer.MAX_VALUE.
   */
  private int topicPassivationThreshold = Integer.MAX_VALUE;

  public Integer getTopicPassivationThreshold() {
    return new Integer(topicPassivationThreshold);
  }

  public void setTopicPassivationThreshold(String topicPassivationThreshold) {
    this.topicPassivationThreshold = new Integer(topicPassivationThreshold).intValue();
  }

  /**
   * This threshold is the minimum
   * messages number below which
   * the subscription is activated.
   * Default is 0.
   */
  private int topicActivationThreshold = 0;

  public Integer getTopicActivationThreshold() {
    return new Integer(topicActivationThreshold);
  }

  public void setTopicActivationThreshold(String topicActivationThreshold) {
    this.topicActivationThreshold = new Integer(topicActivationThreshold).intValue();
  }
  
  private String name;
  
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

	/** 
   * Constructs an <code>ActivationSpecImpl</code> instance.
   */
  public ActivationSpecImpl() { 
  	if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "ActivationSpecImpl<>");
  }
 
  /**
   * Checks if the configuration information is valid.
   *
   * @exception InvalidPropertyException  If a parameter is missing, incorrect,
   *                                      or not consistent with other
   *                                      parameters.
   */
  public void validate() throws InvalidPropertyException {
    if (destinationType != null
        && ! destinationType.equals("javax.jms.Queue")
        && ! destinationType.equals("javax.jms.Topic"))
      throw new InvalidPropertyException("Invalid destination type: " 
                                         + destinationType);

    if (destination == null)
      throw new InvalidPropertyException("Missing destination property.");

    if (acknowledgeMode != null
        && ! acknowledgeMode.equals(AUTO_ACKNOWLEDGE)
        && ! acknowledgeMode.equals(DUPS_OK_ACKNOWLEDGE))
      throw new InvalidPropertyException("Invalid acknowledge mode: " 
                                         + acknowledgeMode);

    if (subscriptionDurability != null) {

      if (subscriptionDurability.equals("Durable")
          && destinationType.equals("javax.jms.Queue"))
        throw new InvalidPropertyException("Can't set a durable subscription "
                                           + "on a JMS queue.");

      if (subscriptionDurability.equals("Durable")
          && subscriptionName == null)
        throw new InvalidPropertyException("Missing durable subscription name.");
    }
  }


  /** Sets the resource adapter central authority. */
  public void setResourceAdapter(ResourceAdapter ra) throws ResourceException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this + " setResourceAdapter(" + ra + ")");

    if (this.ra != null)
      throw new IllegalStateException("Can not change resource adapter"
                                      + " association.");

    if (! (ra instanceof JoramAdapter))
      throw new ResourceException("Provided resource adapter is not a JORAM "
                                  + "resource adapter: "
                                  + ra.getClass().getName());

    this.ra = (JoramAdapter) ra;
    
    if (hostName == null && serverPort == -2) {
    	// set the ra value for this ActivationSpec
    	// this value can be override by the MDB descriptor
    	hostName = this.ra.getHostName();
    	serverPort = this.ra.getServerPort();
    	haURL = this.ra.getHAURL();
    	// userName = the default value is anonymous
    	// password = the default value is anonymous
    	identityClass = this.ra.getIdentityClass();
    	collocated = this.ra.getCollocated();
    	isHA = this.ra.getIsHa();
    }
  }

  /** Returns the resource adapter central authority instance. */
  public ResourceAdapter getResourceAdapter() {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this + " getResourceAdapter = " + ra);

    return ra;
  }


  // ------------------------------------------
  // --- JavaBean setter and getter methods ---
  // ------------------------------------------
  /**
   * Sets the destination type (either "javax.jms.Queue" or
   * "javax.jms.Topic").
   */ 
  public void setDestinationType(String destinationType)
  {
    this.destinationType = destinationType;
  }

  /** Sets the destination name. */
  public void setDestination(String destination)
  {
    this.destination = destination;
  }

  /** Sets the user identification. */
  public void setUserName(String userName)
  {
    this.userName = userName;
  }

  /** Sets the user password. */
  public void setPassword(String password)
  {
    this.password = password;
  }

  /** set the identity class name. */
  public void setIdentityClass(String identityClass) {
    this.identityClass = identityClass;
  }
  
  /** Sets the message selector. */
  public void setMessageSelector(String messageSelector)
  {
    this.messageSelector = messageSelector;
  }

  /** Sets the durability of the subscription. */
  public void setSubscriptionDurability(String subscriptionDurability)
  {
    this.subscriptionDurability = subscriptionDurability;
  }

  /** Sets the name of the durable subscription. */
  public void setSubscriptionName(String subscriptionName)
  {
    this.subscriptionName = subscriptionName;
  }

  /** Sets the acknowledgment mode. */
  public void setAcknowledgeMode(String acknowledgeMode)
  {
    this.acknowledgeMode = acknowledgeMode;
  }

  /** Sets the maximum number of work instances to be submitted. */
  public void setMaxNumberOfWorks(String maxNumberOfWorks)
  {
    this.maxNumberOfWorks = maxNumberOfWorks;
  }
  
  public void setMaxMessages(String maxMessages) {
    this.maxMessages = maxMessages;
  }
  
  /**
   * Set the deleteDurableSubscription attribute.
   * 
   * @param deleteDurableSubscription to set deleteDurableSubscription
   * @see #deleteDurableSubscription
   */
  public void setDeleteDurableSubscription(String deleteDurableSubscription) {
  	this.deleteDurableSubscription = new Boolean(deleteDurableSubscription);
  }

  /** Returns the destination type. */
  public String getDestinationType()
  {
    return destinationType;
  }

  /** Returns the destination name. */
  public String getDestination()
  {
    return destination;
  }

  /** Returns the user identification. */
  public String getUserName()
  {
    return userName;
  }

  /** Returns the user password. */
  public String getPassword()
  {
    return password;
  }

  /** Returns the identity class name. */
  public String getIdentityClass() {
    return identityClass;  
  }
  
  /** Returns the message selector. */
  public String getMessageSelector()
  {
    return messageSelector;
  }

  /** Returns the subscription durability. */
  public String getSubscriptionDurability()
  {
    return subscriptionDurability;
  }

  /** Returns the name of the durable subscription. */
  public String getSubscriptionName()
  {
    return subscriptionName;
  }

  /** Returns the acknowledgment mode. */
  public String getAcknowledgeMode()
  {
    return acknowledgeMode;
  }

  /** Returns the maximum number of work instances to be submitted. */
  public String getMaxNumberOfWorks()
  {
    return maxNumberOfWorks;
  }
  
  public String getMaxMessages() {
    return maxMessages;
  }
  
  /**
   * Returns the deleteDurableSubscription attribute.
   * 
   * @return the DeleteDurableSubscription
   * @see #deleteDurableSubscription
   */
  public Boolean getDeleteDurableSubscription() {
  	return new Boolean(deleteDurableSubscription);
  }
  
  public void setActivationSpecConfig(Properties props) {
  	if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "ActivationSpecImpl.setActivationSpecConfig(" + props + ')');
  	
  	if (props == null)
  		return;
  	name = props.getProperty("name");
  	hostName = props.getProperty("HostName", "localhost");
  	serverPort = new Integer(props.getProperty("ServerPort", "16010")).intValue();
  	haURL = props.getProperty("HAURL", "hajoram://localhost:16010,localhost:16011");

  	userName = props.getProperty("UserName", "anonymous");
  	password = props.getProperty("Password", "anonymous");
  	identityClass = props.getProperty("IdentityClass", "org.objectweb.joram.shared.security.SimpleIdentity");
  	collocated = new Boolean(props.getProperty("Collocated", "false")).booleanValue();
  	isHA = new Boolean(props.getProperty("IsHA", "false")).booleanValue();

  	connectingTimer = new Integer(props.getProperty("ConnectingTimer", "0")).intValue();
  	cnxPendingTimer = new Integer(props.getProperty("CnxPendingTimer", "0")).intValue();
  	txPendingTimer = new Integer(props.getProperty("TxPendingTimer", "0")).intValue();
  	
  	topicPassivationThreshold = new Integer(props.getProperty("TopicPassivationThreshold", ""+Integer.MAX_VALUE)).intValue();
  	topicActivationThreshold = new Integer(props.getProperty("TopicActivationThreshold", "0")).intValue();
  	topicAckBufferMax = new Integer(props.getProperty("TopicAckBufferMax", "0")).intValue();
  	subscriptionName = props.getProperty("SubscriptionName");
  	subscriptionDurability = props.getProperty("SubscriptionDurability");
  	queueMessageReadMax = new Integer(props.getProperty("QueueMessageReadMax", "2")).intValue();
  	messageSelector = props.getProperty("MessageSelector");
  	maxNumberOfWorks = props.getProperty("MaxNumberOfWorks", "0");
  	maxMessages = props.getProperty("MaxMessages", "10");
  	destinationType = props.getProperty("DestinationType");
  	destination = props.getProperty("Destination");
  	deleteDurableSubscription = new Boolean(props.getProperty("DeleteDurableSubscription", "false")).booleanValue();
  	acknowledgeMode = props.getProperty("AcknowledgeMode", AUTO_ACKNOWLEDGE);
  }

	/* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
	  return "ActivationSpecImpl@" + hashCode() + " [destinationType=" + destinationType
	      + ", destination=" + destination + ", userName=" + userName
	      + ", password=" + password + ", identityClass=" + identityClass
	      + ", messageSelector=" + messageSelector + ", subscriptionDurability="
	      + subscriptionDurability + ", subscriptionName=" + subscriptionName
	      + ", acknowledgeMode=" + acknowledgeMode + ", maxNumberOfWorks="
	      + maxNumberOfWorks + ", maxMessages=" + maxMessages
	      + ", deleteDurableSubscription=" + deleteDurableSubscription + ", ra="
	      + ra + ", collocated=" + collocated + ", hostName=" + hostName
	      + ", serverPort=" + serverPort + ", haURL=" + haURL + ", isHA=" + isHA
	      + ", connectingTimer=" + connectingTimer + ", txPendingTimer="
	      + txPendingTimer + ", cnxPendingTimer=" + cnxPendingTimer
	      + ", queueMessageReadMax=" + queueMessageReadMax
	      + ", topicAckBufferMax=" + topicAckBufferMax
	      + ", topicPassivationThreshold=" + topicPassivationThreshold
	      + ", topicActivationThreshold=" + topicActivationThreshold + ", name="
	      + name + "]";
  }
}
