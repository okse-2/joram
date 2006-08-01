/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - Bull SA
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
 * Initial developer(s): Frederic Maistre (Bull SA)
 * Contributor(s):
 */
package bridge;

import javax.jms.*;

/**
 * Consumes messages on a foreign destination through the JORAM bridge.
 */
public class BridgeConsumer
{
  public static void main(String[] args) throws Exception
  {
    javax.naming.Context jndiCtx = new javax.naming.InitialContext();
    Destination dest = (Destination) jndiCtx.lookup("bridgeD");
    ConnectionFactory cnxFact = (ConnectionFactory) jndiCtx.lookup("cf");
    jndiCtx.close();

    Connection cnx = cnxFact.createConnection();
    Session sess = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
    MessageConsumer cons = sess.createConsumer(dest);
 
    cons.setMessageListener(new MsgListener());

    cnx.start();

    System.in.read();

    cnx.close();
  }
}
