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
package fr.dyade.aaa.mom.messages;

/** 
 * The <code>DestinationInfo</code> class is used to store a destination 
 * information carried by a <code>Message</code> instance.
 */
public class DestinationInfo implements java.io.Serializable
{
  /** Identifier of the destination. */
  private String name;
  /** <code>true</code> if the destination is a queue. */
  private boolean queue;
  /** <code>true</code> if the destination is temporary. */
  private boolean temporary;

  /**
   * Constructs a <code>DestinationInfo</code> instance.
   */
  public DestinationInfo(String name, boolean queue, boolean temporary)
  {
    this.name = name;
    this.queue = queue;
    this.temporary = temporary;
  }


  /** Returns the identifier of the destination. */
  public String getName()
  {
    return name;
  }

  /** Returns <code>true</code> if the destination is a queue. */
  public boolean isQueue()
  {
    return queue;
  }

  /** Returns <code>true</code> if the destination is temporary. */
  public boolean isTemporary()
  {
    return temporary;
  }
}
