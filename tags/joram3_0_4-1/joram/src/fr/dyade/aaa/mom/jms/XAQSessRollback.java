/*
 * Copyright (C) 2002 - ScalAgent Distributed Technologies
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
 * The present code contributor is ScalAgent Distributed Technologies.
 */
package fr.dyade.aaa.mom.jms;

import java.util.*;

/**
 * An <code>XAQSessRollback</code> instance is used by an
 * <code>XAQueueSession</code> for rolling back the operations performed
 * during a transaction.
 */
public class XAQSessRollback extends XASessRollback
{
  /**
   * The table holding the identifiers of the messages to deny.
   * <br>
   * <b>Key:</b> destination name<br>
   * <b>Object:</b> vector of messages identifiers
   */
  private Hashtable denyings;

  /**
   * Constructs an <code>XAQSessRollback</code> instance.
   *
   * @param id  Identifier of the resource and the rolling back transaction.
   */
  public XAQSessRollback(String id)
  {
    super(id);
    denyings = new Hashtable();
  }

  /**
   * Adds a vector of message identifiers in the table.
   *
   * @param dest  Name of the destination where denying the messages.
   * @param ids  Vector of message identifiers.
   */
  public void add(String dest, Vector ids)
  {
    denyings.put(dest, ids);
  }

  /** Returns <code>true</code> if the request still contains data. */
  public boolean hasMoreDests()
  {
    return (denyings.keys()).hasMoreElements();
  }

  /** Returns the next destination name. */
  public String nextDest()
  {
    return (String) (denyings.keys()).nextElement();
  }
  
  /** Returns the vector of msg identifiers for a given destination. */
  public Vector getIds(String dest)
  {
    return (Vector) denyings.remove(dest);
  }
}
