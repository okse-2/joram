/*
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
 *
 * The contents of this file are subject to the Joram Public License,
 * as defined by the file JORAM_LICENSE.TXT 
 * 
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License on the Objectweb web site
 * (www.objectweb.org). 
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific terms governing rights and limitations under the License. 
 * 
 * The Original Code is Joram, including the java packages fr.dyade.aaa.agent,
 * fr.dyade.aaa.util, fr.dyade.aaa.ip, fr.dyade.aaa.mom, and fr.dyade.aaa.joram,
 * released May 24, 2000. 
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
 */


package fr.dyade.aaa.joram;

import java.util.*;
import java.io.*;
import java.net.MalformedURLException;

/**
 * The component URL structure. 
 */
public class CURL implements Serializable {
    private static final String PROTO = "joram";
    private static final String SEP = "/";

    private String host;
    private int port;
    private String agentId = null;
   
    /**
     * @param host the hostname.
     * @param port the listening port.
     * @param agentId agent in AAAMOM.
     */
    public CURL(String host,int port,String agentId) {
	this.host = host;
	this.port = port;
	this.agentId = agentId;
    }
    public CURL(String url) throws Exception {
	int i = url.indexOf("://");
	if(i == -1)
	    throw new MalformedURLException(url);
	String toparse = url.substring(i + 3);
	StringTokenizer token = new StringTokenizer(toparse,":"+SEP,false);
	host = token.nextToken();
	port = Integer.valueOf(token.nextToken()).intValue();
	if(token.hasMoreTokens()) agentId = token.nextToken();
    }

    /**
     * returns the URL as a String which format is:
     * joram://<host>:<port>/<agentId>
     */
    public String toString() { 
	return PROTO + ":" + SEP + SEP + host + ":" + port + SEP + agentId;
    }

    public String getProtocol() {
	return PROTO;
    }
    public String getHost() {
	return host;
    }
    
    public int getPort() {
	return port;
    }

    public String getAgentId() {
	return agentId;
    }

    public CURL getConfigURL() {
	return new CURL(host,port,agentId);
    }

    public boolean equals(Object obj) {
	return (obj instanceof CURL) && obj.toString().equals(toString());
    }

}
