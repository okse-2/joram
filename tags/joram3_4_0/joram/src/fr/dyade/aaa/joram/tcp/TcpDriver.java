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
package fr.dyade.aaa.joram.tcp;

import fr.dyade.aaa.mom.jms.AbstractJmsReply;

import java.io.*;


/**
 * A <code>TcpDriver</code> gets server deliveries coming through a TCP socket.
 */
class TcpDriver extends fr.dyade.aaa.joram.Driver
{
  /** The input stream to listen on. */
  private ObjectInputStream ois;

  /**
   * Constructs a <code>TcpDriver</code> daemon.
   *
   * @param cnx  The connection the driver belongs to.
   * @param ois  The connection's input stream.
   */
  TcpDriver(fr.dyade.aaa.joram.Connection cnx, ObjectInputStream ois)
  {
    super(cnx);
    this.ois = ois;
  }

 
  /**
   * Returns an <code>AbstractJmsReply</code> delivered by the connected
   * server.
   *
   * @exception IOException  If the connection failed.
   * @exception ClassNotFoundException  If the reply is invalid.
   */
  protected AbstractJmsReply getDelivery() throws Exception
  {
    return (AbstractJmsReply) ois.readObject();
  } 

  /** Shuts down the driver. */
  public void shutdown()
  {
    try {
      ois.close();
    }
    catch (Exception e) {}
    close();
  }
}
