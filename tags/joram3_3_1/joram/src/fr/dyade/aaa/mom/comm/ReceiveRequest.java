/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
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
 * fr.dyade.aaa.ip, fr.dyade.aaa.joram, fr.dyade.aaa.mom, and
 * fr.dyade.aaa.util, released May 24, 2000.
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
 *
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s):
 */
package fr.dyade.aaa.mom.comm;

/**
 * A <code>ReceiveRequest</code> instance is used by a <b>client</b> agent for 
 * requesting a message on a queue.
 */
public class ReceiveRequest extends AbstractRequest
{
  /**
   * String selector for filtering messages, null or empty for no selection.
   */
  private String selector;
  /**
   * The time-to-live value in milliseconds, during which a receive request
   * is valid.
   */
  private long timeOut;
  /** The expiration time of the request. */
  private long expirationTime;
  /**
   * If <code>true</code>, the consumed message will be immediately
   * deleted on the queue.
   */
  private boolean autoAck;
  /**
   * Identifier of the client requesting a message, set by the queue if
   * storing the request.
   */
  public fr.dyade.aaa.agent.AgentId requester;


  /**
   * Constructs a <code>ReceiveRequest</code> instance involved in an
   * external client - MOM interaction.
   *
   * @param key  See superclass.
   * @param requestId  See superclass.
   * @param selector  Selector expression for filtering messages, null or empty
   *          for no selection.
   * @param timeOut  Time-to-live value. For immediate delivery, should be set
   *          to 0. For infinite time-to-live, should be negative.
   * @param autoAck  <code>true</code> for immediately acknowledging the
   *          delivered message on the queue, <code>false</code> otherwise.
   */
  public ReceiveRequest(int key, String requestId, String selector,
                        long timeOut, boolean autoAck) 
  {
    super(key, requestId);
    this.selector = selector;
    this.timeOut = timeOut;
    this.autoAck = autoAck;
  }

  /**
   * Constructs a <code>ReceiveRequest</code> instance not involved in an
   * external client - MOM interaction.
   *
   * @param requestId  See superclass.
   * @param selector  Selector expression for filtering messages, null or empty
   *          for no selection.
   * @param timeOut  Time-to-live value. For immediate delivery, should be set
   *          to 0. For infinite time-to-live, should be negative.
   * @param autoAck  <code>true</code> for immediately acknowledging the
   *          delivered message on the queue, <code>false</code> otherwise.
   */
  public ReceiveRequest(String requestId, String selector, long timeOut,
                        boolean autoAck) 
  {
    this(0, requestId, selector, timeOut, autoAck);
  }


  /** Returns the selector of the request. */
  public String getSelector()
  {
    return selector;
  }

  /**
   * Returns the time-to-live parameter of this request, in milliseconds (0 for
   * immediate delivery, negative for infinite validity).
   */
  public long getTimeOut()
  {
    return timeOut;
  }

  /** Checks the autoAck mode of this request. */
  public boolean getAutoAck()
  {
    return autoAck;
  }

  /** Updates the expiration time field, if needed. */
  public void setExpiration(long currentTime)
  {
    if (timeOut > 0)
      this.expirationTime = currentTime + timeOut;
  }

  /** Returns <code>false</code> if the request expired. */
  public boolean isValid()
  {
    if (timeOut > 0)
      return System.currentTimeMillis() < expirationTime;
    return true;
  }
} 
