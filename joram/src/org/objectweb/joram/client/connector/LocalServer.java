/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - Bull SA
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
 * Initial developer(s): Frederic Maistre (Bull SA)
 * Contributor(s): Nicolas Tachker (Bull SA)
 */
package org.objectweb.joram.client.connector;

import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;


/**
 * The <code>LocalServer</code> class allows to manage the adapter's
 * underlying server.
 */
public class LocalServer implements LocalServerMBean
{
  /** The wrapped adapter instance. */
  private JoramAdapter adapter;
  
  private String objectName;
  private boolean eventProvider = false;
  private boolean stateManageable = false;
  private boolean statisticsProvider = false;

  /** Constructs a <code>LocalServer</code> instance. */
  public LocalServer(JoramAdapter adapter) {
    this.adapter = adapter;
  }

  public void setObjectName(String objectName) {
    this.objectName = objectName;
  }

  public void setStateManageable(boolean stateManageable) {
    this.stateManageable = stateManageable;
  }

  public void setStatisticsProvider(boolean statisticsProvider) {
    this.statisticsProvider = statisticsProvider;
  }

  public void setEventProvider(boolean eventProvider) {
    this.eventProvider = eventProvider;
  }

  public String getobjectName() {
    return objectName;
  }

  public boolean iseventProvider() {
    return eventProvider;
  }

  public boolean isstateManageable() {
    return stateManageable;
  }

  public boolean isstatisticsProvider() {
    return statisticsProvider;
  }

  public String getPlatformConfiguration()
  {
    if (adapter.platformServersIds != null
        && adapter.platformServersIds.size() > 1)
      return "Distributed";
    else
      return "Centralized";
  }

  public List getPlatformServersIds()
  {
    return adapter.platformServersIds;
  }

  public String getLocalServerId()
  {
    return "" + adapter.serverId;
  }

  public String getRelease()
  {
    return "4.1.0";
  }

  public String getRunningMode()
  {
    if (adapter.getCollocatedServer().booleanValue())
      return "Collocated";
    return "Remote";
  }

  public String getHost()
  {
    return adapter.hostName;
  }

  public String getPort()
  {
    return "" + adapter.serverPort;
  }

  public List getLocalQueuesNames()
  {
    try {
      List list = AdminModule.getDestinations();
      Iterator it = list.iterator();
      Destination dest;
      Vector names = new Vector();
      while (it.hasNext()) {
        dest = (Destination) it.next();
        if (dest instanceof Queue)
          names.add(dest.getAdminName());
      }
      return names;
    }
    catch (Exception exc) {
      return new Vector();
    }
  }

  public List getLocalTopicsNames()
  {
    try {
      List list = AdminModule.getDestinations();
      Iterator it = list.iterator();
      Destination dest;
      Vector names = new Vector();
      while (it.hasNext()) {
        dest = (Destination) it.next();
        if (dest instanceof Topic)
          names.add(dest.getAdminName());
      }
      return names;
    }
    catch (Exception exc) {
      return new Vector();
    }
  }

  public List getLocalUsersNames()
  {
    try {
      List list = AdminModule.getUsers();
      Iterator it = list.iterator();
      User user;
      Vector names = new Vector();
      while (it.hasNext()) {
        user = (User) it.next();
        names.add(user.getName());
      }
      return names;
    }
    catch (Exception exc) {
      return new Vector();
    }
  }


  public void createLocalQueue(String jndiName) throws Exception
  {
    JoramAdapter.createQueue(jndiName);
  }

  public void createLocalTopic(String jndiName) throws Exception
  {
    JoramAdapter.createTopic(jndiName);
  }
}
