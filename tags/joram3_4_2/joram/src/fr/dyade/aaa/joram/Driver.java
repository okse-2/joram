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

import java.io.IOException;

import javax.jms.IllegalStateException;
import javax.jms.JMSException;

import org.objectweb.util.monolog.api.BasicLevel;

/**
 * Each <code>Connection</code> holds a <code>Driver</code> daemon for
 * listening to asynchronous replies coming from the connected server.
 *
 * @see fr.dyade.aaa.joram.tcp.TcpDriver
 * @see fr.dyade.aaa.joram.soap.SoapDriver
 */
public abstract class Driver extends fr.dyade.aaa.util.Daemon
{
  /** The connection the driver belongs to. */
  private Connection cnx;

  /** <code>true</code> if the driver is stopping. */
  boolean stopping = false;

  
  /**
   * Constructs a <code>Driver</code> daemon.
   *
   * @param cnx  The connection the driver belongs to.
   */
  protected Driver(Connection cnx)
  {
    super(cnx.toString());
    this.cnx = cnx;

    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, this + ": created.");
  }

  /** String view of a <code>Driver</code> instance. */
  public String toString()
  {
    return "Driver:" + cnx.toString();
  }


  /** The driver's listening loop. */
  public void run()
  {
    AbstractJmsReply delivery = null;

    try {
      while (running) {
        canStop = true; 
  
        // Waiting for an asynchronous delivery:
        try {
          if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
            JoramTracing.dbgClient.log(BasicLevel.DEBUG, "Driver: waiting...");
          delivery = getDelivery();
          if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
            JoramTracing.dbgClient.log(BasicLevel.DEBUG,
                                       "Driver: got a delivery!");
        }
        // Catching an InterruptedException:
        catch (InterruptedException exc) {
          if (JoramTracing.dbgClient.isLoggable(BasicLevel.WARN))
            JoramTracing.dbgClient.log(BasicLevel.WARN,
                                       "Driver: caught an"
                                       + " InterruptedException: " + exc);
          continue;
        }
        // Catching an IOException:
        catch (IOException exc) {
          if (! cnx.closing) {
            IllegalStateException jmsExc =
              new IllegalStateException("The connection is broken,"
                                        + " the driver stops.");
            jmsExc.setLinkedException(exc);
  
            // Passing the asynchronous exception to the connection:
            cnx.onException(jmsExc);

            // Interrupting the synchronous requesters:
            if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
              JoramTracing.dbgClient.log(BasicLevel.DEBUG, this
                                         + "interrupts synchronous"
                                         + " requesters.");

            java.util.Enumeration enum = cnx.requestsTable.keys();
            String reqId;
            Object obj;
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
            stopping = true;
            if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
              JoramTracing.dbgClient.log(BasicLevel.DEBUG,
                                         this + ": closes the connection.");
            try {
              cnx.close();
            }
            catch (javax.jms.JMSException jExc) {}
          }
          canStop = true;
          break;
        }
        // Passing the reply to the connection:
        canStop = false;
        cnx.distribute(delivery);
      }
    }
    catch (Exception exc) {
      JMSException jmsExc = new JMSException("Exception while getting data"
                                             + " from the server.");
      jmsExc.setLinkedException(exc);
  
      // Passing the asynchronous exception to the connection:
      cnx.onException(jmsExc);
    }
    finally {
      finish();
    }
  }

  /**
   * Returns an <code>AbstractJmsReply</code> delivered by the connected
   * server.
   *
   * @exception Exception  If a problem occurs when getting the delivery.
   */
  protected abstract AbstractJmsReply getDelivery() throws Exception;

  /** Shuts the driver down. */
  public abstract void shutdown();

  /** Releases the driver's resources. */
  public void close()
  {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, this + ": closed."); 
  }
}
