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
 
import java.lang.*; 

/** 
 * A <code>NotificationSubscription</code> wraps a subscription
 * query to a Topic.
 * 
 * @see  fr.dyade.aaa.mom.Topic 
 * @see  fr.dyade.aaa.mom.Theme
 * @see  fr.dyade.aaa.mom.AgentClient 
 */ 
public class NotificationSubscription
  extends fr.dyade.aaa.mom.NotificationMOMRequest
{ 
  /** The subscription name.  */
  public String nameSubscription;
  /** Path and name of the subscribed theme. */ 
  public String theme; 
  /** 
   * If true, the client won't receive its own
   * published messages.    
   */ 
  public boolean noLocal;  
  /** The selector of the request. */ 
  public String selector;  
  /** The nature of the subscription. */
  public boolean durable;

  public int ackMode;
  public String sessionID;

  public boolean connectionConsumer;
  
  /** Constructor used for durable subscriptions. */
  public NotificationSubscription(SubscriptionMessageMOMExtern msgSub)
  {
    super(msgSub.getMessageMOMExternID(), msgSub.getDriverKey());
    this.nameSubscription = msgSub.nameSubscription;
    this.theme = msgSub.topic.getTheme(); 
    this.noLocal = msgSub.noLocal; 
    this.selector = msgSub.selector;
    this.ackMode = msgSub.ackMode;
    this.sessionID = msgSub.sessionID;
    this.connectionConsumer = msgSub.connectionConsumer;
    this.durable = true;
  }

  /** Constructor used for non durable subscriptions. */
  public NotificationSubscription(SubscriptionNoDurableMOMExtern msgSub)
  {
    super(msgSub.getMessageMOMExternID(), msgSub.getDriverKey());
    this.nameSubscription = msgSub.nameSubscription;
    this.theme = msgSub.topic.getTheme(); 
    this.noLocal = msgSub.noLocal; 
    this.selector = msgSub.selector;
    this.ackMode = msgSub.ackMode;
    this.sessionID = msgSub.sessionID;
    this.connectionConsumer = msgSub.connectionConsumer;
    this.durable = false ;
  }
}
