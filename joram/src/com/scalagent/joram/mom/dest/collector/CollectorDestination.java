/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 - 2010 ScalAgent Distributed Technologies
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
package com.scalagent.joram.mom.dest.collector;

import fr.dyade.aaa.common.stream.Properties;

/**
 * 
 */
public interface CollectorDestination {
  
  public static final String DEFAULT_COLLECTOR = "com.scalagent.joram.mom.dest.collector.URLCollector";
  public static final String CLASS_NAME = "collector.className";
  public static final String PERSISTENT_MSG = "persistent";
  public static final String EXPIRATION_MSG = "expiration";
  public static final String PRIORITY_MSG = "priority";
  
  /**
   * send message.
   * 
   * @param type message type.
   * @param body message body.
   * @param properties message properties.
   */
  public void sendMessage(int type, byte[] body, Properties properties);
}
