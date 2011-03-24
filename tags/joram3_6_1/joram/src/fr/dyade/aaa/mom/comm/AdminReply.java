/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - ScalAgent Distributed Technologies
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA.
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