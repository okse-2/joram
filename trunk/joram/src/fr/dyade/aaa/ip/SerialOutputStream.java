/*
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
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
 */


package fr.dyade.aaa.ip;

import fr.dyade.aaa.agent.*;
import java.io.*;


/**
  * Class which writes a <code>Notification</code> object as a byte stream
  * using the standard serialization mechanism of java.
  *
  * @author	Lacourte Serge
  * @version	v1.0
  */
public class SerialOutputStream implements NotificationOutputStream {

public static final String RCS_VERSION="@(#)$Id: SerialOutputStream.java,v 1.10 2004-03-16 10:03:45 fmaistre Exp $"; 


  ObjectOutputStream out;

  /**
   * Creates a filter built on top of the specified <code>OutputStream</code>.
   */
  public SerialOutputStream(OutputStream out) throws IOException {
    this.out = new ObjectOutputStream(out);
    // Flush the header in order to prevent the input side from
    // blocking if the output stream is buffered.
    this.out.flush();
  }

  /**
    * Writes a <code>Notification</code> to the stream.
    */
  public void writeNotification(Notification msg) throws IOException {
    out.writeObject(msg);
    out.reset();
    out.flush();
  }

  /**
   * Closes the stream.
   */
  public void close() throws IOException {
    try {
      out.close();
    } catch (IOException exc) {
      // close calls flush, but flush is called after each write
      // so there should be nothing left to write when closing this object
      // however it seems an ObjectOutputStream writes data on close
      // which may raise exceptions when the stream is already closed
    }
  }
}
