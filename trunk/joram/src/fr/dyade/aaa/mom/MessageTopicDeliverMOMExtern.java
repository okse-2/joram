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
 * A <code>MessageTopicDeliverMOMExtern</code> contains the
 * message sent by a <code>Topic</code> to a client subscriber.
 */ 
public class MessageTopicDeliverMOMExtern extends MessageMOMExtern
{ 
  /** The subscription name. */
  public String nameSubscription;

  /** The message. */
  public fr.dyade.aaa.mom.Message message;

  /** The subscription theme. */
  public String theme;

  public boolean connectionConsumer = false;

  public boolean toListener = true;

  /** Constructor. */
  public MessageTopicDeliverMOMExtern(long requestID, String nameSubscription, 
    fr.dyade.aaa.mom.Message message, String theme, int driversKey)
  {
    super(requestID, driversKey);
    this.nameSubscription = nameSubscription;
    this.message = message;
    this.theme = theme;
  }
  public MessageTopicDeliverMOMExtern(long requestID, String nameSubscription, 
    fr.dyade.aaa.mom.Message message, String theme, int driversKey,
    boolean connectionConsumer)
  {
  this(requestID, nameSubscription, message, theme, driversKey);
  this.connectionConsumer = connectionConsumer;
  }
	
}
