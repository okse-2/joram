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
 * An <code>AdminReply</code> is used by a destination agent for replying to
 * a client administration request.
 */
public class AdminReply extends AbstractNotification
{
  /** Field identifying the original request. */
  private String requestId;
  /** <code>true</code> if the request succeeded. */
  private boolean success;
  /** Info related to the processing of the request. */
  private String info;


  /**
   * Constructs an <code>AdminReply</code>.
   */
  public AdminReply(AdminRequest request, boolean success, String info)
  {
    requestId = request.getId();
    this.success = success;
    this.info = info;
  }


  /** Returns the request identifier. */
  public String getRequestId()
  {
    return requestId;
  }

  /** Returns <code>true</code> if the request was successful. */
  public boolean getSuccess()
  {
    return success;
  }

  /** Returns the info related to the processing of the request. */
  public String getInfo()
  {
    return info;
  }
}
