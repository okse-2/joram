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
package org.objectweb.joram.client.jms;

/**
 * Implements the <code>javax.transaction.xa.Xid</code> interface for
 * identifying distributed transactions.
 */
public class XidImpl implements javax.transaction.xa.Xid
{
  public byte[] branchQualifier;
  public int formatId;
  public byte[] globalTransactionId;


  /** Constructs a <code>XidImpl</code> instance. */
  public XidImpl(byte[] branchQualifier,
                 int formatId,
                 byte[] globalTransactionId)
  {
    this.branchQualifier = branchQualifier;
    this.formatId = formatId;
    this.globalTransactionId = globalTransactionId;
  }


  public byte[] getBranchQualifier()
  {
    return branchQualifier;
  }

  public int getFormatId()
  {
    return formatId;
  }

  public byte[] getGlobalTransactionId()
  {
    return globalTransactionId;
  }
}
