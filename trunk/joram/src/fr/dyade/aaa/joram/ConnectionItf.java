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
