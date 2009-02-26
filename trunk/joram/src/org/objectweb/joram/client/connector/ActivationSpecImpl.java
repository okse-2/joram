/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - Bull SA
 * Copyright (C) 2008 - ScalAgent Distributed Technologies
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

import javax.resource.ResourceException;
import javax.resource.spi.IllegalStateException;
import javax.resource.spi.InvalidPropertyException;
import javax.resource.spi.ResourceAdapter;

import org.objectweb.joram.shared.security.Identity;
import org.objectweb.joram.shared.security.SimpleIdentity;
import org.objectweb.util.monolog.api.BasicLevel;

/**
 * An <code>ActivationSpecImpl</code> instance holds configuration information
 * related to an endpoint deployment.
 */
public class ActivationSpecImpl
       implements javax.resource.spi.ActivationSpec,
                  javax.resource.spi.ResourceAdapterAssociation,
                  java.io.Serializable
{
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

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
  String identityClass = SimpleIdentity.class.getName();

  /** Message selector. */
  private String messageSelector = null;
  /** Subscription durability. */
  private String subscriptionDurability = null;
  /** Durable subscription name, if any. */
  private String subscriptionName;

  /** Acknowledgement mode. */
  private String acknowledgeMode = AUTO_ACKNOWLEDGE;

  /** Maximum number of work instances to be submitted (0 for infinite). */
  private String maxNumberOfWorks = "0";
  
  /**
   * The maximum number of messages that can be assigned 
   * to a server session at one time
   * Default is 10.
   */
  private String maxMessages = "10";

  /** Resource adapter central authority. */
  private transient ResourceAdapter ra = null;


  /** 
   * Constructs an <code>ActivationSpecImpl</code> instance.
   */
  public ActivationSpecImpl()
  {}

 
  /**
   * Checks if the configuration information is valid.
   *
   * @exception InvalidPropertyException  If a parameter is missing, incorrect,
   *                                      or not consistent with other
   *                                      parameters.
   */
  public void validate() throws InvalidPropertyException
  {
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
  public void setResourceAdapter(ResourceAdapter ra) throws ResourceException
  {
    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
      AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG, this + " setResourceAdapter(" + ra + ")");

    if (this.ra != null)
      throw new IllegalStateException("Can not change resource adapter"
                                      + " association.");

    if (! (ra instanceof JoramAdapter))
      throw new ResourceException("Provided resource adapter is not a JORAM "
                                  + "resource adapter: "
                                  + ra.getClass().getName());

    this.ra = ra;
  }

  /** Returns the resource adapter central authority instance. */
  public ResourceAdapter getResourceAdapter()
  {
    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
      AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG, this + " getResourceAdapter = " + ra);

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

  /** Sets the acknowledgement mode. */
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

  /** Returns the subscription durabbility. */
  public String getSubscriptionDurability()
  {
    return subscriptionDurability;
  }

  /** Returns the name of the durable subscription. */
  public String getSubscriptionName()
  {
    return subscriptionName;
  }

  /** Returns the acknowledgement mode. */
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
}
