/*
 * Copyright (C) 2002 - ScalAgent Distributed Technologies
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
 * fr.dyade.aaa.ip, fr.dyade.aaa.joram, fr.dyade.aaa.mom, and
 * fr.dyade.aaa.util, released May 24, 2000.
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
 *
 * The present code contributor is ScalAgent Distributed Technologies.
 */
package fr.dyade.aaa.mom.proxies;

import fr.dyade.aaa.agent.NotificationInputStream;
import fr.dyade.aaa.mom.jms.AbstractJmsRequest;

import java.io.*;

/**
 * The <code>JmsInputStream</code> class is used by JMS proxy agents for 
 * reading JMS request objects on the input stream and wrapping them
 * into notifications.
 */
public class JmsInputStream implements NotificationInputStream
{
  /** The input stream. */
  private ObjectInputStream in;


  /**
   * Constructs a <code>JmsInputStream</code> instance for a given
   * <code>InputStream</code>.
   *
   * @param in  The input stream.
   */
  public JmsInputStream(InputStream in) throws IOException
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
