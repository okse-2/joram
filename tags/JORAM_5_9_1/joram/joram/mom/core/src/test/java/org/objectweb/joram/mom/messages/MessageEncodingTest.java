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
package org.objectweb.joram.mom.messages;

import junit.framework.Assert;

import org.junit.Test;
import org.objectweb.joram.mom.proxies.EncodingHelper;
import org.objectweb.joram.mom.util.MessageIdListImpl;

import fr.dyade.aaa.common.stream.Properties;

public class MessageEncodingTest {
  
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
    
    
    Message momMsg = new Message(sharedMsg);
    sharedMsg.toName = "toName"; // null
    sharedMsg.body = new byte[1000];  // null
    sharedMsg.clientID = "clientId"; // null
    momMsg.order = 999;

    checkEncoding(momMsg);
    
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
    checkEncoding(momMsg);
  }

  private void checkEncoding(Message msg) throws Exception {
    byte[] bytes = EncodingHelper.encode(msg);

    Message msgDec = (Message) EncodingHelper.decode(
        msg.getEncodableClassId(), bytes);

    Assert.assertEquals(msg.order, msgDec.order);
    Assert.assertEquals(msg.getFullMessage().id, msgDec.getFullMessage().id);
    Assert.assertEquals(msg.getFullMessage().toId, msgDec.getFullMessage().toId);
    Assert.assertEquals(msg.getFullMessage().toName, msgDec.getFullMessage().toName);
    Assert.assertEquals(msg.getFullMessage().toType, msgDec.getFullMessage().toType);
    Assert.assertEquals(msg.getFullMessage().timestamp, msgDec.getFullMessage().timestamp);
    Assert.assertEquals(msg.getFullMessage().compressed, msgDec.getFullMessage().compressed);
    Assert.assertEquals(msg.getFullMessage().deliveryTime, msgDec.getFullMessage().deliveryTime);
    
    Assert.assertEquals(msg.getFullMessage().type, msgDec.getFullMessage().type);
    Assert.assertEquals(msg.getFullMessage().replyToId, msgDec.getFullMessage().replyToId);
    Assert.assertEquals(msg.getFullMessage().replyToName, msgDec.getFullMessage().replyToName);
    Assert.assertEquals(msg.getFullMessage().replyToId, msgDec.getFullMessage().replyToId);
    Assert.assertEquals(msg.getFullMessage().priority, msgDec.getFullMessage().priority);
    Assert.assertEquals(msg.getFullMessage().expiration, msgDec.getFullMessage().expiration);
    Assert.assertEquals(msg.getFullMessage().correlationId, msgDec.getFullMessage().correlationId);
    Assert.assertEquals(msg.getFullMessage().deliveryCount, msgDec.getFullMessage().deliveryCount);
    Assert.assertEquals(msg.getFullMessage().jmsType, msgDec.getFullMessage().jmsType);
    Assert.assertEquals(msg.getFullMessage().redelivered, msgDec.getFullMessage().redelivered);
    Assert.assertEquals(msg.getFullMessage().persistent, msgDec.getFullMessage().persistent);
    
    if (msg.getFullMessage().properties != null) {
      Assert.assertEquals(msg.getFullMessage().properties.size(), msgDec.getFullMessage().properties.size());
      Assert.assertEquals(msg.getFullMessage().properties.get(PROPERTY1), msgDec.getFullMessage().properties.get(PROPERTY1));
      Assert.assertEquals(msg.getFullMessage().properties.get(PROPERTY2), msgDec.getFullMessage().properties.get(PROPERTY2));
      Assert.assertEquals(msg.getFullMessage().properties.get(PROPERTY3), msgDec.getFullMessage().properties.get(PROPERTY3));
      Assert.assertEquals(msg.getFullMessage().properties.get(PROPERTY4), msgDec.getFullMessage().properties.get(PROPERTY4));
    }
    
    if (msg.getFullMessage().body != null) {
      Assert.assertEquals(msg.getFullMessage().body.length, msgDec.getFullMessage().body.length);
    } else {
      Assert.assertEquals(null, msgDec.getFullMessage().body);
    }
    Assert.assertEquals(msg.getFullMessage().clientID, msgDec.getFullMessage().clientID);

  }

}
