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

import java.net.*;
import javax.jms.*;

/**
 * XAQueueConnection provides the same create options as QueueConnection
 * (optional). The only difference is that an XAConnection is by definition
 * transacted.
 *
 * @author Laurent Chauvirey
 * @version 1.0
 */

public class XAQueueConnection extends XAConnection implements javax.jms.XAQueueConnection {
    
    /**
     * Construct an <code>XAQueueConnection</code>. The difference with
     * <code>Connection</code> is that a <code>XAConnection</code>
     * is by definition transacted.
     */
    public XAQueueConnection(String proxyAgentIdString,
			     InetAddress proxyAddress, int proxyPort,
			     String login, String passwd) throws javax.jms.JMSException {
	super(proxyAgentIdString, proxyAddress, proxyPort, login, passwd);
    }

    /**
     * Create an <code>XAQueueSession</code>.
     */
    public javax.jms.XAQueueSession createXAQueueSession() throws JMSException {
	long sessionID = getNextSessionID();
	return new XAQueueSession(sessionID, this);
    }

    /**
     * Create a <code>QueueSession</code>.
     */
    public javax.jms.QueueSession createQueueSession(boolean transacted,
						     int acknowledgeMode) throws JMSException {
	throw new JMSException("Not implemented");
    }

    /*
     * Create a connection consumer for this connection. This is an expert
     * facility not used by regular JMS clients.
     */
    public javax.jms.ConnectionConsumer createConnectionConsumer(javax.jms.Queue queue,
								 String messageSelector,
								 javax.jms.ServerSessionPool sessionPool,
								 int maxMessages) throws JMSException {
	throw new JMSException("Not implemented");
    }

} // XAQueueConnection
