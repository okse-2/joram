/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2004 ScalAgent Distributed Technologies
 * Copyright (C) 2004 - France Telecom R&D
 * Copyright (C) 1996 - 2000 Dyade
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
 * Contributor(s): ScalAgent Distributed Technologies
 */
package org.objectweb.joram.shared.admin;

/**
 * An <code>AdminReply</code> is a reply sent by a
 * <code>org.objectweb.joram.mom.dest.AdminTopic</code> topic and containing data or
 * information destinated to a client administrator.
 */
public class AdminReply implements java.io.Serializable
{
  /** <code>true</code> if this reply replies to a successful request. */
  private boolean success = false;
  /** Information. */
  private String info;
  /** Object. */
  private Object replyObj;

  /**
   * Constructs an <code>AdminReply</code> instance.
   *
   * @param success  <code>true</code> if this reply replies to a successful
   *          request.
   * @param info  Information to carry.
   */
  public AdminReply(boolean success, 
                    String info) {
    this(success,info,null);
  }

  /**
   * Constructs an <code>AdminReply</code> instance.
   *
   * @param success  <code>true</code> if this reply replies to a successful
   *          request.
   * @param info  Information to carry.
   * @param replyObj Object to carry.
   */
  public AdminReply(boolean success, 
                    String info,
                    Object replyObj)
  {
    this.success = success;
    this.info = info;
    this.replyObj = replyObj;
  }

  /**
   * Returns <code>true</code> if this reply replies to a successful request.
   */
  public boolean succeeded()
  {
    return success;
  }

  /** Returns the carried info. */
  public String getInfo()
  {
    return info;
  }

  /** Returns the carried object. */
  public Object getReplyObject() {
    return replyObj;
  }

  public String toString() {
    return '(' + super.toString() + 
      ",success=" + success +
      ",info=" + info + 
      ",replyObj=" + replyObj + ')';
  }
}
