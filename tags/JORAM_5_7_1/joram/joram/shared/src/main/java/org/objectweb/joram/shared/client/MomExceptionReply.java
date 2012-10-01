/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2011 ScalAgent Distributed Technologies
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
package org.objectweb.joram.shared.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.objectweb.joram.shared.excepts.MomException;

import fr.dyade.aaa.common.stream.StreamUtil;

/**
 * A <code>MomExceptionReply</code> instance is used by a JMS client proxy
 * to send a <code>MomException</code> back to a JMS client.
 */
public final class MomExceptionReply extends AbstractJmsReply {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  public static final int MomException = 1;
  public static final int AccessException = 2;
  public static final int DestinationException = 3;
  // public static final int MessageException = 4;
  // public static final int MessageROException = 5;
  public static final int MessageValueException = 6;
  public static final int RequestException = 7;
  public static final int SelectorException = 8;
  public static final int StateException = 9;

  // Only used MOM side.
  public static final int HBCloseConnection = 99999;

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

  protected int getClassId() {
    return MOM_EXCEPTION_REPLY;
  }

  /**
   * Constructs a <code>MomExceptionReply</code> instance.
   *
   * @param correlationId   Identifier of the failed request.
   * @param exc             The resulting exception.
   */
  public MomExceptionReply(int correlationId, MomException exc) {
    super(correlationId);
    this.type = exc.getType();
    this.message = exc.getMessage();
  }

  /**
   * Constructs a <code>MomExceptionReply</code> instance.
   *
   * @param exc The exception to wrap.
   */
  public MomExceptionReply(MomException exc) {
    this.type = exc.getType();
    this.message = exc.getMessage();
  }

  /**
   * Public no-arg constructor needed by Externalizable.
   */
  public MomExceptionReply() {}

  public void toString(StringBuffer strbuf) {
    super.toString(strbuf);
    strbuf.append(",momExceptType=").append(type);
    strbuf.append(",momExceptMessage=").append(message);
    strbuf.append(')');
  }

  /* ***** ***** ***** ***** *****
   * Streamable interface
   * ***** ***** ***** ***** ***** */

  /**
   *  The object implements the writeTo method to write its contents to
   * the output stream.
   *
   * @param os the stream to write the object to
   */
  public void writeTo(OutputStream os) throws IOException {
    super.writeTo(os);
    StreamUtil.writeTo(type, os);
    StreamUtil.writeTo(message, os);
  }

  /**
   *  The object implements the readFrom method to restore its contents from
   * the input stream.
   *
   * @param is the stream to read data from in order to restore the object
   */
  public void readFrom(InputStream is) throws IOException {
    super.readFrom(is);
    type = StreamUtil.readIntFrom(is);
    message = StreamUtil.readStringFrom(is);
  }
}
