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
 * A <code>NotificationUnsubscription</code> tells a
 * <code>Topic</code> about a client unsubscription.
 * 
 * @see  fr.dyade.aaa.mom.Topic 
 */ 
public class NotificationUnsubscription
  extends fr.dyade.aaa.mom.NotificationMOMRequest
{ 
  /** The subscription name. */
  public java.lang.String nameSubscription;

  /** The subscription theme.  */ 
  public java.lang.String theme; 

  public String sessionID;

  /** Constructor. */ 
  public NotificationUnsubscription(long messageID,
    java.lang.String nameSubscription, java.lang.String theme,
    int drvKey)
  { 
    super(messageID, drvKey);
    this.nameSubscription = nameSubscription;
    this.theme = theme; 
  } 

  public NotificationUnsubscription(UnsubscriptionMessageMOMExtern msgUnsub)
  {
    super(msgUnsub.getMessageMOMExternID(), msgUnsub.getDriverKey());
    this.nameSubscription = msgUnsub.nameSubscription;
    this.theme = msgUnsub.topic.getTheme();
    this.sessionID = msgUnsub.sessionID;
  }
 
}
