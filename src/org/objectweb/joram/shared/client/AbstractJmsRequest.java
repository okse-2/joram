/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2008 ScalAgent Distributed Technologies
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
package org.objectweb.joram.shared.client;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

import org.objectweb.joram.shared.stream.StreamUtil;

/**
 * An <code>AbstractJmsRequest</code> is a request sent by a Joram client
 * to its proxy.
 */
public abstract class AbstractJmsRequest extends AbstractJmsMessage {
  /** 
   * Identifier of the request. 
   * Declared volatile to allow a thread that is not the thread sending the
   * request to get the identifier in order to cancel it during a close.
   */
  protected volatile int requestId = -1;

  /** 
   * Sets the request identifier. 
   */
  public final void setRequestId(int requestId) {
    this.requestId = requestId;
  }
  
  /** Returns the request identifier. */
  public final synchronized int getRequestId() {
    return requestId;
  }

  /**
   * The request target is either a destination agent name, or a subscription 
   * name.
   */
  protected String target = null;

   /** Sets the request target name. */
  public final void setTarget(String target) {
    this.target = target;
  }

  /** Returns the request target name.  */
  public final String getTarget() {
    return target;
  }

  /**
   * Constructs an <code>AbstractJmsRequest</code>.
   */
  public AbstractJmsRequest() {}

  /**
   * Constructs an <code>AbstractJmsRequest</code>.
   *
   * @param target  String identifier of the request target, either a queue
   *          name, or a subscription name.
   */
  public AbstractJmsRequest(String target) {
    this.target = target;
  }

  public final String toString() {
    StringBuffer strbuf = new StringBuffer();
    toString(strbuf);
    return strbuf.toString();
  }

  public void toString(StringBuffer strbuf) {
    strbuf.append('(').append(super.toString());
    strbuf.append(",requestId=").append(requestId);
    strbuf.append(",target=").append(target);
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
    StreamUtil.writeTo(requestId, os);
    StreamUtil.writeTo(target, os);
  }

  /**
   *  The object implements the readFrom method to restore its contents from
   * the input stream.
   *
   * @param is the stream to read data from in order to restore the object
   */
  public void readFrom(InputStream is) throws IOException {
    requestId = StreamUtil.readIntFrom(is);
    target = StreamUtil.readStringFrom(is);
  }
}
