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


package fr.dyade.aaa.agent;

import java.io.*;


/**
 * This interface must be implemented by all classes used in
 * filtering an input stream to a <code>DriverIn</code> object.
 *
 * @author	Lacourte Serge
 * @version	v1.0
 *
 * @see	ProxyAgent
 * @see	DriverIn
 */
public interface NotificationInputStream {

public static final String RCS_VERSION="@(#)$Id: NotificationInputStream.java,v 1.1.1.1 2000-05-30 11:45:24 tachkeni Exp $"; 


  /**
   * Gets a <code>Notification</code> from the stream.
   *
   * @return	a notification, or <code>null</code> on end of stream.
   * @exception ClassNotFoundException
   *	when the notification class may not be found
   * @exception IOException
   *	thrown by the underlying stream operations
   */
  public Notification readNotification()
    throws ClassNotFoundException, IOException;

  /**
   * Closes the stream.
   */
  public void close() throws IOException;
}
