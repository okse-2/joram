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
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s):
 */
package fr.dyade.aaa.joram;

import fr.dyade.aaa.mom.jms.AbstractJmsReply;

import java.io.*;
import java.net.*;

import javax.jms.JMSException;

import org.objectweb.util.monolog.api.BasicLevel;

/**
 * A <code>Driver</code> daemon is attached to a connection for getting
 * server replies on its input stream.
 */
class Driver extends fr.dyade.aaa.util.Daemon
{
  /** The connection the driver is attached to. */
  private Connection cnx;
  /** The input stream to listen to. */
  private ObjectInputStream ois;

  /** <code>true</code> if the driver is stopping. */
  boolean stopping = false;
  
  /**
   * Constructs a <code>Driver</code> daemon.
   *
   * @param cnx  The connection the driver listens on.
   * @param ois  The connection's input stream.
   */
  public Driver(Connection cnx, ObjectInputStream ois)
  {
    super(cnx.toString());
    this.cnx = cnx;
    this.ois = ois;
  }

  /** The driver's loop. */
  public void run()
  {
    Object obj = null;
    try {
      while (running) {
        canStop = true; 
  
        // Waiting for an object on the stream:
        try {
          if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
            JoramTracing.dbgClient.log(BasicLevel.DEBUG, "Driver: waiting...");
          obj = ois.readObject();
          if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
            JoramTracing.dbgClient.log(BasicLevel.DEBUG,
                                       "Driver: got an object!");
        }
        // Catching an IOException:
        catch (IOException ioE) {
          if (! cnx.closing) {
            JMSException jE = new JMSException("The connection is broken,"
                                               + " the driver stops.");
            jE.setLinkedException(ioE);
  
            // Passing the asynchronous exception to the connection:
            cnx.onException(jE);

            // Interrupting the synchronous requesters:
            java.util.Enumeration enum = cnx.requestsTable.keys();
            String reqId;
            while (enum.hasMoreElements()) {
              reqId = (String) enum.nextElement();
              obj = cnx.requestsTable.remove(reqId);
              if (obj instanceof Lock) {
                synchronized(obj) {
                  obj.notify();
                }
              }
            }
            // Closing the connection:
           try {
             stopping = true;
             cnx.close();
           }
           catch (JMSException jE3) {}
          }
          canStop = true;
          break;
        }
        // Passing the reply to the connection:
        canStop = false;
        cnx.distribute((AbstractJmsReply) obj);
      }
    }
    catch (Exception e) {
      JMSException jE2 = new JMSException("Exception while getting data from"
                                          + " the socket.");
      jE2.setLinkedException(e);
  
      // Passing the asynchronous exception to the connection:
      cnx.onException(jE2);
    }
    finally {
      finish();
    }
  } 

  /** Shuts the driver down. */
  public void shutdown()
  {
    try {
      ois.close();
    }
    catch (Exception e) {}
    close();

    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, "Driver: shut down."); 
  }

  /** Releases the driver's resources. */
  public void close()
  {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, "Driver: finished."); 
  }
}
