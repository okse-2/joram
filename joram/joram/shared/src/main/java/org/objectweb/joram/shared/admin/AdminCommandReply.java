/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2010 ScalAgent Distributed Technologies
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
package org.objectweb.joram.shared.admin;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import fr.dyade.aaa.common.stream.StreamUtil;

public class AdminCommandReply extends AdminReply {
	private static final long serialVersionUID = 1L;
	
	private Properties prop = null;
	
	public AdminCommandReply() { }
	
	/**
   * Constructs an <code>AdminCommandReply</code> instance.
   *
   * @param success  <code>true</code> if this reply replies to a successful request.
   * @param info  Information to carry.
   * @param prop  the properties (may be null).
   */
	public AdminCommandReply(boolean success, String info, Properties prop) {
		super(success, info);
		this.prop = prop;
	}

	/**
   * @return the properties
   */
  public Properties getProp() {
  	return prop;
  }

	/* (non-Javadoc)
	 * @see org.objectweb.joram.shared.admin.AbstractAdminMessage#getClassId()
	 */
	protected int getClassId() {
    return CMD_ADMIN_REPLY;
  }
	
	/* (non-Javadoc)
	 * @see fr.dyade.aaa.common.stream.Streamable#writeTo(java.io.OutputStream)
	 */
	public void writeTo(OutputStream os) throws IOException {
		super.writeTo(os);
		StreamUtil.writeTo(prop, os);
	}

	/* (non-Javadoc)
	 * @see fr.dyade.aaa.common.stream.Streamable#readFrom(java.io.InputStream)
	 */
	public void readFrom(InputStream is) throws IOException {
		super.readFrom(is);
		prop = StreamUtil.readJPropertiesFrom(is);
	}
}
