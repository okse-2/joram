/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2011 ScalAgent Distributed Technologies
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
package org.ow2.joram.mom.amqp.exceptions;

import org.ow2.joram.mom.amqp.marshalling.AMQP;

/**
 * Exception thrown if the peer sent a frame that was not expected, usually in
 * the context of a content header and body. This strongly indicates a fault in
 * the peer's content processing.
 */
public class UnexpectedFrameException extends ConnectionException {

  private static final long serialVersionUID = 1L;

  public UnexpectedFrameException(String message) {
    super(message);
  }

  public int getCode() {
    return AMQP.UNEXPECTED_FRAME;
  }

}
