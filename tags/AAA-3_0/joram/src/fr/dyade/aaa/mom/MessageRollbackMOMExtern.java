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

/** 
 *	a MessageQueueDeliverMOMExtern allows a TCP Proxy to
 *	deliver a message to a client on its request
 * 
 *	@see         subclasses
 */ 
 
public class MessageRollbackMOMExtern extends MessageMOMExtern { 

  /** the identity of the agent which has to acknowledge the message */ 
  private javax.jms.Destination dest;
  
  /** the identifier of the Session */  
  private String sessionID;

  /** this variable is the identifier of the message */ 
  private String JMSMessageID;

  /** the name of the subscription (Topic)*/
  private String nameSubscription;
  
  public MessageRollbackMOMExtern(long requestIdNew, javax.jms.Destination dest, String sessionID, String messageID) {
    super(requestIdNew);
    this.dest = dest;
    this.sessionID = sessionID;
    JMSMessageID = messageID;
  }
   
  /** return the jms message id */
  public java.lang.String getJMSMessageID() {
    return JMSMessageID;
  }
    
  /** return the agentClient */ 
  public javax.jms.Destination getJMSDestination() { 
    return dest; 
  } 
  
  /** return the sessionID */
  public String getJMSSessionID() {
    return sessionID;
  }
  /** return the name of the subscription */
  public String getJMSnameSubscription() {
    return nameSubscription;
  }

  /** set the name of the subscription */
  public void setJMSnameSubscription(String nameSub) {
    nameSubscription = nameSub;
  }
}
