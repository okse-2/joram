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
package org.objectweb.joram.mom.notifications;

import junit.framework.Assert;

import org.junit.Test;
import org.objectweb.joram.mom.proxies.EncodingHelper;

import fr.dyade.aaa.agent.AgentId;

public class TopicForwardNotTest {
  
  public static final String PROPERTY1 = "property1";
  public static final String PROPERTY2 = "property2";
  public static final String PROPERTY3 = "property3";
  public static final String PROPERTY4 = "property4";

  @Test
  public void run() throws Exception {
    EncodingHelper.init();
    
    org.objectweb.joram.shared.messages.Message sharedMsg = new org.objectweb.joram.shared.messages.Message();
    sharedMsg.id = "msgId";
    sharedMsg.toId = "toId";
    sharedMsg.toName = null;
    sharedMsg.toType = 1;
    sharedMsg.timestamp = 17899823;
    sharedMsg.compressed = true;
    sharedMsg.deliveryTime = 100;
    sharedMsg.body = null;
    sharedMsg.clientID = null;

    sharedMsg.toName = "toName"; // null
    sharedMsg.body = new byte[1000];  // null
    sharedMsg.clientID = "clientId"; // null
    
    ClientMessages clientMessages = new ClientMessages(1, 2);
    clientMessages.addMessage(sharedMsg);
    clientMessages.setProxyId(new AgentId((short)1, (short)2, 3));
    
    TopicForwardNot topicForwardNot = new TopicForwardNot(clientMessages, false);
    checkEncoding(topicForwardNot);
    /*
    sharedMsg.toName = null;
    sharedMsg.body = null;
    sharedMsg.clientID = null;
    checkEncoding(momMsg);
    
    sharedMsg.type = org.objectweb.joram.shared.messages.Message.TEXT;
    sharedMsg.replyToId = "replyToId";
    sharedMsg.replyToName = "replyToName";
    sharedMsg.replyToType = 1;
    sharedMsg.priority = 10;
    sharedMsg.expiration = 150;
    sharedMsg.correlationId = "correlationId";
    sharedMsg.deliveryCount = 4;
    sharedMsg.jmsType = "jmsType";
    sharedMsg.redelivered = true;
    sharedMsg.persistent = true;
    checkEncoding(momMsg);
    
    Properties properties = new Properties();
    properties.put(PROPERTY1, "propValue1");
    properties.put(PROPERTY2, new Integer(1));
    properties.put(PROPERTY3, new Long(1));
    properties.put(PROPERTY4, Boolean.TRUE);
    sharedMsg.properties = properties;
    checkEncoding(momMsg);*/
  }

  private void checkEncoding(TopicForwardNot topicForwardNot) throws Exception {
    byte[] bytes = EncodingHelper.encode(topicForwardNot);

    TopicForwardNot dec = (TopicForwardNot) EncodingHelper.decode(
        topicForwardNot.getEncodableClassId(), bytes);

    ClientMessages cm = topicForwardNot.messages;
    
    Assert.assertEquals(topicForwardNot.fromCluster, dec.fromCluster);
    Assert.assertEquals(cm.getProxyId(), dec.messages.getProxyId());
  }

}
