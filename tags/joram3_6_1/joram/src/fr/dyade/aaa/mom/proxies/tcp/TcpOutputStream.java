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
 * Initial developer(s):Andre Freyssinet (ScalAgent)
 * Contributor(s):
 */
package fr.dyade.aaa.mom.proxies.tcp;

import fr.dyade.aaa.agent.NotificationOutputStream;
import fr.dyade.aaa.mom.jms.AbstractJmsReply;
import java.io.*;


/** 
 * The <code>TcpOutputStream</code> class is used by the JMS tcp proxy agents
 * for extracting JMS reply objects from <code>OutputNotification</code>
 * notifications and writing them on the output stream.
 */
public class TcpOutputStream implements NotificationOutputStream
{
  /** The output stream. */
  private ObjectOutputStream out;

  /**
   * Constructs a <code>TcpOutputStream</code> instance for a given
   * <code>OutputStream</code>.
   *
   * @param out  The output stream.
   */
  public TcpOutputStream(OutputStream out) throws IOException
  {
    this.out = new ObjectOutputStream(out);
    this.out.flush();
  }
  
  /**
   * Gets an <code>AbstractJmsReply</code> object from a given notification
   * and writes it on the output stream.
   *
   * @param not  Notification containing a JMS reply object.
   * @exception IOException  In case of an output stream problem.
   * @exception StreamCorruptedException  In case of an incorrect notification
   *                or wrapped object.
   */ 
  public void writeNotification(fr.dyade.aaa.agent.Notification not)
    throws IOException
  {
    if (not instanceof OutputNotification) {
      Object obj = ((OutputNotification) not).getObj();

      if (obj instanceof AbstractJmsReply)
        out.writeObject((AbstractJmsReply) obj);
      else
        throw new StreamCorruptedException("Object " + obj.getClass().getName()
                                           + " is not a JMS reply and won't"
                                           + " be written on the output"
                                           + " stream.");
    }
    else
      throw new StreamCorruptedException("Invalid notification "
                                         + not.getClass().getName()
                                         + " passed to the output stream"
                                         + " filter.");
    out.reset();
    out.flush();
  }

  /** Closes the stream. */ 
  public void close() throws IOException
  {
    out.close();
  }
}		
