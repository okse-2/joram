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
package fr.dyade.aaa.mom.comm;

/**
 * An <code>AccessReply</code> is used by a <b>destination</b> agent for 
 * replying to an <code>AccessRequest</code> client request.
 */
public class AccessReply extends AbstractReply
{
  /** Right requested. */
  private int right;
  /** <code>true</code> if the requested right is granted. */
  private boolean granted;

  /**
   * Constructs an <code>AccessReply</code> instance.
   *
   * @param request  The replied request.
   * @param granted  <code>true</code> if the requested right is granted.
   */
  public AccessReply(AccessRequest request, boolean granted)
  {
    super(request.getConnectionKey(), request.getRequestId());
    this.right = request.getRight();
    this.granted = granted;
  }

  /** Returns the right requested. */
  public int getRight()
  {
    return right;
  }

  /** Returns <code>true</code> if the right is granted. */
  public boolean getGranted()
  {
    return granted;
  }
}
