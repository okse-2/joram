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

import fr.dyade.aaa.agent.NotificationOutputStream;
import fr.dyade.aaa.mom.jms.AbstractJmsReply;
import java.io.*;


/** 
 * The <code>JmsOutputStream</code> class is used by the JMS proxy agents
 * for extracting JMS reply objects from <code>OutputNotification</code>
 * notifications and writing them on the output stream.
 */
public class JmsOutputStream implements NotificationOutputStream
{
  /** The output stream. */
  private ObjectOutputStream out;

  /**
   * Constructs a <code>JMSOutputStream</code> instance for a given
   * <code>OutputStream</code>.
   *
   * @param out  The output stream.
   */
  public JmsOutputStream(OutputStream out) throws IOException
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
