/*
 *  JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2013 - 2014 ScalAgent Distributed Technologies
 * Copyright (C) 2013 - 2014 Université Joseph Fourier
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
 * Initial developer(s): Université Joseph Fourier
 * Contributor(s): ScalAgent Distributed Technologies
 */
package org.objectweb.joram.shared.admin;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import fr.dyade.aaa.common.stream.StreamUtil;

/**
 * Class to gather all scaling requests
 * 
 * @author Ahmed El Rheddane
 */
public class ScaleRequest extends DestinationAdminRequest {
	/**
	 * Operation corresponding to the addition of one resource.
	 */
	public static final int SCALE_IN = -1;
	
	/**
	 * Operation corresponding to the removal of one resource.
	 */
	public static final int SCALE_OUT = 1;
	
	/**
	 * Generic balancing operation.
	 */
	public static final int BALANCE = 0;
	
	/** define serialVersionUID for interoperability */
	private static final long serialVersionUID = 1L;
	
	private int op;
	private String param;
	
	/**
	 * Adds a destination to a cluster.
	 * <p>
	 * 
	 * @param clusteredDest Destination part of the cluster.
	 * @param addedDest Destination joining the cluster.
	 */
	public ScaleRequest(String destId, int op, String param) {
		super(destId);
		this.op = op;
		this.param = param;
	}

	public ScaleRequest() { }

	protected int getClassId() {
		return SCALE_REQUEST;
	}

	public int getOperation() {
		return op;
	}
	
	/**
	 * @return the resource to be managed.
	 */
	public String getParameter() {
		return param;
	}
	
	public void readFrom(InputStream is) throws IOException {
		super.readFrom(is);
		op = StreamUtil.readIntFrom(is);
		param = StreamUtil.readStringFrom(is);
	}

	public void writeTo(OutputStream os) throws IOException {
		super.writeTo(os);
		StreamUtil.writeTo(op, os);
		StreamUtil.writeTo(param, os);
	}
}
