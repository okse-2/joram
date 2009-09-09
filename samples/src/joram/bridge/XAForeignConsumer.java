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
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.TextMessage;
import javax.jms.XAConnection;
import javax.jms.XAConnectionFactory;
import javax.jms.XASession;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.objectweb.joram.client.jms.XidImpl;

/**
 * Consumes messages on a foreign destination through the JORAM bridge.
 */
public class XAForeignConsumer {
  
  public static void main(String[] args) throws Exception {
    
    javax.naming.Context jndiCtx = new javax.naming.InitialContext();
    Destination foreignDest = (Destination) jndiCtx.lookup("foreignQueue");
    XAConnectionFactory foreignCF = (XAConnectionFactory) jndiCtx.lookup("foreignCF");
    jndiCtx.close();
    
    XAConnection foreignCnx = foreignCF.createXAConnection();
    XASession foreignSess = foreignCnx.createXASession();
    MessageConsumer foreignCons = foreignSess.createConsumer(foreignDest);
    XAResource resource = foreignSess.getXAResource();
    foreignCnx.start();   
 
    Xid xid = new XidImpl(new byte[0], 1, new String(""+System.currentTimeMillis()).getBytes());
    resource.start(xid, XAResource.TMNOFLAGS);
    System.out.println("resource = " + resource);

    for (int i = 1; i < 11; i++) {
      Message msg = foreignCons.receive();
      if (msg != null)
        System.out.println("reiceive : " + ((TextMessage)msg).getText());
      else
        System.out.println("msg = null");
    }

    System.out.println("commit xid = " + xid);
    resource.end(xid, XAResource.TMSUCCESS);
    resource.prepare(xid);
    resource.commit(xid, false);

    foreignCnx.close();
  }
}
