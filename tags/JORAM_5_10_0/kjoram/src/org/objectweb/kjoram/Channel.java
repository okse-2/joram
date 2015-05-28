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

public abstract class Channel {

  public static final int INIT = 0;
  public static final int CONNECTING = 1;
  public static final int CONNECTED = 2;
  public static final int CLOSE = 3;

  public static final String[] statusNames = {"INIT",
                                              "CONNECTING",
                                              "CONNECTED",
                                              "CLOSE"};

  protected volatile int status;

  int key;

  /**
   * Open connection, InputStream and OutputStream.
   * 
   * @throws IOException
   */
  public abstract void connect() throws IOException;

  /**
   * Sending a request through the TCP connection.
   * 
   * @param obj
   * @throws IOException
   */
  public abstract void send(AbstractRequest obj) throws IOException;

  /**
   * Receive an abstract reply.
   * 
   * @return
   * @throws Exception
   */
  public abstract AbstractReply receive() throws Exception;

  /**
   * Closes the TCP connection.
   * 
   * @throws IOException
   */
  public abstract void close() throws IOException;
}
