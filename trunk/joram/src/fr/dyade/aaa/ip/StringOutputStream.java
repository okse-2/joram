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
  * Class which writes a <code>Notification</code> object as its string value.
  *
  * @author	Lacourte Serge
  * @version	v1.0
  */
public class StringOutputStream implements NotificationOutputStream {

public static final String RCS_VERSION="@(#)$Id: StringOutputStream.java,v 1.9 2004-02-13 10:23:58 fmaistre Exp $"; 


  BufferedWriter out;

  /**
   * Creates a filter built on top of the specified <code>OutputStream</code>.
   */
  public StringOutputStream(OutputStream out) throws IOException {
    this.out = new BufferedWriter(new OutputStreamWriter(out));
  }

  /**
    * Writes a <code>Notification</code> to the stream.
    */
  public void writeNotification(Notification msg) throws IOException {
    out.write(msg.toString());
    out.newLine();
    out.flush();
  }

  /**
   * Closes the stream.
   */
  public void close() throws IOException {
    out.close();
  }
}
