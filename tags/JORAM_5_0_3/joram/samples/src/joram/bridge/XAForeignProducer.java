/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2007 - ScalAgent DT
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
 * Initial developer(s): Nicolas Tachker (ScalAgent)
 * Contributor(s): 
 */
package bridge;

import javax.jms.Destination;
import javax.jms.MessageProducer;
import javax.jms.TextMessage;
import javax.jms.XAConnection;
import javax.jms.XAConnectionFactory;
import javax.jms.XASession;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.objectweb.joram.client.jms.XidImpl;

/**
 * Produces messages on the foreign destination.
 */
public class XAForeignProducer {
  public static void main(String[] args) throws Exception {
    javax.naming.Context jndiCtx = new javax.naming.InitialContext();  
    Destination foreignDest = (Destination) jndiCtx.lookup("foreignQueue");
    XAConnectionFactory foreignCF = (XAConnectionFactory) jndiCtx.lookup("foreignCF");
    jndiCtx.close();

    XAConnection foreignCnx = foreignCF.createXAConnection();
    XASession foreignSess = foreignCnx.createXASession();
    MessageProducer foreignSender = foreignSess.createProducer(foreignDest);
    XAResource producerRes = foreignSess.getXAResource();
    
    Xid xid = new XidImpl(new byte[0], 1, new String(""+System.currentTimeMillis()).getBytes());
    producerRes.start(xid, XAResource.TMNOFLAGS);

    TextMessage foreignMsg = foreignSess.createTextMessage();

    for (int i = 1; i < 11; i++) {
      foreignMsg.setText("Foreign message number " + i);
      System.out.println("send msg = " + foreignMsg.getText());
      foreignSender.send(foreignMsg);
    }

    producerRes.end(xid, XAResource.TMSUCCESS);
    producerRes.prepare(xid);
    producerRes.commit(xid, false);

    foreignCnx.close();
  }
}
