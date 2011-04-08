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

public class AdminCommandRequest extends AdminRequest {

	/** the string identifier of the target. */
  private String targetId;
  /** the command. */
	private int command;
	/** the properties. */
	private Properties prop;

	public AdminCommandRequest() {
	}
	
	/**
	 * @param targetId  Identifier (agentId) of the target.
	 * @param command	the command to execute.
	 * @param prop the properties
	 */
	public AdminCommandRequest(String targetId, int command, Properties prop) {
		this.targetId = targetId;
		this.command = command;
		this.prop = prop;
	}
  
	/**
	 * @return the command.
	 */
	public int getCommand() {
  	return command;
  }

	/**
	 * @return the properties
	 */
	public Properties getProp() {
  	return prop;
  }
	
	/** Returns the string identifier of the target. */
  public String getTargetId() {
    return targetId;
  }
	
	/* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString() {
  	StringBuffer buff = new StringBuffer();
  	buff.append("AdminCommandRequest(");
  	buff.append("targetId=");
  	buff.append(targetId);
  	buff.append(", command=");
  	buff.append(AdminCommandConstant.commandNames[command]);
  	buff.append(", prop=");
  	buff.append(prop);
  	buff.append(')');
  	return buff.toString();
  }

	/* (non-Javadoc)
	 * @see org.objectweb.joram.shared.admin.AbstractAdminMessage#getClassId()
	 */
	protected int getClassId() {
    return CMD_ADMIN_REQUEST;
  }
	
	/* (non-Javadoc)
	 * @see fr.dyade.aaa.common.stream.Streamable#writeTo(java.io.OutputStream)
	 */
	public void writeTo(OutputStream os) throws IOException {
		StreamUtil.writeTo(targetId, os);
		StreamUtil.writeTo(command, os);
		StreamUtil.writeTo(prop, os);
	}

	/* (non-Javadoc)
	 * @see fr.dyade.aaa.common.stream.Streamable#readFrom(java.io.InputStream)
	 */
	public void readFrom(InputStream is) throws IOException {
		targetId = StreamUtil.readStringFrom(is);
		command = StreamUtil.readIntFrom(is);
		prop = StreamUtil.readJPropertiesFrom(is);
	}
}
