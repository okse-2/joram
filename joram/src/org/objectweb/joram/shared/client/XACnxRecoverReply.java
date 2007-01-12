/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - Bull SA
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
 * Initial developer(s): Frederic Maistre (Bull SA)
 * Contributor(s):
 */
package org.objectweb.joram.shared.client;

import java.util.Hashtable;
import java.util.Vector;


/**
 * A <code>XACnxRecoverReply</code> replies to a
 * <code>XACnxRecoverRequest</code> and carries transaction identifiers.
 */
public class XACnxRecoverReply extends AbstractJmsReply
{
  /** Branch qualifiers. */
  private Vector bqs;
  /** Format identifiers. */
  private Vector fis;
  /** Global transaction identifiers. */
  private Vector gtis;


  /**
   * Constructs a <code>XACnxRecoverReply</code> instance. 
   *
   * @param req   The replied request.
   * @param bqs   Branch qualifiers.
   * @param fis   Format identifiers.
   * @param gtis  Global transaction identifiers.
   */
  public XACnxRecoverReply(XACnxRecoverRequest req,
                            Vector bqs,
                            Vector fis,
                            Vector gtis)
  {
    super(req.getRequestId());
    this.bqs = bqs;
    this.fis = fis;
    this.gtis = gtis;
  }

  /**
   * Constructs a <code>XACnxRecoverReply</code> instance. 
   */
  public XACnxRecoverReply()
  {}

  
  /** Returns the number of transaction identifiers. */
  public int getSize()
  {
    return bqs.size();
  }

  /** Returns a branch qualifier. */
  public byte[] getBranchQualifier(int index)
  {
    return (byte[]) bqs.get(index);
  }

  /** Returns a format identifier. */
  public int getFormatId(int index)
  {
    return ((Integer) fis.get(index)).intValue();
  }

  /** Returns a global transaction identifier. */
  public byte[] getGlobalTransactionId(int index)
  {
    return (byte[]) gtis.get(index);
  }

  public void setBQS(Vector bqs)
  {
    this.bqs = bqs;
  }

  public void setFIS(Vector fis)
  {
    this.fis = fis;
  }

  public void setGTIS(Vector gtis)
  {
    this.gtis = gtis;
  }


  public Hashtable soapCode()
  {
    Hashtable h = super.soapCode();
    h.put("bqs", bqs);
    h.put("fis", bqs);
    h.put("gtis", bqs);
    return h;
  }

  public static Object soapDecode(Hashtable h)
  {
    XACnxRecoverReply req = new XACnxRecoverReply();
    req.setCorrelationId(((Integer) h.get("correlationId")).intValue());
    req.setBQS((Vector) h.get("bqs"));
    req.setFIS((Vector) h.get("fis"));
    req.setGTIS((Vector) h.get("gtis"));
    return req;
  }
}
