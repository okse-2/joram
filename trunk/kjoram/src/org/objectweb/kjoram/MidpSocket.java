/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 ScalAgent Distributed Technologies
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
package org.objectweb.kjoram;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.SocketConnection;


public class MidpSocket implements TcpSocket {
  private SocketConnection connection;

  /* (non-Javadoc)
   * @see org.objectweb.kjoram.TcpSocket#connect(java.lang.String, int)
   */
  public void connect(String host, int port) throws IOException {
    connection = (SocketConnection) Connector.open("socket://" + host + ':' + port);
    connection.setSocketOption(SocketConnection.LINGER, 5);
  }

  /* (non-Javadoc)
   * @see org.objectweb.kjoram.TcpSocket#getInputStream()
   */
  public InputStream getInputStream() throws IOException {
    return connection.openInputStream();
  }

  /* (non-Javadoc)
   * @see org.objectweb.kjoram.TcpSocket#getOutputStream()
   */
  public OutputStream getOutputStream() throws IOException {
    return connection.openOutputStream();
  }
}
