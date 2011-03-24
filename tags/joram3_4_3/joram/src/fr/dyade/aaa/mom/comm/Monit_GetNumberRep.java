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
package fr.dyade.aaa.mom.comm;


/**
 * A <code>Monit_GetNumberRep</code> reply is used by a destination for
 * sending to an administrator client an integer value.
 */
public class Monit_GetNumberRep extends AdminReply
{
  /** The wrapped value. */
  private int number;


  /**
   * Constructs a <code>Monit_GetNumberRep</code>.
   *
   * @param request  The request this reply replies to.
   * @param number  The value to wrap.
   */
  public Monit_GetNumberRep(AdminRequest request, int number)
  {
    super(request, true, null);
    this.number = number;
  }

  
  /** Returns the wrapped number. */
  public int getNumber()
  {
    return number;
  }
}