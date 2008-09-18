/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 ScalAgent Distributed Technologies
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
 * Initial developer(s): ScalAgent Distributed Technologies
 * Contributor(s): 
 */
package org.objectweb.kjoram;

/**
 *  An exception listener allows a client to be notified of a problem
 * asynchronously. If the Joram server detects a serious problem during
 * a connection, it informs the client through the ExceptionListener if it
 * has been registered. It does this by calling the listener's onException
 * method, passing it an exception describing the problem.
 */
interface ExceptionListener {
  /**
   * Notifies the user of an asynchronous error during connection.
   *
   * @param exc	An exception describing the issue.
   */
  void onException(JoramException exc);
}
