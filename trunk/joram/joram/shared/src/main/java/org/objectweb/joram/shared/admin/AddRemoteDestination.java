/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2012 ScalAgent Distributed Technologies
 * Copyright (C) 2012 Universite Joseph Fourier
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
 * Contributor(s): Ahmed El Rheddane
 */
package org.objectweb.joram.shared.admin;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import fr.dyade.aaa.common.stream.StreamUtil;

public class AddRemoteDestination extends DestinationAdminRequest {
  /** define serialVersionUID for interoperability */
	private static final long serialVersionUID = 1L;

	private String newId;

	/**
	 * Adds a destination to a cluster.
	 * <p>
	 * 
	 * @param clusteredDest Destination part of the cluster.
	 * @param addedDest Destination joining the cluster.
	 */
	public AddRemoteDestination(String queueId, String newId) {
		super(queueId);
		this.newId = newId;
	}

	public AddRemoteDestination() { }

	protected int getClassId() {
		return ADD_REMOTE_DEST;
	}

	/**
	 * @return the joiningDest
	 */
	public String getNewId() {
		return newId;
	}

	public void readFrom(InputStream is) throws IOException {
		super.readFrom(is);
		newId = StreamUtil.readStringFrom(is);
	}

	public void writeTo(OutputStream os) throws IOException {
		super.writeTo(os);
		StreamUtil.writeTo(newId, os);
	}
}
