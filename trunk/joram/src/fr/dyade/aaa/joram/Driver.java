/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
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
 *
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s):
 */
package fr.dyade.aaa.joram;

import fr.dyade.aaa.mom.jms.AbstractJmsReply;

import java.io.IOException;
import java.util.Enumeration;

import javax.jms.IllegalStateException;
import javax.jms.JMSException;

import org.objectweb.util.monolog.api.BasicLevel;

/**
 * Each <code>Connection</code> holds a <code>Driver</code> daemon for
 * listening to asynchronous replies coming from the connected server.
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

            Integer reqId;
            Object obj;
            for (Enumeration enum = cnx.requestsTable.keys();
                 enum.hasMoreElements();) {
              reqId = (Integer) enum.nextElement();
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
