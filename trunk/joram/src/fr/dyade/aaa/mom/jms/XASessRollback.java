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
package fr.dyade.aaa.mom.jms;

import java.util.*;

/**
 * An <code>XASessRollback</code> instance is used by an <code>XASession</code>
 * for rolling back the operations performed during a transaction.
 */
public class XASessRollback extends AbstractJmsRequest
{
  /** Identifier of the resource and the rolling back transaction. */
  private String id;
  /**
   * Table holding the identifiers of the messages to deny on queues.
   * <p>
   * <b>Key: </b> queue name<br>
   * <b>Object: </b> vector of messages identifiers
   */
  private Hashtable qDenyings = null;
  /**
   * Table holding the identifiers of the messages to deny on subscriptions.
   * <p>
   * <b>Key: </b> subscription name<br>
   * <b>Object: </b> vector of messages identifiers
   */
  private Hashtable subDenyings = null;


  /**
   * Constructs an <code>XASessRollback</code> instance.
   *
   * @param id  Identifier of the resource and the rolling back transaction.
   */
  public XASessRollback(String id)
  {
    super(null);
    this.id = id;
  }

  /**
   * Constructs an <code>XASessRollback</code> instance.
   */
  public XASessRollback()
  {}

 
  /** Sets the identifier. */
  public void setId(String id)
  {
    this.id = id;
  }
 
  /** Returns the id of the resource and the rolling back transaction. */
  public String getId()
  {
    return id;
  }

  /**
   * Adds a vector of message identifiers in one of the tables.
   *
   * @param target  Name of the queue or of the subscription where denying the
   *          messages.
   * @param ids  Vector of message identifiers.
   * @param queueMode  <code>true</code> if the messages are to be denied on a
   *          queue.
   */
  public void add(String target, Vector ids, boolean queueMode)
  {
    if (queueMode) {
      if (qDenyings == null)
        qDenyings = new Hashtable();
      qDenyings.put(target, ids);
    }
    else {
      if (subDenyings == null)
        subDenyings = new Hashtable();
      subDenyings.put(target, ids);
    }
  }

  /**
   * Returns <code>true</code> if the request still contains data for queues.
   */
  public boolean hasMoreQueues()
  {
    if (qDenyings == null)
      return false;
    return (qDenyings.keys()).hasMoreElements();
  }

  /** Returns the next queue name. */
  public String nextQueue()
  {
    if (qDenyings == null)
      return null;
    return (String) (qDenyings.keys()).nextElement();
  }
  
  /** Returns the vector of msg identifiers for a given queue. */
  public Vector getQueueIds(String queue)
  {
    if (qDenyings == null)
      return null;
    return (Vector) qDenyings.remove(queue);
  }

  /** Returns <code>true</code> if the request still contains data for subs. */
  public boolean hasMoreSubs()
  {
    if (subDenyings == null)
      return false;
    return (subDenyings.keys()).hasMoreElements();
  }

  /** Returns the next subscription name. */
  public String nextSub()
  {
    if (subDenyings == null)
      return null;
    return (String) (subDenyings.keys()).nextElement();
  }
  
  /** Returns the vector of msg identifiers for a given subscription. */
  public Vector getSubIds(String sub)
  {
    if (subDenyings == null)
      return null;
    return (Vector) subDenyings.remove(sub);
  }

  /** Sets the queue denyings table. */
  public void setQDenyings(Hashtable qDenyings)
  {
    this.qDenyings = qDenyings;
  }

  /** Sets the sub denyings table. */
  public void setSubDenyings(Hashtable subDenyings)
  {
    this.subDenyings = subDenyings;
  }

  /** Returns the queue denyings table. */
  public Hashtable getQDenyings()
  {
    return qDenyings;
  }
 
  /** Returns the sub denyings table. */
  public Hashtable getSubDenyings()
  {
    return subDenyings;
  }
}
