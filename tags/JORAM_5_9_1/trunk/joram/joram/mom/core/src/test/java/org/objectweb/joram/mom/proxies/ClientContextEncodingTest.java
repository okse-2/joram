/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2013 ScalAgent Distributed Technologies
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
 * Initial developer(s): ScalAgent Distributed Technologies
 * Contributor(s):
 */
package org.objectweb.joram.mom.proxies;

import junit.framework.Assert;

import org.junit.Test;

import fr.dyade.aaa.agent.AgentId;

public class ClientContextEncodingTest {
  
  @Test
  public void run() throws Exception {
    EncodingHelper.init();
    
    // Not encoded by ClientContext
    AgentId proxyId = null;
    int id = 40;
    
    ClientContext cc1 = new ClientContext(proxyId, id);
    
    cc1.getActiveSubList().add("sub1");
    cc1.getActiveSubList().add("sub2");
    
    AgentId queue1 = new AgentId((short) 50, (short) 60, 70);
    cc1.getDeliveringQueueTable().put(queue1, queue1);
    AgentId queue2 = new AgentId((short) 80, (short) 90, 100);
    cc1.getDeliveringQueueTable().put(queue2, queue2);
    
    AgentId tmpDest1 = new AgentId((short) 110, (short) 120, 130);
    cc1.getTempDestinationList().add(tmpDest1);
    AgentId tmpDest2 = new AgentId((short) 120, (short) 130, 140);
    cc1.getTempDestinationList().add(tmpDest2);
    
    // TODO: test XA
    /*
    Xid xid1 = new Xid("bq1".getBytes(), 150, "gti".getBytes());
    XACnxPrepare prep1 = new XACnxPrepare(xid1.bq, xid1.fi, xid1.gti, null, null);
    cc1.getTransactionsTable().put(xid1, prep1);*/
    
    checkEncoding(cc1);
  }
  
  private void checkEncoding(ClientContext cc) throws Exception {
    byte[] bytes = EncodingHelper.encode(cc);

    ClientContext ccDec = (ClientContext) EncodingHelper.decode(
        cc.getEncodableClassId(), bytes);

    Assert.assertEquals(cc.getId(), ccDec.getId());
    Assert.assertEquals(cc.getActiveSubList(), ccDec.getActiveSubList());
    Assert.assertEquals(cc.getDeliveringQueueTable(), ccDec.getDeliveringQueueTable());
    Assert.assertEquals(cc.getTempDestinationList(), ccDec.getTempDestinationList());
    
    // TODO: test XA
    //Assert.assertEquals(cc.getTransactionsTable(), ccDec.getTransactionsTable());
  }

}
