/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2010 ScalAgent Distributed Technologies
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

import fr.dyade.aaa.common.stream.StreamUtil;

/**
 * A <code>SessCreateDestRequest</code> is sent by a <code>Session</code>
 * for creating a destination.
 */
public final class SessCreateDestRequest extends AbstractJmsRequest {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  /** Destination type, Queue or Topic, temporary or not. */
  private byte type;
  
  /**
   * Returns the destination type, Queue or Topic, temporary or not.
   * 
   * @return  the destination type, Queue or Topic, temporary or not.
   */
  public byte getType() {
    return type;
  }
  
  /** Name of the destination if any */
  private String name;
  
  /**
   * Returns the destination name if any.
   * 
   * @return  the destination name if any, null if not set.
   */
  public String getName() {
    return name;
  }

  /** Constructs a <code>SessCreateTQRequest</code> instance. */
  public SessCreateDestRequest(byte type) {
    this.type = type;
    this.name = null;
  }

  /** Constructs a <code>SessCreateTQRequest</code> instance. */
  public SessCreateDestRequest(byte type, String name) {
    this.type = type;
    this.name = name;
  }
  
  /**
   * @see org.objectweb.joram.shared.client.AbstractJmsMessage#getClassId()
   */
  protected int getClassId() {
    return SESS_CREATE_DEST_REQUEST;
  }

  /** Constructs a <code>SessCreateTQRequest</code> instance. */
  public SessCreateDestRequest() {
    super(null);
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
    StreamUtil.writeTo(name, os);
  }

  /**
   *  The object implements the readFrom method to restore its contents from
   * the input stream.
   *
   * @param is the stream to read data from in order to restore the object
   */
  public void readFrom(InputStream is) throws IOException {
    super.readFrom(is);
    type = StreamUtil.readByteFrom(is);
    name = StreamUtil.readStringFrom(is);
  }
}
