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

public interface TcpSocket {
  
  /**
   * open the connection.
   * 
   * @param host Joram server host name.
   * @param port Joram server port.
   * @throws IOException
   */
  public void connect(String host, int port) throws IOException;
  
  /**
   * get the connection input stream.
   * 
   * @return connection input stream.
   * @throws IOException
   */
  public InputStream getInputStream() throws IOException;
  
  /**
   * get the connection output stream.
   * 
   * @return connection output stream.
   * @throws IOException
   */
  public OutputStream getOutputStream() throws IOException;
}
