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
 * fr.dyade.aaa.util, fr.dyade.aaa.ip, fr.dyade.aaa.mom, fr.dyade.aaa.ns,
 * fr.dyade.aaa.jndi and fr.dyade.aaa.joram, released September 11, 2000. 
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
 */

package fr.dyade.aaa.jndi;

import fr.dyade.aaa.agent.*;
import fr.dyade.aaa.ip.*;
import fr.dyade.aaa.ns.*;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.naming.*;

/**
 *      <code>ProxyClient<code\> is a TcpMultiServerProxy to bind, rebind, unbind and lookup an Referencebale object in the Name Service.
 *
 *      @author Nicolas Tachker
 *
 *	@see fr.dyade.aaa.ip.TcpMultiServerProxy
 *      @see fr.dyade.aaa.ns.NameService
 */
public class ProxyContext extends TcpMultiServerProxy {
    /** AgentId NameService */
    protected static AgentId nsid;
    static {
	try {
	    nsid = NameService.getDefault((short) 0);
	} catch (Exception e) {}
    }
    /** Well known port for this agent to listen to */
    static final int WKNPort = 16400;
    
    public ProxyContext () throws Exception {
	super();
    }
    public ProxyContext (AgentId nsid) throws Exception {
	super(WKNPort);
	this.nsid = nsid;
    }
    
    public ProxyContext (AgentId nsid, int localPort) throws Exception {
	super(localPort);
	this.nsid = nsid;
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
		throw new IllegalArgumentException("Bad port number for NWTcpServer: " +
						   args);
	    }
	}
	// creates the server
	ProxyContext proxyContext = new ProxyContext(NameService.getDefault((short) 0));
	proxyContext.deploy();
    }
    
  /**
   * Reacts to <code>ProxyContext</code> specific notifications.
   *
   * @param from	agent sending notification
   * @param not		notification to react to
   *
   * @exception Exception
   *	unspecialized exception
   */
    public void react(AgentId from, Notification not) throws Exception {
	if (not instanceof NotificationContext) {
	    NotificationContext n = (NotificationContext) not;
	    if ( (n.cmd).equals("lookup") ) {
		sendTo(nsid, new LookupObject(getId(), n.name));
	    } else if ( (n.cmd).equals("rebind") ) {
		sendTo(nsid, new RegisterObject(getId(), n.name, n.obj));
	    } else if ( (n.cmd).equals("bind") ) {
		sendTo(nsid, new BindObject(getId(), n.name, n.obj));
	    } else if ( (n.cmd).equals("unbind") ) {
		sendTo(nsid, new UnregisterCommand(getId(), n.name));
	    } else if ( (n.cmd).equals("list") ) {
		sendTo(nsid, new ListObject(getId(), n.name));
	    }
	} else if (not instanceof LookupReportObject) {
	    qout.push(not);
	} else if (not instanceof ListReportObject) {
	    qout.push(not);
	} else if (not instanceof BindReportObject) {
	    if (((BindReportObject) not).getStatus() == fr.dyade.aaa.ns.SimpleReport.Status.FAIL) {
		qout.push(new NotificationNamingException(" already bound, use rebind."));
	    } else {
		qout.push(null);
	    }
	} else if (not instanceof SimpleReport) {
	    if (((SimpleReport) not).getStatus() == fr.dyade.aaa.ns.SimpleReport.Status.FAIL) {
		SimpleCommand cmd = ((SimpleReport) not).getCommand();
		if (cmd instanceof LookupObject) {
		    qout.push(new NotificationNamingException(null));
		}
	    }
	} else {
	    super.react(from, not);
	}
    }

    /**
     * Creates a (chain of) filter(s) for transforming the specified
     * <code>InputStream</code> into a <code>NotificationInputStream</code>.
     */
    protected NotificationInputStream setInputFilters(InputStream in) throws StreamCorruptedException, IOException {
	return new SerialInputStream(in);
    }

    /**
     * Creates a (chain of) filter(s) for transforming the specified
     * <code>OutputStream</code> into a <code>NotificationOutputStream</code>.
     */
    protected NotificationOutputStream setOutputFilters(OutputStream out) throws IOException {
	return new SerialOutputStream(out);
    }
}
