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
package fr.dyade.aaa.joram; 
 
import javax.jms.*;

/**
 * A <code>ConnectionConsumer</code> is a special facility for
 * consuming messages arriving on a <code>Connection</code>.
 * <br>To be used by application servers only.
 *
 * @author Frederic Maistre
 */
public class ConnectionConsumer implements javax.jms.ConnectionConsumer
{
  /** The destination the consumed messages come from. */
  private Destination destination;
  /** Selector for filtering the messages. */
  private String selector;
  /** The ServerSessionPool from which getting the Sessions. */
  private ServerSessionPool ssp;
  /** The number of messages to pass to a same Session. */
  private int maxMessages;

  private ServerSession serverSession;
  private fr.dyade.aaa.joram.Session session;
  private int counter = 1;


  /** Constructor. */
  public ConnectionConsumer(Destination destination, String selector, 
    ServerSessionPool ssp, int maxMessages)
  {
    this.destination = destination;
    this.selector = selector;
    this.ssp = ssp;
    this.maxMessages = maxMessages;
  }

  /** Method returning the ServerSessionPool parameter. */
  public ServerSessionPool getServerSessionPool() throws JMSException
  {
    if (ssp != null)
      return ssp;
    else
      throw new JMSException("ServerSessionPool is null");
  }

  /** Method getting the messages from the connection. */
  public void getMessage(fr.dyade.aaa.mom.MessageMOMExtern msg)
    throws JMSException
  {
    try {
      if (counter == 1) {
        // When starting to work on a new series of messages,
        // getting new ServerSession and Session.
        serverSession = ssp.getServerSession();
        session = (fr.dyade.aaa.joram.Session) serverSession.getSession();
      }
      // Putting the message in the session.
      session.addCCMessage(msg);
     
      if (counter == maxMessages) {
        // When reaching the maximum number of messages per Session,
        // starting the ServerSession, and resetting counter to start. 
        serverSession.start();
        counter = 1;
      }
      else
        counter++;
    } catch (JMSException e) {
      throw (e);
    }
  }
   
  /** Closing method. */ 
  public void close() 
  {
  System.gc();
  }

}
