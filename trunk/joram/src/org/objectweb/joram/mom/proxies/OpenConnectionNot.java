/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
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
 * Contributor(s): David Feliot (ScalAgent DT)
 */
package org.objectweb.joram.mom.proxies;

import fr.dyade.aaa.agent.*;

/**
 * Notification to open a user connection. 
 * It is sent by the <code>ConnectionManager</code>
 * and received by a <code>UserAgent</code>.
 * 
 * @see ConnectionManager
 * @see UserAgent
 */
public class OpenConnectionNot extends Notification {

  /**
   * The connection to open. It is transient because
   * the data contained by a connection are not
   * serializable.
   */
  private transient UserConnection uc;

  /**
   * Exception that may be raised during
   * the connection opening.
   */
  private transient Exception exception;

  public OpenConnectionNot(UserConnection uc) {
    this.uc = uc;
    this.exception = null;
  }

  /**
   * Returns the connection to open. May be null.
   */
  public final UserConnection getUserConnection() {
    return uc;
  }

  public final Exception getException() {
    return exception;
  }

  public void setException(Exception exception) {
    this.exception = exception;
  }
}
