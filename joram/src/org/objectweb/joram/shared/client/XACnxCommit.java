/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - Bull SA
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
 * Contributor(s):
 */
package org.objectweb.joram.shared.client;

import java.util.*;

/**
 * An <code>XACnxCommit</code> instance is used by an <code>XAConnection</code>
 * for commiting the messages and acknowledgements it sent to the proxy.
 */
public class XACnxCommit extends AbstractJmsRequest
{
  /** Transaction branch qualifier. */
  private byte[] bq;
  /** Transaction identifier format. */
  private int fi;
  /** Global transaction identifier. */
  private byte[] gti;


  /**
   * Constructs an <code>XACnxCommit</code> instance.
   *
   * @param bq        Transaction branch qualifier.
   * @param fi        Transaction identifier format.
   * @param gti       Global transaction identifier.
   */
  public XACnxCommit(byte[] bq, int fi, byte[] gti)
  {
    super();
    this.bq = bq;
    this.fi = fi;
    this.gti = gti;
  }

  /**
   * Constructs an <code>XACnxCommit</code> instance.
   */
  public XACnxCommit()
  {}


  /** Returns the transaction branch qualifier. */
  public byte[] getBQ()
  {
    return bq;
  }

  /** Returns the transaction identifier format. */
  public int getFI()
  {
    return fi;
  }

  /** Returns the global transaction identifier. */
  public byte[] getGTI()
  {
    return gti;
  }

  public void setBQ(byte[] bq)
  {
    this.bq = bq;
  }

  public void setFI(int fi)
  {
    this.fi = fi;
  }

  public void setGTI(byte[] gti)
  {
    this.gti = gti;
  }

  public Hashtable soapCode() {
    Hashtable h = super.soapCode();
    h.put("bq",bq);
    h.put("fi", new Integer(fi));
    h.put("gti", gti);
    return h;
  }

  public static Object soapDecode(Hashtable h) {
    XACnxCommit req = new XACnxCommit();
    req.setRequestId(((Integer) h.get("requestId")).intValue());
    req.setTarget((String) h.get("target"));
    req.setBQ((byte[]) h.get("bq"));
    req.setFI(((Integer) h.get("fi")).intValue());
    req.setGTI((byte[]) h.get("gti"));
    return req;
  }
}
