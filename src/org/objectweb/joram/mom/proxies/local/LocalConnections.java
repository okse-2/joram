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
 */
package org.objectweb.joram.mom.proxies.local;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.joram.client.jms.local.LocalRequestChannel;
import org.objectweb.joram.mom.proxies.ConnectionManager;

/**
 * Class used to check off local connections.
 */
public class LocalConnections implements LocalConnectionsMBean {
  
  private static final String MBEAN_NAME = "type=Connection,mode=local";

  /** Unique LocalConnections instance. */
  private static LocalConnections currentInstance;
  
  /** Lists the opened local connections. */
  private List connections = new ArrayList();

  private boolean activated = true;

  public void activate() {
    activated = true;
  }

  public void closeAllConnections() {
    LocalRequestChannel[] array = (LocalRequestChannel[]) connections
        .toArray(new LocalRequestChannel[connections.size()]);
    for (int i = 0; i < array.length; i++) {
      array[i].close();
    }
  }

  public void deactivate() {
    activated = false;
  }

  public int getRunningConnectionsCount() {
    return connections.size();
  }

  public boolean isActivated() {
    return activated;
  }

  public void addLocalConnection(LocalRequestChannel localRequestChannel) {
    connections.add(localRequestChannel);
  }
  
  public void removeLocalConnection(LocalRequestChannel localRequestChannel) {
    connections.remove(localRequestChannel);
  }
  
  public static LocalConnections getCurrentInstance() {
    if (currentInstance == null) {
      currentInstance = new LocalConnections();
      ConnectionManager.getCurrentInstance().addManager(currentInstance);
    }
    return currentInstance;
  }

  public String getMBeanName() {
    return MBEAN_NAME;
  }

}
