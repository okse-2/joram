/*
 * Copyright (C) 2001 SCALAGENT
 */
package fr.dyade.aaa.agent;

import java.io.*;

import org.objectweb.monolog.api.BasicLevel;
import org.objectweb.monolog.api.Monitor;

/**
 * Message used to acknowledge transmission.
 *
 * @see fr.dyade.aaa.PoolCnxNetwork
 * @see fr.dyade.aaa.TransientNetworkProxy
 * @see fr.dyade.aaa.TransientNetworkServer
 */
class StatusMessage implements Serializable {
  /** RCS version number of this file: $Revision: 1.2 $ */
  public static final String RCS_VERSION="@(#)$Id: StatusMessage.java,v 1.2 2002-03-06 16:50:00 joram Exp $";

  transient byte status;
  transient int stamp;

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
