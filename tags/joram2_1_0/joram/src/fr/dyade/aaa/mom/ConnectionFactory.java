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
package fr.dyade.aaa.mom;

import java.lang.*;
import java.io.*; 
import fr.dyade.aaa.agent.*;

/**
 * ConnectionFactory is a TCP proxy used for connecting JMS client
 * with agents in charge of MOM.
 *
 * @see	fr.dyade.aaa.mom.CommonClientAAA	
 * @see	fr.dyade.aaa.mom.Queue
 * @see	fr.dyade.aaa.mom.Topic
 * @see	fr.dyade.aaa.mom.ClientSubscription
 */
public class ConnectionFactory extends fr.dyade.aaa.ip.TcpMultiServerProxy {
  /** Well known port for this agent to listen to */
  static final int WKNPort = 16010;

  /* Constructor for the ConnectionFactory */
  public ConnectionFactory(int port) {
    super(port);
    super.newClient = false;
  }

    /* Constructor for the agentClient */
    protected ConnectionFactory() {
    	super();
	super.newClient = false;
    }	

  /**
   * Initializes the proxy as a service.
   *
   * @param args	parameters from the configuration file,
   *			may hold an optional port number to listen to
   * @param firstTime	<code>true</code> when agent server starts anew
   */
  public static void init(String args, boolean firstTime) throws Exception {
      if (! firstTime)
        return;

    // gets the optional port number
    int listenPort = WKNPort;
    if (args != null) {
      try {
	listenPort = Integer.parseInt(args);
      } catch (NumberFormatException exc) {
	throw new IllegalArgumentException("Bad port number: " + args);
      }
    }
    // creates the server
    ConnectionFactory cnxFact = new ConnectionFactory(listenPort);
    cnxFact.deploy();
  }

  public void react(AgentId from, Notification not) throws Exception { 
    try {
	/* later check the name of the clients  */
	super.react(from, not);
    } catch (Exception exc) { 
      System.err.println(exc); 
    }
  }

    public AgentId idFromString(String header) {  
	if( header.equals("NewAgentClient") )
	    return null;
	else
	    return AgentId.fromString(header);
    }


    protected fr.dyade.aaa.ip.TcpMultiServerProxy createNewProxy(String header) throws Exception {
	fr.dyade.aaa.ip.TcpMultiServerProxy proxy = null;
	if ( header.equals("NewAgentClient") ) {
	    fr.dyade.aaa.mom.AgentClient agentClient = new fr.dyade.aaa.mom.AgentClient();
	    return  agentClient;
	} else {
	    return (fr.dyade.aaa.ip.TcpMultiServerProxy) getClass().newInstance();
	}
    }

  /**
   * Creates a (chain of) filter(s) for transforming the specified
   * <code>InputStream</code> into a <code>NotificationInputStream</code>.
   */
  protected NotificationInputStream setInputFilters(InputStream in) throws StreamCorruptedException, IOException {
    return (new NotificationMOMInputStream(in));
  }

  /**
   * Creates a (chain of) filter(s) for transforming the specified
   * <code>OutputStream</code> into a <code>NotificationOutputStream</code>.
   */
  protected NotificationOutputStream setOutputFilters(OutputStream out) throws IOException {
    return (new NotificationMOMOutputStream(out));
  }

}
