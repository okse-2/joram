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

import javax.jms.*;
import java.util.*;
import java.net.*;
import java.io.*;
import fr.dyade.aaa.mom.*;

/**
 * <code>XAConnection</code> extends the capability of
 * <code>Connection</code> by providing
 * an <code>XASession</code>.
 *
 * @author Laurent Chauvirey
 * @version 1.0
 */

public abstract class XAConnection extends Connection implements javax.jms.XAConnection, javax.jms.Connection {

    private static final int STOP = 0;
    private static final int START = 1;
    
    /**
     * Create a new <code>XAConnection</code> with default user identity.
     * @param agentClient the name of the JMS proxy agent
     * @param proxyAddress the address of the JMS proxy agent
     * @param proxyPort the port the JMS proxy agent is listening on
     */
    public XAConnection(String agentClient,
			InetAddress proxyAddress, int proxyPort) throws JMSException {
	super(agentClient, proxyAddress, proxyPort, "anonymous", "anonymous");
	//init(agentClient, proxyAddress, proxyPort);
    }

    /**
     * Create a new <code>XAConnection</code> with specified user identity.<br>
     * Currently, there is no user authentication.
     * @param agentClient the name of the JMS proxy agent
     * @param proxyAddress the address of the JMS proxy agent
     * @param proxyPort the port the JMS proxy agent is listening on
     * @param userName the user login
     * @param password the user password
     */
    public XAConnection(String agentClient,
			InetAddress proxyAddress, int proxyPort,
			String userName, String password) throws JMSException {
	super(agentClient, proxyAddress, proxyPort, userName, password);
	//init(agentClient, proxyAddress, proxyPort);
    }

    /**
     * Common method used by constructors.
     */
//     private void init(String agentClient,
// 		      InetAddress proxyAddress,
// 		      int proxyPort) throws JMSException {
// 	this.proxyAddress = proxyAddress;
// 	this.proxyPort = proxyPort;
// 	this.agentClient = agentClient;

// 	messageCounter = 0;
// 	sessionCounter = 0;
// 	waitThreadTable = new Hashtable();
// 	messageJMSMOMTable = new Hashtable();

// 	started = false;
// 	isClosed = false;
	
// 	try {
// 	    socketClientProxy = new Socket(proxyAddress, proxyPort);
// 	    socketClientProxy.setTcpNoDelay(true);
// 	    socketClientProxy.setSoTimeout(0);
// 	    socketClientProxy.setSoLinger(true, 1000);

// 	    /* FIXME : différence par rapport à Connection */
// 	    oos = new ObjectOutputStream(socketClientProxy.getOutputStream());
// 	    ois = new ObjectInputStream(new BufferedInputStream(socketClientProxy.getInputStream()));
// 	    oos.writeObject(agentClient);
// 	    oos.flush();

// 	    driver = new Driver(this, ois);
// 	} catch (IOException ioe) {
// 	    JMSException jmse = new JMSException("Internal error");
// 	    jmse.setLinkedException(ioe);
// 	    throw jmse;
// 	}	
//     }

} // XAConnection
