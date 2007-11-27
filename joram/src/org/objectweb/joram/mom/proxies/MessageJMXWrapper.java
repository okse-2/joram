/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2004 ScalAgent Distributed Technologies
 * Copyright (C) 2004 - France Telecom R&D
 * Copyright (C) 1996 - 2004 Bull SA
 * Copyright (C) 1996 - 2000 Dyade
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
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s): ScalAgent Distributed Technologies
 */
package org.objectweb.joram.mom.proxies;

import java.util.Enumeration;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;

import org.objectweb.joram.mom.messages.Message;

public class MessageJMXWrapper {

  public final static String[] itemNames = { "id", "priority" };

  public final static String[] itemDescs = { "xxx", "xxx" };

  public final static String[] itemDesc = { "xxx", "xxx" };

  public final static String[] itemDes = { "xxx", "xxx" };

  public final static OpenType[] itemTypes = { SimpleType.STRING, SimpleType.INTEGER };

  private static CompositeType rowType;

  public static CompositeData createCompositeDataSupport(Message msg) throws Exception {
    if (rowType == null) {
      rowType = new CompositeType("Message", "xxx", itemNames, itemDescs, itemTypes);
    }
    return new CompositeDataSupport(rowType, itemNames, new Object[] { msg.getIdentifier(),
        new Integer(msg.getPriority()) });
  }

  public static TabularData createTabularDataSupport(Enumeration messages) throws Exception {
    if (rowType == null) {
      rowType = new CompositeType("Message", "xxx", itemNames, itemDescs, itemTypes);
    }
    CompositeType rowType = new CompositeType("Message", "xxx", itemNames, itemDescs, itemTypes);
    String[] id = { "id" };
    TabularDataSupport tds = new TabularDataSupport(new TabularType("Messages",
        "Id and priority of the messages", rowType, id));

    while (messages.hasMoreElements()) {
      Message message = (Message) messages.nextElement();
      tds.put(MessageJMXWrapper.createCompositeDataSupport(message));
    }
    return tds;
  }
}
