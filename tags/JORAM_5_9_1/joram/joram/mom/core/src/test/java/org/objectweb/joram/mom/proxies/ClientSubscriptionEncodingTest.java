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
import org.objectweb.joram.mom.util.MessageTable;

import fr.dyade.aaa.agent.AgentId;

public class ClientSubscriptionEncodingTest {

  @Test
  public void run() throws Exception {
    EncodingHelper.init();

    AgentId proxyId = new AgentId((short) 10, (short) 20, 30);
    int contextId = 40; // Not encoded by ClientSubscription
    int reqId = 50; // Not encoded by ClientSubscription
    boolean durable = true;
    AgentId topicId = new AgentId((short) 60, (short) 70, 80);
    String name = "test-subscription";
    String selector = "test-selector";
    boolean noLocal = true;
    AgentId dmqId = new AgentId((short) 90, (short) 100, 110);
    int threshold = 120;
    int nbMaxMsg = 130;
    MessageTable messagesTable = null; // Not encoded by ClientSubscription
    String clientID = null;

    ClientSubscription cs1 = new ClientSubscription(proxyId, contextId, reqId,
        durable, topicId, name, selector, noLocal, dmqId, threshold, nbMaxMsg,
        messagesTable, clientID);

    cs1.getDeliveredIds().put("msg1", "msg1");
    cs1.getDeliveredIds().put("msg2", "msg2");

    cs1.getDeniedMsgs().put("msg3", 1);
    cs1.getDeniedMsgs().put("msg4", 2);

    checkEncoding(cs1);

    clientID = "clientId";
    ClientSubscription cs2 = new ClientSubscription(proxyId, contextId, reqId,
        durable, topicId, name, selector, noLocal, dmqId, threshold, nbMaxMsg,
        messagesTable, clientID);

    checkEncoding(cs2);
  }

  private void checkEncoding(ClientSubscription cs) throws Exception {
    byte[] bytes = EncodingHelper.encode(cs);

    ClientSubscription csDec = (ClientSubscription) EncodingHelper.decode(
        cs.getEncodableClassId(), bytes);

    Assert.assertEquals(cs.getThreshold(), csDec.getThreshold());
    Assert.assertEquals(cs.getDeliveredIds(), csDec.getDeliveredIds());
    Assert.assertEquals(cs.getDeniedMsgs(), csDec.getDeniedMsgs());
    Assert.assertEquals(cs.getClientID(), csDec.getClientID());
  }

}
