/*
 * Copyright (C) 2001 SCALAGENT
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
 */
package fr.dyade.aaa.agent;

import java.io.*;

/**
 * Message used to acknowledge transmission.
 *
 * @see fr.dyade.aaa.PoolCnxNetwork
 * @see fr.dyade.aaa.TransientNetworkProxy
 * @see fr.dyade.aaa.TransientNetworkServer
 */
class StatusMessage implements Serializable {
  /** RCS version number of this file: $Revision: 1.10 $ */
  public static final String RCS_VERSION="@(#)$Id: StatusMessage.java,v 1.10 2004-02-13 10:15:21 fmaistre Exp $";

  transient byte status;
  transient int stamp = -1;

  StatusMessage(byte status) {
    super();
    this.status = status;
  }

  static byte AckStatus = 0;
  static byte NAckStatus = -1;

  public final String toString() {
    return "StatusMessage(" + status + ", " + stamp + ")";
  }

  private void writeObject(java.io.ObjectOutputStream out)
    throws IOException {
    out.writeByte(status);
    out.writeInt(stamp);
  }

  private void readObject(java.io.ObjectInputStream in)
    throws IOException, ClassNotFoundException {
    status = in.readByte();
    stamp = in.readInt();
  }
}
