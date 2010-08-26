/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2005 - 2006 ScalAgent Distributed Technologies
 * Copyright (C) 2003 - ScalAgent Distributed Technologies
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
package org.objectweb.joram.mom.notifications;

import org.objectweb.joram.shared.admin.SpecialAdmin;

/**
 * A <code>SpecialAdminRequest</code> instance is used by a destination agent
 * to do special administration.
 */
public class SpecialAdminRequest extends AdminRequest {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private SpecialAdmin request;

  /**
   * Constructs a <code>SpecialAdminRequest</code> instance.
   *
   * @param id  Identifier of the request, may be null.
   * @param request SpecialAdmin
   */
  public SpecialAdminRequest(String id, SpecialAdmin request) {
    super(id);
    this.request = request;
  }

  /** Returns the SpecialAdmin request */
  public SpecialAdmin getRequest() {
    return request;
  }

  /**
   * Appends a string image for this object to the StringBuffer parameter.
   *
   * @param output
   *	buffer to fill in
   * @return
	<code>output</code> buffer is returned
   */
  public StringBuffer toString(StringBuffer output) {
    output.append('(');
    super.toString(output);
    output.append(", request=").append(request);
    output.append(')');

    return output;
  }
} 
