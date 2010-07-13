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

import fr.dyade.aaa.common.stream.StreamUtil;

/**
 * A <code>QBrowseRequest</code> instance is sent by a 
 * <code>QueueBrowser</code> when requesting an enumeration.
 */
public final class QBrowseRequest extends AbstractJmsRequest {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  /** The selector for filtering messages. */
  private String selector;

  /** Sets the selector. */
  public void setSelector(String selector) {
    this.selector = selector;
  }

  /** Returns the selector for filtering the messages. */
  public String getSelector() {
    return selector;
  }

  protected int getClassId() {
    return QBROWSE_REQUEST;
  }

  /**
   * Constructs a <code>QBrowseRequest</code> instance.
   *
   * @param to  Name of the queue to browse. 
   * @param selector  The selector for filtering messages, if any.
   */
  public QBrowseRequest(String to, String selector) {
    super(to);
    this.selector = selector;
  }

  /**
   * Constructs a <code>QBrowseRequest</code> instance.
   */
  public QBrowseRequest() {}

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
    StreamUtil.writeTo(selector, os);
  }

  /**
   *  The object implements the readFrom method to restore its contents from
   * the input stream.
   *
   * @param is the stream to read data from in order to restore the object
   */
  public void readFrom(InputStream is) throws IOException {
    super.readFrom(is);
    selector = StreamUtil.readStringFrom(is);
  }
}
