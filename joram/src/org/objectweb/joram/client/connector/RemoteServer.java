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

import java.util.Iterator;
import java.util.List;
import java.util.Vector;


/**
 * The <code>RemoteServer</code> class allows to handle remote JORAM servers.
 */
public class RemoteServer implements RemoteServerMBean
{
  /** Remote server identifier. */
  private short id;


  /** Constructs a <code>RemoteServer</code> instance. */
  public RemoteServer(short id)
  {
    this.id = id;
  }


  public String getRemoteServerId()
  {
    return "" + id;
  }


  public List retrieveRemoteQueuesNames() throws Exception
  {
    List list = AdminModule.getDestinations(id);
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

  public List retrieveRemoteTopicsNames() throws Exception
  {
    List list = AdminModule.getDestinations(id);
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
}
