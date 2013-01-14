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
 * Initial developer(s): NicoScalAgent Distributed Technologies
 * Contributor(s):
 */
package org.objectweb.joram.shared.admin;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import fr.dyade.aaa.common.stream.StreamUtil;

/**
 * An <code>SpecialAdmin</code> is a request sent by a client administrator
 * inside a <code>org.objectweb.joram.shared.messages.Message</code> to an
 * <code>org.objectweb.joram.mom.dest.AdminTopic</code> topic for requesting an
 * administration operation which will occur on the specified destination.
 */
public abstract class DestinationAdminRequest extends AdminRequest {

  private static final long serialVersionUID = 1L;

  /** Identifier of the destination. */
  private String destId;
  
  public DestinationAdminRequest(String destId) {
    this.destId = destId;
  }

  public DestinationAdminRequest() { }
  
  /** Returns the identifier of the destination. */
  public String getDestId() {
    return destId;
  }

  public void readFrom(InputStream is) throws IOException {
    destId = StreamUtil.readStringFrom(is);
  }

  public void writeTo(OutputStream os) throws IOException {
    StreamUtil.writeTo(destId, os);    
  }
}
