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

import java.io.IOException;

/**
 * A <code>MomExceptionReply</code> instance is used by the proxy
 * to send a <code>MomException</code> back to the client.
 */
public final class MomExceptionReply extends AbstractReply {
  /**
   * Public no-arg constructor needed by Externalizable.
   */
  public MomExceptionReply() {}

  protected int getClassId() {
    return MOM_EXCEPTION_REPLY;
  }

  public static final int MomException = 1;
  public static final int AccessException = 2;
  public static final int DestinationException = 3;
  public static final int MessageException = 4;
  public static final int MessageROException = 5;
  public static final int MessageValueException = 6;
  public static final int RequestException = 7;
  public static final int SelectorException = 8;
  public static final int StateException = 9;

  /** The wrapped exception type. */
  private int type;

  /** Returns the exception wrapped by this reply. */
  public int getType() {
    return type;
  }

  /** The wrapped exception message. */
  private String message;

  public String getMessage() {
    return message;
  }

  /* ***** ***** ***** ***** *****
   * Streamable interface
   * ***** ***** ***** ***** ***** */

  /**
   *  The object implements the readFrom method to restore its contents from
   * the input stream.
   *
   * @param is the stream to read data from in order to restore the object
   */
  public void readFrom(InputXStream is) throws IOException {
    super.readFrom(is);
    type = is.readInt();
    message = is.readString();
  }
}
