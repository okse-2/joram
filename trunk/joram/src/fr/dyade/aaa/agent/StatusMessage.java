/*
 * Copyright (C) 2001 SCALAGENT
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
  /** RCS version number of this file: $Revision: 1.4 $ */
  public static final String RCS_VERSION="@(#)$Id: StatusMessage.java,v 1.4 2002-10-21 08:41:13 maistrfr Exp $";

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
