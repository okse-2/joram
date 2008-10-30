/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2007-2008 ScalAgent Distributed Technologies
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
package org.objectweb.joram.mom.amqp.marshalling;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.util.Debug;

public class MarshallingBody {
  public static Logger logger = Debug.getLogger(MarshallingBody.class.getName());

  private byte[] binPayload;
  
  /**
   * @param binPayload the binPayload to set
   */
  public void setBinPayload(byte[] binPayload) {
    this.binPayload = binPayload;
  }

  /**
   * @return the binPayload
   */
  public byte[] getBinPayload() {
    return binPayload;
  }

  /**
   * Constructs an <code>MarshallingBody</code>.
   */
  public MarshallingBody() {
  }

  public static void write(MarshallingBody msg, OutputStream os)
      throws IOException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "MarshallingBody.write(" + msg + ", " + os + ')');
    DataOutputStream out = new DataOutputStream(os);
    out.write(msg.getBinPayload());
    //AMQPStreamUtil.writeOctet(AMQP.FRAME_END, out);
    out.flush();
  }

  public static MarshallingBody read(InputStream is, int bodySize)
      throws IOException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "MarshallingBody.read(" + is + ", " + bodySize + ')');

    MarshallingBody marshallingBody = new MarshallingBody();
    DataInputStream in = new DataInputStream(is);
    //System.out.println("* body available = " + in.available());
    marshallingBody.binPayload = AMQPStreamUtil.readByteArray(in, bodySize);
    //int frameEndMarker = AMQPStreamUtil.readOctet(in);
    return marshallingBody;
  }
}
