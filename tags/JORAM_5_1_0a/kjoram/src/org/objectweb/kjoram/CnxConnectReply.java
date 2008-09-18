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
 * A <code>CnxConnectReply</code> is sent by a JMS proxy as a reply to a
 * connection <code>CnxConnectRequest</code> and holds the connection's key
 * and the proxy identifier.
 */
public final class CnxConnectReply extends AbstractReply {
  /**
   * Constructs a <code>CnxConnectReply</code>.
   */
  public CnxConnectReply() {}

  /** The connection's key. */
  private int cnxKey;
 
  /** Returns the connection's key. */
  public int getCnxKey() {
    return cnxKey;
  }

  /** The proxy's identifier. */
  private String proxyId;

  /** Returns the proxy's identifier */
  public String getProxyId() {
    return proxyId;
  } 

  protected int getClassId() {
    return CNX_CONNECT_REPLY;
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
    cnxKey = is.readInt();
    proxyId = is.readString();
  }
}
