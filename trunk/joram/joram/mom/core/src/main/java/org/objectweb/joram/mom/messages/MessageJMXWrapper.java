/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2009 - 2010 ScalAgent Distributed Technologies
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
import java.util.List;
import java.util.Map;

import javax.management.openmbean.ArrayType;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;

import org.objectweb.joram.mom.util.MessageIdList;

import fr.dyade.aaa.common.stream.Properties;

public class MessageJMXWrapper {
  private final static String[] itemNames = { "order", "identifier", "priority", "timestamp", "expiration", "properties" };

  private final static String[] itemDescs = { "order", "identifier", "priority", "timestamp", "expiration", "properties" };

  private static OpenType[] itemTypes;

  private static CompositeType rowType;
 
  static {
    try {
      ArrayType a1 = new ArrayType(1, SimpleType.STRING);

      itemTypes = new OpenType[] { SimpleType.LONG, SimpleType.STRING, SimpleType.INTEGER, SimpleType.LONG, SimpleType.LONG,  a1};
      rowType = new CompositeType("Message", "Message", itemNames, itemDescs, itemTypes);
    } catch (OpenDataException exc) {
      // Should never happened
    }
  }
  
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
    String[] props = null;

    Properties properties = msg.getHeaderMessage().properties;
    if (properties != null) {
      props = new String[properties.size()];
      int i = 0;
      StringBuffer strbuf = new StringBuffer();
      for (Enumeration e = properties.keys(); e.hasMoreElements();) {
        String key = (String) e.nextElement();
        strbuf.append(key).append('=').append(properties.get(key));
        props[i++] = strbuf.toString();
        strbuf.setLength(0);
      }
    }
    
    CompositeDataSupport cds = new CompositeDataSupport(rowType, itemNames,
                                                        new Object[] {
                                                                      new Long(msg.order),
                                                                      msg.getId(),
                                                                      new Integer(msg.getPriority()),
                                                                      new Long(msg.getTimestamp()),
                                                                      new Long(msg.getExpiration()),
                                                                      props
                                                                      });
    return cds;
  }

  /**
   * Returns the description of a vector of messages.
   * 
   * @param messages The vector of messages to describe.
   * @return The messages description.
   * 
   * @throws Exception
   */
  public static TabularData createTabularDataSupport(List messages) throws Exception {
    String[] id = { "identifier" };
    TabularDataSupport tds = new TabularDataSupport(new TabularType("Messages", "Messages", rowType, id));

    for (int i=0; i<messages.size(); i++) {
      Message message = (Message) messages.get(i);
      tds.put(MessageJMXWrapper.createCompositeDataSupport(message));
    }
    return tds;
  }

  /**
   * Returns the description of a subset of an hashtable of messages.
   * 
   * @param messages The hashtable of messages to describe.
   * @return The messages description.
   * 
   * @throws Exception
   */
  public static TabularData createTabularDataSupport(Map messages, MessageIdList ids) throws Exception {
    String[] id = { "identifier" };
    TabularDataSupport tds = new TabularDataSupport(new TabularType("Messages", "Messages", rowType, id));

    for (int i=0; i<ids.size(); i++) {
      Message message = (Message) messages.get(ids.get(i));
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
