/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
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
 * fr.dyade.aaa.ip, fr.dyade.aaa.joram, fr.dyade.aaa.mom, and
 * fr.dyade.aaa.util, released May 24, 2000.
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
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
