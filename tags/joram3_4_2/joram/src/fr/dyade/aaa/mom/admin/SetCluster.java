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
 * Initial developer(s): Frederic Maistre
 * Contributor(s):
 */
package fr.dyade.aaa.mom.admin;

/** 
 * A <code>SetCluster</code> instance is used for adding a given topic
 * to a cluster an other topic is part of, or for creating a new cluster.
 */
public class SetCluster extends AdminRequest
{
  /**
   * Identifier of the topic already part of a cluster, or chosen as the
   * initiator.
   */
  private String initId;
  /** Identifier of the topic joining the cluster, or the initiator. */
  private String topId;

  /**
   * Constructs a <code>SetCluster</code> instance.
   *
   * @param initName  Identifier of the topic already part of a cluster, or
   *          chosen as the initiator.
   * @param topName  Identifier of the topic joining the cluster, or the
   *          initiator.
   */
  public SetCluster(String initId, String topId)
  {
    this.initId = initId;
    this.topId = topId;
  }

  /**
   * Returns the identifier of the topic already part of a cluster, or chosen
   * as the initiator.
   */
  public String getInitId()
  {
    return initId;
  }

  /**
   * Returns the identifier of the topic joining the cluster, or the
   * initiator.
   */
  public String getTopId()
  {
    return topId;
  }
}
