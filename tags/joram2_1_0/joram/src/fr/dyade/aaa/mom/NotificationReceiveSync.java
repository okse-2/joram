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
import fr.dyade.aaa.agent.*; 
 
/** 
 * A <code>NotificationReceiveSync</code> wrapps an <code>AgentClient</code> 
 * request to receive a message from a <code>Queue</code> synchronously. 
 *
 * @see fr.dyade.aaa.mom.Queue 
 * @see fr.dyade.aaa.mom.AgentClient 
 */  
public class NotificationReceiveSync 
  extends fr.dyade.aaa.mom.NotificationMOMRequest
{ 
  /**
   * The availability time of the request. 
   * A negative value means no time constraint.
   * 0 value means nowait delivery. 
   * A positive value is the request availibility limit in milliseconds.
   */ 
  public long timeOut;  

  /** The request selector. */ 
  public String selector;

  /** The Session identifier. */  
  public String sessionID;

  public boolean toListener;

  /**
   * Constructor.
   * 
   * @param driverKey  key identifying the connection which received the
   * request.
   */
  public NotificationReceiveSync(long messageID, long timeOut,
    String selector, String sessionID, int driverKey)
  { 
    super(messageID, driverKey);
    this.timeOut = timeOut; 
    this.selector = selector;
    this.sessionID = sessionID;
  }


  public String toString()
  {
    return "timeOut=" + timeOut +
    " selector=" + selector + 
    " sessionID=" + sessionID +
    " driverKey=" + driverKey;
  }

}
