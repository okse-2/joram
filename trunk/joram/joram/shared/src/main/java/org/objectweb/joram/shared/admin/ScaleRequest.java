package org.objectweb.joram.shared.admin;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

import fr.dyade.aaa.common.stream.StreamUtil;

/**
 * Class to gather all scaling requests
 * 
 * @author Ahmed El Rheddane
 *
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
