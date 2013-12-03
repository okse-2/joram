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
import org.objectweb.joram.mom.util.MessageIdListImpl;

public class MessageIdListImplEncodingTest {

  @Test
  public void run() throws Exception {
    EncodingHelper.init();

    MessageIdListImpl list = new MessageIdListImpl("testList");
    
    for (int i = 0; i < 3; i++) {
      list.add("msg" + i, true);
    }

    checkEncoding(list);
  }

  private void checkEncoding(MessageIdListImpl list) throws Exception {
    byte[] bytes = EncodingHelper.encode(list);

    MessageIdListImpl listDec = (MessageIdListImpl) EncodingHelper.decode(
        list.getEncodableClassId(), bytes);

    Assert.assertEquals(list.size(), listDec.size());
    for (int i = 0; i < list.size(); i++) {
      Assert.assertEquals(list.get(i), listDec.get(i));
    }
  }

}
