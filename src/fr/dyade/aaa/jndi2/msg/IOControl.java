/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2006 ScalAgent Distributed Technologies
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
 * Initial developer(s): ScalAgent Distributed Technologies
 */
package fr.dyade.aaa.jndi2.msg;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamConstants;
import java.io.OutputStream;
import java.net.Socket;

public class IOControl {
  /**
   *  This property allow to enable/disable SO_TIMEOUT with the specified
   * timeout in milliseconds.
   */
  public static final String SOCKET_SOTIMEOUT = "fr.dyade.aaa.jndi2.socketTimeOut";

  /**
   *  Enable/disable SO_TIMEOUT with the specified timeout in milliseconds.
   * The default value is zero which means the option is disabled.
   *  This value can be adjusted by setting the environment property
   * <code>fr.dyade.aaa.jndi2.socketTimeOut</code>.
   *  With this option set to a non-zero timeout, a read() call on the
   * InputStream associated with this Socket will block for only this amount
   * of time.
   */
  private static int socketTimeOut =
    Integer.getInteger(SOCKET_SOTIMEOUT, 0).intValue();

  private Socket socket;

  private BufferedInputStream bis;

  private NetOutputStream nos;

  public IOControl(Socket socket) throws IOException {
    this.socket = socket;
    socket.setTcpNoDelay(true);
    socket.setSoTimeout(socketTimeOut);
    socket.setSoLinger(true, 1000);
    nos = new NetOutputStream(socket);
    bis = new BufferedInputStream(socket.getInputStream());
  }

  public Object readObject() 
    throws IOException, ClassNotFoundException {
    ObjectInputStream ois = new ObjectInputStream(bis);
    return ois.readObject();
  }

  public int readInt() 
    throws IOException {
    DataInputStream dis = new DataInputStream(bis);
    return dis.readInt();
  }
  
  public void writeObject(Object obj) throws IOException {
    nos.send(obj);
  }

  public void writeInt(int i) throws IOException {
    nos.send(i);
  }

  public void close() {
    try {
      socket.getInputStream().close();
    } catch (IOException exc) {}
    try {
      socket.getOutputStream().close();
    } catch (IOException exc) {}
    try {
      socket.close();
    } catch (IOException exc) {}
  }

  public final Socket getSocket() {
    return socket;
  }

  static class NetOutputStream {
    private ByteArrayOutputStream baos = null;
    private ObjectOutputStream oos = null;
    private OutputStream os = null;

    static private final byte[] streamHeader = {
      (byte)((ObjectStreamConstants.STREAM_MAGIC >>> 8) & 0xFF),
      (byte)((ObjectStreamConstants.STREAM_MAGIC >>> 0) & 0xFF),
      (byte)((ObjectStreamConstants.STREAM_VERSION >>> 8) & 0xFF),
      (byte)((ObjectStreamConstants.STREAM_VERSION >>> 0) & 0xFF)
    };

    NetOutputStream(Socket sock) throws IOException {
      baos = new ByteArrayOutputStream(1024);
      oos = new ObjectOutputStream(baos);
      baos.reset();
      os = sock.getOutputStream();
    }

    void send(Object msg) throws IOException {
      try {
        baos.write(streamHeader, 0, 4);
        oos.writeObject(msg);
        oos.flush();

        baos.writeTo(os);
        os.flush();
      } finally {
        oos.reset();
        baos.reset();
      }
    }

    void send(int i) throws IOException {
      DataOutputStream daos = new DataOutputStream(os);
      daos.writeInt(i);
      daos.flush();
    }
  }
}
