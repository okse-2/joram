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

import java.io.*;
import java.net.*;
import javax.jms.*;

/**
 * XAConnectionFactory allows an application program to create new
 * XAConnections to a JMS proxy.
 *
 * @author Laurent Chauvirey
 * @version 1.0
 */

public abstract class XAConnectionFactory implements javax.jms.XAConnectionFactory,javax.naming.Referenceable, Serializable {
    
    protected String proxyAgentIdString;
    protected InetAddress proxyAddress;
    protected int proxyPort;

    protected CURL proxyAgentURL;

    protected String login = "anonymous";
    protected String passwd = "anonymous";

    // The static table of all XAConnectionFactory (jndi)
    private static java.util.Hashtable xacfList = new java.util.Hashtable();

    /**
     * Creates a new XAConnectionFactory.
     * @param proxyAgentIdString the name of the JMS proxy agent
     * @param proxyAddress the address of the JMS proxy agent
     * @param proxyPort the port the JMS proxy agent is listening on
     */
    public XAConnectionFactory(String proxyAgentIdString,
			       InetAddress proxyAddress, int proxyPort) {
	this.proxyAgentIdString = proxyAgentIdString;
	this.proxyAddress = proxyAddress;
	this.proxyPort = proxyPort;
	
	try {
	    proxyAgentURL = new CURL(proxyAddress.getHostName(),
				     proxyPort,
				     proxyAgentIdString);
	} catch (Exception e) {
	    System.out.println("XAConnectionFactory error");
	    e.printStackTrace();
	}
    }

    /**
     * Creates a new XAConnectionFactory.
     * @param proxyAgentURLString the URL string of the JMS proxy agent
     * (<em>joram://host:port/#x.y.z</em>)
     */
    public XAConnectionFactory(String proxyAgentURLString) {
	try {
	    proxyAgentURL = new CURL(proxyAgentURLString);
	    proxyAgentIdString = proxyAgentURL.getAgentId();
	    proxyAddress = InetAddress.getByName(proxyAgentURL.getHost());
	    proxyPort = proxyAgentURL.getPort();
	} catch (Exception e) {
	    System.out.println("XAConnectionFactory error");
	    e.printStackTrace();
	}
    }

    /**
     * Let this object be referenced through JNDI.
     */
    public javax.naming.Reference getReference() throws javax.naming.NamingException {
	javax.naming.Reference ref =  new javax.naming.Reference(this.getClass().getName(),
								 "fr.dyade.aaa.joram.ObjectConnectionFactory",
								 null);
	ref.add(new javax.naming.StringRefAddr("xacnxfactory.joramURL", proxyAgentURL.toString()));
	return ref;
    }

    // use for jndi
    public void setXAConnectionFactoryList(String s) {
	xacfList.put(s,this);
    }
    public static Object getXAConnectionFactory(String s) {
	return xacfList.get(s);
    }

    /**
     * Returns a string representation of this XAConnectionFactory,
     * actually the URL of the proxy agent.
     */
    public String toString() {
	return proxyAgentURL.toString();
    }

} // XAConnectionFactory
