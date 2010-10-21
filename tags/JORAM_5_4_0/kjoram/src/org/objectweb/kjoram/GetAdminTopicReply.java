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
 * A <code>GetAdminTopicReply</code> replies to a <code>CnxCloseRequest</code>.
 */
public final class GetAdminTopicReply extends AbstractReply {
  /** Identifier of the admin topic. */
  private String id;

  /** Sets the identifier of the admin topic. */
  public void setId(String id) {
    this.id = id;
  }

  /** Returns the identifier of the admin topic. */
  public String getId() {
    return id;
  }

  /**
   * Constructs a <code>GetAdminTopicReply</code> instance.
   *
   * @param request  The <code>GetAdminTopicRequest</code> being answered.
   * @param id  The identifier of the admin topic.
   */
  public GetAdminTopicReply(GetAdminTopicRequest request, String id) {
    super(request.getRequestId());
    this.id = id;
  }

  protected int getClassId() {
    return GET_ADMIN_TOPIC_REPLY;
  }

  /**
   * Constructs a <code>GetAdminTopicReply</code> instance.
   */
  public GetAdminTopicReply() {}

  public void toString(StringBuffer strbuf) {
    super.toString(strbuf);
    strbuf.append(",id=").append(id);
    strbuf.append(')');
  }

  /* ***** ***** ***** ***** *****
   * Streamable interface
   * ***** ***** ***** ***** ***** */

  /**
   *  The object implements the writeTo method to write its contents to
   * the output stream.
   *
   * @param os the stream to write the object to
   */
  public void writeTo(OutputXStream os) throws IOException {
    super.writeTo(os);
    os.writeString(id);
  }

  /**
   *  The object implements the readFrom method to restore its contents from
   * the input stream.
   *
   * @param is the stream to read data from in order to restore the object
   */
  public void readFrom(InputXStream is) throws IOException {
    super.readFrom(is);
    id = is.readString();
  }
}
