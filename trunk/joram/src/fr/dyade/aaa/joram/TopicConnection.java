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
 * A TopicConnection is an active connection to a JMS Pub/Sub provider.
 * A client uses a TopicConnection to create one or more TopicSessions
 * for producing and consuming messages.
 *
 * @author Laurent Chauvirey
 * @version 1.0
 */

public class TopicConnection extends Connection implements javax.jms.TopicConnection {
    
    /**
     * Construct a <code>TopicConnection</code>.
     */
    public TopicConnection(String proxyAgentIdString,
			   InetAddress proxyAddress, int proxyPort,
			   String login, String passwd) throws JMSException {
	super(proxyAgentIdString, proxyAddress, proxyPort, login, passwd);
    }


    /**
     * Create a TopicSession.
     */
    public javax.jms.TopicSession createTopicSession(boolean transacted, int acknowledgeMode) throws JMSException {
	try {
	    long sessionCounterNew = sessionCounter;
	    sessionCounter = calculateMessageID(sessionCounter);
	    fr.dyade.aaa.joram.TopicSession session = new fr.dyade.aaa.joram.TopicSession(transacted, acknowledgeMode, sessionCounterNew, this);
	    if (session == null) {
		sessionCounter = sessionCounter - 1;
		throw (new fr.dyade.aaa.joram.JMSAAAException("Internal Error during creation TopicSession",JMSAAAException.ERROR_CREATION_SESSION));
	    } else {
		return session;
	    }
	} catch (JMSException e) {
	    throw(e);
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
    public javax.jms.ConnectionConsumer createConnectionConsumer(javax.jms.Topic queue,
								 String messageSelector,
								 javax.jms.ServerSessionPool sessionPool,
								 int maxMessages) throws JMSException {
	throw (new JMSAAAException("Not yet available", JMSAAAException.NOT_YET_AVAILABLE));
    }
    

    /**
     * Create a durable connection consumer for this connection (optional operation).
     * This is an expert facility not used by regular JMS clients.
     */
    public javax.jms.ConnectionConsumer createDurableConnectionConsumer(javax.jms.Topic topic,
									String subscriptionName,
									String messageSelector,
									javax.jms.ServerSessionPool sessionPool,
									int maxMessages) throws javax.jms.JMSException {
	throw (new fr.dyade.aaa.joram.JMSAAAException("Not yet available", JMSAAAException.NOT_YET_AVAILABLE));
    }
  


} // TopicConnection
