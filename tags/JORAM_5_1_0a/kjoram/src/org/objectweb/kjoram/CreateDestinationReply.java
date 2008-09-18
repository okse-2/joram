/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 ScalAgent Distributed Technologies
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
package org.objectweb.kjoram;

import java.io.IOException;

/**
 * A <code>CreateDestinationReply</code> instance replies to a
 * destination creation request, produced by the AdminTopic.
 */
public class CreateDestinationReply extends AdminReply {

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

  public void toString(StringBuffer strbuf) {
    strbuf.append('(');
    strbuf.append(",id=").append(id); 
    strbuf.append(",name=").append(name); 
    strbuf.append(",type=").append(type).append(')');
  }
  
  protected int getClassId() {
    return CREATE_DESTINATION_REPLY;
  }
  
  public void readFrom(InputXStream is) throws IOException {
    super.readFrom(is);
    id = is.readString();
    name = is.readString();
    type = is.readString();
  }

  public void writeTo(OutputXStream os) throws IOException {
    super.writeTo(os);
    os.writeString(id);
    os.writeString(name);
    os.writeString(type);
  }
}
