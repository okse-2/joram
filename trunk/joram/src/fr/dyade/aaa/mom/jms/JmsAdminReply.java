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
package fr.dyade.aaa.mom.jms;

/**
 * A <code>JmsAdminReply</code> is used by an admin proxy for replying to a
 * to an administrator <code>JmsAdminRequest</code>.
 */
public class JmsAdminReply extends AbstractJmsReply
{
  /**
   * <code>true</code> is the request succeeded, <code>false</code> otherwise.
   */
  private boolean success;
  /** String information destinated to the administrator. */
  private String info;

  /**
   * Constructs a <code>JmsAdminReply</code> instance.
   * 
   * @param request The request being answered.
   * @param success  <code>true</code> if the request was successfull.
   * @param info  Information to send to the administrator.
   */
  public JmsAdminReply(JmsAdminRequest request, boolean success, String info)
  {
    super(request.getRequestId());
    this.success = success;
    this.info = info;
  }


  /** Returns <code>true</code> if the admin request was successfull. */
  public boolean succeeded()
  {
    return success;
  }

  /** Returns the information carried by this reply. */
  public String getInfo()
  {
    return info;
  }

  public String toString() {
    return "JmsAdminReply[success="+ success +", info= "+ info +"]";
  }
}
