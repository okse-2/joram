/*
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
 *
 * The contents of this file are subject to the Joram Public License,
 * as defined by the file JORAM_LICENSE.TXT 
 * 
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License on the Objectweb web site
 * (www.objectweb.org). 
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific terms governing rights and limitations under the License. 
 * 
 * The Original Code is Joram, including the java packages fr.dyade.aaa.agent,
 * fr.dyade.aaa.util, fr.dyade.aaa.ip, fr.dyade.aaa.mom, and fr.dyade.aaa.joram,
 * released May 24, 2000. 
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
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

public static final String RCS_VERSION="@(#)$Id: SerialOutputStream.java,v 1.2 2000-08-01 09:13:50 tachkeni Exp $"; 


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
