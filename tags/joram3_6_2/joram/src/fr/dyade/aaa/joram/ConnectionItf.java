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


/**
 * The <code>ConnectionItf</code> interface defines the methods provided to
 * <code>Connection</code> objects for actually exchanging requests and
 * replies with the Joram platform.
 * <p>
 * This interface is implemented by classes dedicated to a given communication
 * protocol (as TCP or SOAP).
 *
 * @see fr.dyade.aaa.joram.tcp.TcpConnection
 * @see fr.dyade.aaa.joram.soap.SoapConnection
 */
public interface ConnectionItf
{
  /**
   * Creates a driver for providing the connection with server's replies.
   *
   * @param cnx  The calling <code>Connection</code> instance.
   */
  public Driver createDriver(Connection cnx);

  /**
   * Sends a JMS request to the server.
   *
   * @exception javax.jms.IllegalStateException  If the connection failed to
   *              route the sending because it is broken or not established.
   */
  public void send(fr.dyade.aaa.mom.jms.AbstractJmsRequest request)
              throws javax.jms.IllegalStateException;

  /** Closes the connection. */
  public void close();
}
