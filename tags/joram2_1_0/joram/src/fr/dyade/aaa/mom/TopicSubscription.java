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
import java.util.*; 

/**
 * A <code>TopicSubscription</code> contains the information 
 * of a subscription to a <code>Theme</code> of a <code>Topic</code>.
 * <br>
 * It is uniquely identified by a <code>TopicSubscriptionKey</code>,
 * and stored in the subscriptionTable of the <code>Theme</code>.
 *	
 * @see  fr.dyade.aaa.mom.Topic
 * @see  fr.dyade.aaa.mom.Theme
 * @see  fr.dyade.aaa.mom.TopicSubscriptionKey
 */
public class TopicSubscription implements java.io.Serializable 
{ 
  /** The subscription's name. */
  private String subscriptionName;
  /**
   * The connection key of the subscriber. Only up-to-date
   * for a temporary subscription or a durable subscription 
   * at the first connection.
   */ 
  private int driverKey;
  /** The subscribing agentClient's ID. */
  private AgentId agentClient;
  /** If true, the client won't receive its own published messages. */ 
  private boolean noLocal;  
  /** The request's selector. */ 
  private String selector;  

  /** Constructor. */
  public TopicSubscription(boolean noLocal, String selector, 
    AgentId agentClient, String subscriptionName, int drvKey) 
  {
    this.subscriptionName = subscriptionName;
    this.noLocal = noLocal; 
    this.selector = selector;
    this.agentClient = agentClient;
    this.driverKey = drvKey;
  }


  /** Method returning the connection key. */
  public int getDriverKey() 
  {
    return driverKey;
  }


  /** Method returning the selector. */
  public String getSelector()
  {
    return selector;
  }


  /** Method returning the noLocal attribute. */
  public boolean getNoLocal()
  {
    return noLocal;
  }


  /** Method returning the id of the subscriber. */
  public AgentId getAgentClient()
  {
    return agentClient;
  }


  /** Method returning the name of the subscription. */
  public String getSubscriptionName()
  {
    return subscriptionName;
  }


  /** Method updating the subscription. */
  public void updateSubscription(boolean noLocal, String selector)
  {
    this.selector = selector;
    this.noLocal = noLocal;
  }

}
