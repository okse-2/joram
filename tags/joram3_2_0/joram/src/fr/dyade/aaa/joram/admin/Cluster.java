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
package fr.dyade.aaa.joram.admin;

import java.util.Vector;

/**
 * The <code>Cluster</code> class is used by administrators for creating 
 * clusters of topics.
 */
public class Cluster
{
  /** Cluster name. */
  private String name;
  /** Vector of topic names. */
  private Vector topics;

  /** <code>true</code> when the cluster object has been sent to the MOM. */
  boolean locked;


  /**
   * Constructs a <code>Cluster</code> instance.
   *
   * @param name  String name of the cluster.
   */
  public Cluster(String name)
  {
    this.name = name;
    topics = new Vector();
    locked = false;
  }


  /**
   * Adds a topic to the cluster.
   *
   * @param topic  The <code>Topic</code> instance.
   * @exception AdminException  If the cluster has already been created.
   */
  public void addTopic(fr.dyade.aaa.joram.Topic topic) throws AdminException
  {
    if (locked)
      throw new AdminException("Forbidden method call as the cluster has been"
                               + " already created.");
        
    topics.add(topic.getName());
  }

  /** Returns the name of the cluster. */
  String getName()
  {
    return name;
  }

  /** Returns the vector of topic names. */
  Vector getTopics()
  {
    return topics;
  } 
}
