/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - 2010 ScalAgent Distributed Technologies
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

import fr.dyade.aaa.common.stream.StreamUtil;

/**
 * A <code>Monitor_GetDestinationsRep</code> instance replies to a get
 * destinations monitoring request, and holds the destinations on a given
 * server.
 */
public class GetDestinationsReply extends AdminReply {
  private static final long serialVersionUID = 1L;

  private String[] ids;
  private String[] names;
  private byte[] types;

  public GetDestinationsReply(String[] ids, String[] names, byte[] types) {
    super(true, null);
    this.ids = ids;
    this.names = names;
    this.types = types;
  }
  
  public String[] getIds() {
    return ids;
  }

  public String[] getNames() {
    return names;
  }

  public byte[] getTypes() {
    return types;
  }

  public GetDestinationsReply() {}
  
  protected int getClassId() {
    return MONITOR_GET_DESTINATIONS_REP;
  }
  
  public void readFrom(InputStream is) throws IOException {
    super.readFrom(is);
    ids = StreamUtil.readArrayOfStringFrom(is);
    names = StreamUtil.readArrayOfStringFrom(is);
    types = StreamUtil.readByteArrayFrom(is);
  }

  public void writeTo(OutputStream os) throws IOException {
    super.writeTo(os);
    StreamUtil.writeArrayOfStringTo(ids, os);
    StreamUtil.writeArrayOfStringTo(names, os);
    StreamUtil.writeTo(types, os);
  }
}
