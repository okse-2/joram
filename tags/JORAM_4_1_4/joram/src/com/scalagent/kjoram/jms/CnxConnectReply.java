/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
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
 * Contributor(s): Nicolas Tachker (ScalAgent)
 */
package com.scalagent.kjoram.jms;

import java.util.Hashtable;
import java.util.Enumeration;

/**
 * A <code>CnxConnectReply</code> is sent by a JMS proxy as a reply to a
 * connection <code>CnxConnectRequest</code> and holds the connection's key
 * and the proxy identifier.
 */
public class CnxConnectReply extends AbstractJmsReply
{
  /** The connection's key. */
  private int cnxKey;
  /** The proxy's identifier. */
  private String proxyId;

  /**
   * Constructs a <code>CnxConnectReply</code>.
   *
   * @param req  The replied request.
   * @param cnxKey  The connection's key.
   * @param proxyId  The proxy's identifier.
   */
  public CnxConnectReply(CnxConnectRequest req, int cnxKey, String  proxyId)
  {
    super(req.getRequestId());
    this.cnxKey = cnxKey;
    this.proxyId = proxyId;
  }

  /**
   * Constructs a <code>CnxConnectReply</code>.
   */
  public CnxConnectReply()
  {}

 
   /** Sets the connection key. */
  public void setCnxKey(int cnxKey)
  {
    this.cnxKey = cnxKey;
  }

  /** Sets the proxy's identifier */
  public void setProxyId(String proxyId)
  {
    this.proxyId = proxyId;
  } 
 
  /** Returns the connection's key. */
  public int getCnxKey()
  {
    return cnxKey;
  }

  /** Returns the proxy's identifier */
  public String getProxyId()
  {
    return proxyId;
  } 

  public Hashtable soapCode() {
    Hashtable h = super.soapCode();
    h.put("cnxKey",new Integer(cnxKey));
    h.put("proxyId",proxyId);
    return h;
  }

  public static Object soapDecode(Hashtable h) {
    CnxConnectReply req = new CnxConnectReply();
    req.setCorrelationId(((Integer) h.get("correlationId")).intValue());
    req.setCnxKey(((Integer) h.get("cnxKey")).intValue());
    req.setProxyId((String) h.get("proxyId"));
    return req;
  }
}
