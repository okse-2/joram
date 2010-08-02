/**
 * (c)2010 Scalagent Distributed Technologies
 */


package com.scalagent.appli.client.command.info;

import java.util.Vector;

import com.scalagent.engine.client.command.Response;

/**
 * Response to the action LoadServerAction 
 * 
 * @author Yohann CINTRE
 */
public class LoadServerInfoResponse implements Response{

	private Vector<Float> infos;

	public LoadServerInfoResponse(){}

	public LoadServerInfoResponse(Vector<Float> infos) {
		this.infos = infos;
	}

	public Vector<Float> getInfos() {
		return infos;
	}

}
