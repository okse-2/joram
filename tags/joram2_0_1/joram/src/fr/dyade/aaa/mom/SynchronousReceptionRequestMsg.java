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
 * A <code>SynchronousReceptionRequestMsg</code> wraps a synchronous
 * reception request.
 *
 * @author  Frederic Maistre
 */ 
public class SynchronousReceptionRequestMsg extends MessageMOMExtern
{ 
  /** Validity time of the request (ms). */
  long timeOut;

  /**
   * If this request is adressed to a <code>ClientSubscription</code>,
   * the name of the subscription.
   */
  String subscriptionName;

  /**
   * If this request is adressed to a <code>Queue</code>, the name
   * of the queue.
   */
  QueueNaming queue;
  /** Selector (queue case only). */
  String selector;  
  /** Requesting session ID (queue case only). */
  String sessionID;
  /** Receiver type (queue case only). */
  boolean toListener = false;

  /**
   * Constructor.
   *
   * @param requestID  Request identifier.
   * @param timeOut  Time-to-live attribute of the request in milliseconds.
   */
  public SynchronousReceptionRequestMsg(long requestID, long timeOut)
  { 
    super(requestID);
    this.timeOut = timeOut;
  }


  /** Method setting the subscriptionName parameter. */
  public void setSubName(String subName)
  {
    this.subscriptionName = subName;
  }

  /** Method setting the queue parameter. */
  public void setQueue(QueueNaming queue)
  {
    this.queue = queue;
  }

  /** Method setting the selector parameter. */
  public void setSelector(String selector)
  {
    this.selector = selector;
  }

  /** Method setting the sessionID parameter. */
  public void setSessionID(String sessionID)
  {
    this.sessionID = sessionID;
  }

  /** Method setting the toListener parameter. */
  public void setToListener(boolean toListener)
  {
    this.toListener = toListener;
  }

}
