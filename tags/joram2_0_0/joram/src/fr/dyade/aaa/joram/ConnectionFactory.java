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
 
import java.lang.*;
import java.io.*;
import java.net.*; 
  
/** 
  * @author      Nicolas Tachker
  * 
  * @see         javax.jms.QueueConnectionFactory 
  * @see         javax.jms.TopicConnectionFactory
  */ 
 
 
public abstract class ConnectionFactory implements javax.jms.ConnectionFactory, javax.naming.Referenceable, java.io.Serializable {
  
    /** - addrProxy : host address where is TCP Proxy */ 
    protected InetAddress addrProxy;  
    
    /** - portProxy	: lisent port TCP Proxy */ 
    protected int portProxy; 
    
    /** the name of the agentClient in the mom */
    protected String agentClient; 
    
    /* the security reference */	 
    protected String login = "anonymous"; 
    protected String passwd = "anonymous";
    
    /** URL for Joram naming */
    CURL url = null;

    // The static table of all ConnectionFactory (jndi)
    private static java.util.Hashtable cfList = new java.util.Hashtable();

    public ConnectionFactory(String agentClient, InetAddress addrProxy, int portProxy) { 
	this.agentClient = agentClient;
	this.addrProxy = addrProxy;
	this.portProxy = portProxy;
	try {
	    url = new CURL(addrProxy.getHostName(),portProxy,agentClient);
	} catch (Exception e) {
	    System.out.println("Jms ConnectionFactory");
	    e.printStackTrace();
	}
    }
    
    /** call ConnectionFactory like
     * ConnectionFactory("joram://host:port/#x.y.z")
     */
    public ConnectionFactory(String stringURL) {
	try {
	    url = new CURL(stringURL);
	    if (Debug.debug)
		if (Debug.admin)
		    System.out.println("->ConnectionFactory (Protocol=" + url.getProtocol() +
				       ", Host=" + url.getHost() +
				       ", Port=" + url.getPort() +
				       ", AgentId=" + url.getAgentId() + ")");
	    
	    if ( url.getProtocol().equals("joram") ) {
 		this.agentClient = url.getAgentId();
		this.addrProxy = InetAddress.getByName(url.getHost());
		this.portProxy = url.getPort();
	    }
	} catch (Exception e) {
	    System.out.println("ConnectionFactory Exception");
	    e.printStackTrace();
	}
    }

    /** get AgentID string */
    public String getAgentClient() {
	return agentClient;
    }

    /** comes from the javax.jndi.referenceable interface */
    public javax.naming.Reference getReference() throws javax.naming.NamingException{
	javax.naming.Reference ref =  new javax.naming.Reference(this.getClass().getName(),
					  "fr.dyade.aaa.joram.ObjectConnectionFactory",
					  null);
	ref.add(new javax.naming.StringRefAddr("cnxfactory.joramURL", url.toString()));
	return ref;
    }

    // use for jndi
    public void setConnectionFactoryList(String s) {
	cfList.put(s,this);
    }
    public static Object getConnectionFactory(String s) {
	return cfList.get(s);
    }

    public String toString() {
	return url.toString();
    }
}
 
