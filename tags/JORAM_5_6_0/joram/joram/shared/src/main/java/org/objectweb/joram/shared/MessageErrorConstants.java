/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 - 2011 ScalAgent Distributed Technologies
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
package org.objectweb.joram.shared;

/**
 * A collection of constants listing the different reasons why a message can be
 * sent to a DMQ.
 */
public interface MessageErrorConstants {
  
  /**
   * If the message expired before delivery.
   **/
  public static final short EXPIRED = 0;

  /**
   * If the target destination of the message did not accept the sender as a
   * WRITER.
   **/
  public static final short NOT_WRITEABLE = 1;

  /**
   * If the number of delivery attempts of the message overtook the threshold.
   **/
  public static final short UNDELIVERABLE = 2;

  /**
   * If the message has been deleted by an admin request.
   */
  public static final short ADMIN_DELETED = 3;

  /**
   * If the target destination of the message could not be found.
   */
  public static final short DELETED_DEST = 4;

  /**
   * If the queue has reached its max number of messages.
   */
  public static final short QUEUE_FULL = 5;

  /**
   * If an unexpected error happened during delivery.
   */
  public static final short UNEXPECTED_ERROR = 6;
  
  /**
   * If an interceptors error happened.
   */
  public static final short INTERCEPTORS = 7;
  
  /**
   * If the client tried to work with some entity in a manner that is prohibited
   * by the server
   */
  public static final short NOT_ALLOWED = 8;

}
