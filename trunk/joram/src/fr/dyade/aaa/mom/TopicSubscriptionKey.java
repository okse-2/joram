/*
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
 *
 * The contents of this file are subject to the Joram Public License,
 * as defined by the file JORAM_LICENSE.TXT 
 * 
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License on the Objectweb web site
 * (www.objectweb.org). 
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific terms governing rights and limitations under the License. 
 * 
 * The Original Code is Joram, including the java packages fr.dyade.aaa.agent,
 * fr.dyade.aaa.util, fr.dyade.aaa.ip, fr.dyade.aaa.mom, and fr.dyade.aaa.joram,
 * released May 24, 2000. 
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
 */
package fr.dyade.aaa.mom; 

import fr.dyade.aaa.agent.*;
import java.lang.*;

/** 
 * A <code>TopicSubscriptionKey</code> uniquely identifies
 * a given <code>TopicSubscription</code>, stored in
 * in the subscriptionTable of a <code>Theme</code>.
 * 
 * @author Frederic Maistre
 *
 * @see  fr.dyade.aaa.mom.CommonClientAAA
 * @see  fr.dyade.aaa.mom.Theme
 * @see  fr.dyade.aaa.mom.TopicSubscription
 */
public class TopicSubscriptionKey implements java.io.Serializable
{ 
  /** The subscribing agentClient. */
  AgentId clientId;
  /** The subscription's name. */
  String subscriptionName;
  /* The connection key of the subscriber. */
  int driverKey;
  /** Nature of the subscription. */
  boolean durable;


  /**
   * Constructor.
   */
  public TopicSubscriptionKey(AgentId clientId, String subscriptionName,
    int driverKey, boolean durable)
  {
    this.clientId = clientId;
    this.subscriptionName = subscriptionName;
    this.driverKey = driverKey;
    this.durable = durable;
  }


  /** equals() method. */
  public boolean equals(Object obj)
  {
    if(obj instanceof TopicSubscriptionKey) {
      TopicSubscriptionKey key = (TopicSubscriptionKey) obj;
      if (durable)
        return subscriptionName.equals(key.subscriptionName);
      else
        return (clientId.equals(key.clientId) &&
          subscriptionName.equals(key.subscriptionName) &&
          driverKey == key.driverKey);
    } 
    else	
      return false;
  }


  /**
   * Method returning the hashcode of the key.
   * Allows to build a given key for a given subscription.
   * <br> 
   * Durable subscriptions have a unique name whereas
   * temporary subscriptions names (constructed as
   * sessionID+"_"+consumerID) that correspond to a
   * connection (identified by a driver key) of a given
   * agentClient might conflict with names of subscriptions
   * corresponding to an other connection and/or an other
   * agentClient. That's why in that case the clientId and
   * the driver key are added to the subscription name string
   * for computing the hashcode.
   */
  public int hashCode()
  {
    if (durable)
      return subscriptionName.hashCode();
    else
      return (clientId + "_" + driverKey + "_" + subscriptionName).hashCode();
  }

}
