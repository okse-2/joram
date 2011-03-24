/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - ScalAgent Distributed Technologies
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
package fr.dyade.aaa.mom.admin;

import java.util.Vector;


/**
 * A <code>Monitor_GetDestinationsRep</code> instance replies to a get
 * destinations monitoring request, and holds the destinations on a given
 * server.
 */
public class Monitor_GetDestinationsRep extends Monitor_Reply
{
  /** Queues identifiers. */
  private Vector queues;
  /** Dead message queues identifiers. */
  private Vector dmqs;
  /** Topics identifiers. */
  private Vector topics;



  /** Adds a queue identifier to the reply. */
  public void addQueue(String id)
  {
    if (queues == null)
      queues = new Vector();
    queues.add(id);
  }

  /** Adds a dead message queue identifier to the reply. */
  public void addDeadMQueue(String id)
  {
    if (dmqs == null)
      dmqs = new Vector();
    dmqs.add(id);
  }

  /** Adds a topic identifier to the reply. */
  public void addTopic(String id)
  {
    if (topics == null)
      topics = new Vector();
    topics.add(id);
  }

  
  /** Returns the queues identifiers. */
  public Vector getQueues()
  {
    return queues;
  }

  /** Returns the dmqs identifiers. */
  public Vector getDeadMQueues()
  {
    return dmqs;
  } 

  /** Returns the topics identifiers. */
  public Vector getTopics()
  {
    return topics;
  }
}