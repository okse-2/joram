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
 * A <code>Monitor_GetDestinationsRep</code> instance replies to a get
 * destinations monitoring request, and holds the destinations on a given
 * server.
 */
public class Monitor_GetDestinationsRep extends Monitor_Reply {
  private static final long serialVersionUID = 1L;

  private String[] ids;
  private String[] names;
  private String[] types;

  public Monitor_GetDestinationsRep(
    String[] ids,
    String[] names,
    String[] types) {
    this.ids = ids;
    this.names = names;
    this.types = types;
  }

  public Monitor_GetDestinationsRep() { }
  
  public String[] getIds() {
    return ids;
  }

  public String[] getNames() {
    return names;
  }

  public String[] getTypes() {
    return types;
  }
  
  protected int getClassId() {
    return MONITOR_GET_DESTINATIONS_REP;
  }
  
  public void readFrom(InputStream is) throws IOException {
    super.readFrom(is);
    ids = StreamUtil.readArrayOfStringFrom(is);
    names = StreamUtil.readArrayOfStringFrom(is);
    types = StreamUtil.readArrayOfStringFrom(is);
  }

  public void writeTo(OutputStream os) throws IOException {
    super.writeTo(os);
    StreamUtil.writeArrayOfStringTo(ids, os);
    StreamUtil.writeArrayOfStringTo(names, os);
    StreamUtil.writeArrayOfStringTo(types, os);
  }
}
