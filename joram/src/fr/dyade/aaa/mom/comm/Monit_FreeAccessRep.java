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

import java.util.Vector;


/**
 * A <code>Monit_FreeAccessRep</code> reply is used by a destination for
 * sending to an administrator client its free access settings.
 */
public class Monit_FreeAccessRep extends AdminReply
{
  /** <code>true</code> if READ access is free. */
  private boolean freeReading;
  /** <code>true</code> if WRITE access is free. */
  private boolean freeWriting;


  /**
   * Constructs a <code>Monit_FreeAccessRep</code> instance.
   *
   * @param request  The request this reply replies to.
   * @param freeReading  <code>true</code> if READ access is free.
   * @param freeWriting  <code>true</code> if WRITE access is free.
   */
  public Monit_FreeAccessRep(AdminRequest request,
                             boolean freeReading,
                             boolean freeWriting)
  {
    super(request, true, null);
    this.freeReading = freeReading;
    this.freeWriting = freeWriting;
  }

  
  /** Returns <code>true</code> if READ access is free. */
  public boolean getFreeReading()
  {
    return freeReading;
  }

  /** Returns <code>true</code> if WRITE access is free. */
  public boolean getFreeWriting()
  {
    return freeWriting;
  }
}
