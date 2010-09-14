/**
 * (c)2010 Scalagent Distributed Technologies
 */


package com.scalagent.appli.client.command.info;

import com.scalagent.engine.client.command.Response;

/**
 * Response to the action LoadServerAction 
 * 
 * @author Yohann CINTRE
 */
public class LoadServerInfoResponse implements Response{

    private float[] infos;

	public LoadServerInfoResponse(){}

    public LoadServerInfoResponse(float[] infos) {
		this.infos = infos;
	}

    public float[] getInfos() {
		return infos;
	}

}
