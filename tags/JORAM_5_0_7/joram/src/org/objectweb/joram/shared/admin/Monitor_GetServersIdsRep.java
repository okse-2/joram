/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - 2007 ScalAgent Distributed Technologies
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
package org.objectweb.joram.shared.admin;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.objectweb.joram.shared.stream.StreamUtil;

/**
 * A <code>Monitor_GetServersIdsRep</code> instance holds the list of the
 * platform's servers' identifiers.
 */
public class Monitor_GetServersIdsRep extends Monitor_Reply {
  private static final long serialVersionUID = 1L;

  /** Servers identifiers. */
  private int[] ids;

  private String[] names;

  private String[] hostNames;

  /**
   * Constructs a <code>Monitor_GetServersRep</code> instance.
   */
  public Monitor_GetServersIdsRep(int[] ids,
                                  String[] names,
                                  String[] hostNames) {
    this.ids = ids;
    this.names = names;
    this.hostNames = hostNames;
  }
  
  public Monitor_GetServersIdsRep() { }

  /** Returns the servers' identifiers. */
  public final int[] getIds() {
    return ids;
  }

  public final String[] getNames() {
    return names;
  }

  public final String[] getHostNames() {
    return hostNames;
  }
  
  protected int getClassId() {
    return MONITOR_GET_SERVERS_IDS_REP;
  }
  
  public void readFrom(InputStream is) throws IOException {
    super.readFrom(is);
    ids = StreamUtil.readArrayOfIntFrom(is);
    names = StreamUtil.readArrayOfStringFrom(is);
    hostNames = StreamUtil.readArrayOfStringFrom(is);
  }

  public void writeTo(OutputStream os) throws IOException {
    super.writeTo(os);
    StreamUtil.writeArrayOfIntTo(ids, os);
    StreamUtil.writeArrayOfStringTo(names, os);
    StreamUtil.writeArrayOfStringTo(hostNames, os);
  }
}
