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

package fr.dyade.aaa.agent;

/**
 * <code>Agent</code> used for remote administration of each A3 server.
 */
public final class AgentAdmin extends Agent {
  /** RCS version number of this file: $Revision: 1.6 $ */
  public static final String RCS_VERSION="@(#)$Id: AgentAdmin.java,v 1.6 2001-08-31 08:13:55 tachkeni Exp $";

  /**
   * the Proxy needed for Administred Server
   */
  public static AgentId udpAgentAdminId;

  /**
   * Creates a local administration agent (there is no need to deploy it).
   */
  public AgentAdmin() {
    // TODO: try to set class modifier to package (idem for AgentFactory)...
    //  Be careful: We have to create the local AgentAdmin the first
    // time we run the engine.
    super("AgentAdmin#" + AgentServer.getServerId(),
	  true,
	  AgentId.adminId);
  }

  /**
   * Reacts to <code>AgentAdmin</code> specific notifications.
   * Analyzes the notification request code, then do the appropriate
   * work. By default calls <code>react</code> from base class.
   * Handled notification types are :
   *	<code>AdminRequest</code>,
   *
   * @param from	agent sending notification
   * @param not		notification to react to
   *
   * @exception Exception
   *	unspecialized exception
   */
  public void react(AgentId from, Notification not) throws Exception {
    if (not instanceof AdminRequest) {
      AdminRequest n = (AdminRequest) not;
      switch (n.getRequest()) {
      case AdminRequest.GetServers:
	sendTo(from, new A3ServersList());
	break;
      case AdminRequest.GetProperties:
	sendTo(from, new A3ServerProperties());
	break;
      default:
// 	if (AgentServer.ADMINISTRED) {
// 	  // send the notification to the udpAgentAdmin
// 	  if (udpAgentAdminId == null) {
// 	    // the udpAgentAdmin is not launch (the server is not administred)
// 	    // send an error notification to the sender
// 	    if (Debug.admin)
// 	      Debug.trace(name + ": error, UdpAgentAdmin not launch", false);
// 	  } else {
// 	    sendTo(udpAgentAdminId,n);
// 	  }
// 	}
	break;
      }
    } else { 
      super.react(from, not);
    }
  }
}
