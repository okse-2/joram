/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
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
 * Initial developer(s): Andre Freyssinet (ScalAgent)
 * Contributor(s):
 */
package fr.dyade.aaa.mom.proxies.tcp;

import fr.dyade.aaa.agent.NotificationInputStream;
import fr.dyade.aaa.mom.jms.AbstractJmsRequest;

import java.io.*;

/**
 * The <code>TcpInputStream</code> class is used by JMS tcp proxy agents for 
 * reading JMS request objects on the input stream and wrapping them
 * into notifications.
 */
public class TcpInputStream implements NotificationInputStream
{
  /** The input stream. */
  private ObjectInputStream in;


  /**
   * Constructs a <code>TcpInputStream</code> instance for a given
   * <code>InputStream</code>.
   *
   * @param in  The input stream.
   */
  public TcpInputStream(InputStream in) throws IOException
  {
    this.in = new ObjectInputStream(new BufferedInputStream(in));
  }


  /**
   * Wraps the client request read on the stream in an
   * <code>InputNotification</code>.
   *
   * @exception StreamCorruptedException  In case of an invalid object read
   *                on the stream.
   * @exception IOException  In case of an input stream problem.
   */
  public fr.dyade.aaa.agent.Notification readNotification()
       throws ClassNotFoundException, IOException
  {
    Object obj = in.readObject();
    if (obj instanceof AbstractJmsRequest)
      return new InputNotification(obj);
    else 
      throw new StreamCorruptedException("Invalid object read on stream.");
  }

  /** Closes the stream. */
  public void close() throws IOException
  {
    in.close();
  }
}
