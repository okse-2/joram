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

import javax.management.openmbean.*;

import org.objectweb.joram.shared.messages.Message;

import java.util.Hashtable;
import java.util.Collection;

public class MessageJMXWrapper {

  public final static String[] itemNames = {
    "id", "priority"};

  public final static String[] itemDescs = {
    "xxx", "xxx"};

  public final static OpenType[] itemTypes = {
    SimpleType.STRING, SimpleType.INTEGER};

  public static CompositeDataSupport createCompositeDataSupport(
    Message msg) throws Exception {
    return new CompositeDataSupport(
      new CompositeType("Message",
                        "xxx",
                        itemNames,
                        itemDescs,
                        itemTypes),
      itemNames,
      new Object[]{msg.getIdentifier(),
                   new Integer(msg.getPriority())});
  }
}
