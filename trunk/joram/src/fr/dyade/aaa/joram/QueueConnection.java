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
 * A QueueConnection is an active connection to a JMS PTP provider.
 * A client uses a QueueConnection to create one or more QueueSessions
 * for producing and consuming messages.
 *
 * @author Laurent Chauvirey
 * @version 1.0
 */

public class QueueConnection extends Connection implements javax.jms.QueueConnection {

    /**
     * Construct a <code>QueueConnection</code>.
     */
    public QueueConnection(String proxyAgentIdString,
			   InetAddress proxyAddress, int proxyPort,
			   String login, String passwd) throws javax.jms.JMSException {
	super(proxyAgentIdString, proxyAddress, proxyPort, login, passwd);
    }

    /**
     * Create a QueueSession.
     */
    public javax.jms.QueueSession createQueueSession(boolean transacted, int acknowledgeMode) throws JMSException {
	try {
	    long sessionCounterNew = sessionCounter;
	    sessionCounter = calculateMessageID(sessionCounter);
	    fr.dyade.aaa.joram.QueueSession session = new fr.dyade.aaa.joram.QueueSession(transacted, acknowledgeMode, sessionCounter,this);
	    if (session == null) {
		sessionCounter = sessionCounter - 1;
		throw (new fr.dyade.aaa.joram.JMSAAAException("Internal error during creation QueueSession",JMSAAAException.ERROR_CREATION_SESSION));
	    } else {
		return session;
	    }
	} catch (JMSException jmse) {
	    throw(jmse);
	} catch (Exception e) {
	    JMSException jmse = new JMSException("Internal error");
	    jmse.setLinkedException(e);
	    throw(jmse);
	}
    }


    /**
     * Create a connection consumer for this connection (optional operation).
     * This is an expert facility not used by regular JMS clients.
     */
    public javax.jms.ConnectionConsumer createConnectionConsumer(javax.jms.Queue queue,
								 String messageSelector,
								 javax.jms.ServerSessionPool sessionPool,
								 int maxMessages) throws JMSException {
	throw (new JMSAAAException("Not yet available", JMSAAAException.NOT_YET_AVAILABLE));
    }

} // QueueConnection
