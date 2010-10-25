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
 * A <code>CnxConnectReply</code> is sent by a JMS proxy as a reply to a
 * connection <code>CnxConnectRequest</code> and holds the connection's key
 * and the proxy identifier.
 */
public final class CnxConnectReply extends AbstractJmsReply {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  /** The connection's key. */
  private int cnxKey;

   /** Sets the connection key. */
  public void setCnxKey(int cnxKey) {
    this.cnxKey = cnxKey;
  }
 
  /** Returns the connection's key. */
  public int getCnxKey() {
    return cnxKey;
  }

  /** The proxy's identifier. */
  private String proxyId;

  /** Sets the proxy's identifier */
  public void setProxyId(String proxyId) {
    this.proxyId = proxyId;
  } 

  /** Returns the proxy's identifier */
  public String getProxyId() {
    return proxyId;
  } 

  protected int getClassId() {
    return CNX_CONNECT_REPLY;
  }

  /**
   * Constructs a <code>CnxConnectReply</code>.
   *
   * @param req  The replied request.
   * @param cnxKey  The connection's key.
   * @param proxyId  The proxy's identifier.
   */
  public CnxConnectReply(CnxConnectRequest req, int cnxKey, String  proxyId) {
    super(req.getRequestId());
    this.cnxKey = cnxKey;
    this.proxyId = proxyId;
  }

  /**
   * Constructs a <code>CnxConnectReply</code>.
   */
  public CnxConnectReply() {}

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
    StreamUtil.writeTo(cnxKey, os);
    StreamUtil.writeTo(proxyId, os);
  }

  /**
   *  The object implements the readFrom method to restore its contents from
   * the input stream.
   *
   * @param is the stream to read data from in order to restore the object
   */
  public void readFrom(InputStream is) throws IOException {
    super.readFrom(is);
    cnxKey = StreamUtil.readIntFrom(is);
    proxyId = StreamUtil.readStringFrom(is);
  }
}
