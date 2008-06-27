/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2007 ScalAgent Distributed Technologies
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
package org.objectweb.joram.shared.admin;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.objectweb.joram.shared.stream.StreamUtil;

/**
 * A <code>CreateDestinationReply</code> instance replies to a
 * destination creation request, produced by the AdminTopic.
 */
public class CreateDestinationReply extends AdminReply {
  private static final long serialVersionUID = 1L;

  /** Identifier of the created destination. */
  private String id;

  private String name;

  private String type;

  /**
   * Constructs a <code>CreateDestinationReply</code> instance.
   *
   * @param id  The id of the created destination.
   * @param info  Related information.
   */
  public CreateDestinationReply(
    String id, 
    String name,
    String type,
    String info) {
    super(true, info);
    this.id = id;
    this.name = name;
    this.type = type;
  }

  public CreateDestinationReply() { }
  
  /** Returns the id of the created queue. */
  public final String getId() {
    return id;
  }

  public final String getName() {
    return name;
  }

  public final String getType() {
    return type;
  }

  public String toString() {
    return '(' + super.toString() +
      ",id=" + id + 
      ",name=" + name + 
      ",type=" + type + ')';
  }
  
  protected int getClassId() {
    return CREATE_DESTINATION_REPLY;
  }
  
  public void readFrom(InputStream is) throws IOException {
    super.readFrom(is);
    id = StreamUtil.readStringFrom(is);
    name = StreamUtil.readStringFrom(is);
    type = StreamUtil.readStringFrom(is);
  }

  public void writeTo(OutputStream os) throws IOException {
    super.writeTo(os);
    StreamUtil.writeTo(id, os);
    StreamUtil.writeTo(name, os);
    StreamUtil.writeTo(type, os);
  }
}
