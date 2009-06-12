/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2009 ScalAgent Distributed Technologies
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

import java.util.Enumeration;
import java.util.Properties;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;

public class MessageJMXWrapper {
  private final static String[] itemNames = { "identifier", "priority", "timestamp", "expiration" };

  private final static String[] itemDescs = { "identifier", "priority", "timestamp", "expiration" };

  private final static OpenType[] itemTypes = { SimpleType.STRING, SimpleType.INTEGER, SimpleType.LONG, SimpleType.LONG };

  private static CompositeType rowType;
 
  /**
   * Returns the description of a message.
   * The description includes the type and priority of the message.
   * 
   * @param msg The message to describe.
   * @return    The message description.
   * 
   * @throws Exception
   */
  public static CompositeData createCompositeDataSupport(Message msg) throws Exception {
    if (rowType == null)
      rowType = new CompositeType("Message", "Message", itemNames, itemDescs, itemTypes);

    CompositeDataSupport cds = new CompositeDataSupport(rowType, itemNames,
                                                        new Object[] {
                                                                      msg.getIdentifier(),
                                                                      new Integer(msg.getPriority()),
                                                                      new Long(msg.getTimestamp()),
                                                                      new Long(msg.getExpiration())
                                                                      });
    return cds;
  }

  /**
   * Returns the description of an enumeration of messages.
   * The description includes the type and priority of the message.
   * 
   * @param messages The enumeration of messages to describe.
   * @return The messages description.
   * 
   * @throws Exception
   */
  public static TabularData createTabularDataSupport(Enumeration messages) throws Exception {
    if (rowType == null) {
      rowType = new CompositeType("Message", "xxx", itemNames, itemDescs, itemTypes);
    }

    String[] id = { "identifier" };
    TabularDataSupport tds = new TabularDataSupport(new TabularType("Messages", "Messages", rowType, id));

    while (messages.hasMoreElements()) {
      Message message = (Message) messages.nextElement();
      tds.put(MessageJMXWrapper.createCompositeDataSupport(message));
    }
    return tds;
  }
  
//  public static CompositeData[] createTabularDataSupport(Enumeration messages) throws Exception {
//    if (rowType == null)
//      rowType = new CompositeType("Message", "xxx", itemNames, itemDescs, itemTypes);
//
//    Vector v = new Vector();
//    while (messages.hasMoreElements()) {
//      Message message = (Message) messages.nextElement();
//      v.add(MessageJMXWrapper.createCompositeDataSupport(message));
//    }
//    CompositeData[] cda = new CompositeData[v.size()];
//    return (CompositeData[]) v.toArray(cda);
//  }

}
