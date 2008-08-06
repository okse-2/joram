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
 * Contributor(s): Nicolas Tachker (ScalAgent)
 */
package com.scalagent.kjoram;

import com.scalagent.kjoram.jms.AbstractJmsReply;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.Enumeration;

import com.scalagent.kjoram.excepts.IllegalStateException;
import com.scalagent.kjoram.excepts.JMSException;

/**
 * Each <code>Connection</code> holds a <code>Driver</code> daemon for
 * listening to asynchronous replies coming from the connected server.
 *
 */
public abstract class Driver extends com.scalagent.kjoram.util.Daemon
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

    if (JoramTracing.dbgClient)
      JoramTracing.log(JoramTracing.DEBUG, this + ": created.");
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
          if (JoramTracing.dbgClient)
            JoramTracing.log(JoramTracing.DEBUG, "Driver: waiting...");
          delivery = getDelivery();
          if (JoramTracing.dbgClient)
            JoramTracing.log(JoramTracing.DEBUG,"Driver: got a delivery!");
          if (delivery == null) {
            continue;
          }
        }
        // Catching an InterruptedException:
        catch (InterruptedException exc) {
          if (JoramTracing.dbgClient)
            JoramTracing.log(JoramTracing.WARN,"Driver: caught an" +
                             " InterruptedException: " + exc);
          continue;
        }
        // Catching an IOException:
        catch (IOException exc) {
          if (! cnx.closing) {
            
            stopping = true;

            IllegalStateException jmsExc =
              new IllegalStateException("The connection is broken,"
                                        + " the driver stops.");
            jmsExc.setLinkedException(exc);

            // Passing the asynchronous exception to the connection:
            cnx.onException(jmsExc);

            // Interrupting the synchronous requesters:
            if (JoramTracing.dbgClient)
              JoramTracing.log(JoramTracing.DEBUG, this + "interrupts synchronous"
                               + " requesters.");

            Integer reqId;
            Object obj;
            for (Enumeration e = cnx.requestsTable.keys();
                 e.hasMoreElements();) {
              reqId = (Integer) e.nextElement();
              obj = cnx.requestsTable.remove(reqId);
              if (obj instanceof Lock) {
                synchronized(obj) {
                  obj.notify();
                }
              }
            }

            // Closing the connection:
            if (JoramTracing.dbgClient)
              JoramTracing.log(JoramTracing.DEBUG,this + ": closes the connection.");
            try {
              cnx.close();
            }
            catch (JMSException jExc) {}
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
    if (JoramTracing.dbgClient)
      JoramTracing.log(JoramTracing.DEBUG, this + ": closed."); 
  }
}
