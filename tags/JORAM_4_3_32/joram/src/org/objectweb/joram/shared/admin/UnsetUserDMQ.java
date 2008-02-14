/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2006 ScalAgent Distributed Technologies
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
 * An <code>UnsetUserDMQ</code> instance requests to unset the
 * DMQ of a given user.
 */
public class UnsetUserDMQ extends AdminRequest {
  private static final long serialVersionUID = -9080857791541217603L;

  /** Identifier of the user's proxy which DMQ is unset. */
  private String userProxId;

  /**
   * Constructs an <code>UnsetUserDMQ</code> instance.
   *
   * @param userProxId  Identifier of the user's proxy which DMQ is unset.
   */
  public UnsetUserDMQ(String userProxId) {
    this.userProxId = userProxId;
  }

  /** Returns the identifier of the user's proxy which DMQ is unset. */
  public String getUserProxId() {
    return userProxId;
  }
}
