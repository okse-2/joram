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
 * A <code>SetFather</code> instance is used for linking two topics in a
 * hierarchical relationship.
 */
public class SetFather extends AdminRequest
{
  /** Identifier of the father. */
  private String father;
  /** Identifier of the son. */
  private String son;

  /**
   * Constructs a <code>SetFather</code> instance.
   *
   * @param father  Identifier of the father.
   * @param son  Identifier of the son.
   */
  public SetFather(String father, String son)
  {
    this.father = father;
    this.son = son;
  }

  /** Returns the identifier of the father. */
  public String getFather()
  {
    return father;
  }

  /** Returns the identifier of the son. */
  public String getSon()
  {
    return son;
  }
}
