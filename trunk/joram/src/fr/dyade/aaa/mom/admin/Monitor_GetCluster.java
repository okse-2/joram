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

/**
 * A <code>Monitor_GetCluster</code> instance requests the list of the topics
 * part of a cluster.
 */
public class Monitor_GetCluster extends Monitor_Request
{
  /** Identifier of a topic part of the target cluster. */
  private String topic;

  
  /**
   * Constructs a <code>Monitor_GetCluster</code> instance.
   *
   * @param topic  Identifier of a topic part of the target cluster.
   */
  public Monitor_GetCluster(String topic)
  {
    this.topic = topic;
  }


  /** Returns the identifier of a topic part of the target cluster. */
  public String getTopic()
  {
    return topic;
  }
}
