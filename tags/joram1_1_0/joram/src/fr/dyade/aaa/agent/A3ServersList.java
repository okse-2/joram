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

public final class A3ServersList extends Notification {

public static final String RCS_VERSION="@(#)$Id: A3ServersList.java,v 1.3 2000-10-05 15:15:18 tachkeni Exp $";

 /**
   * A3Node contains informations about an agent server.
   */
  public A3Node nodes[];
 

  public A3ServersList() {
    int nb = Server.networkServers.length;
    if (Server.transientServers != null)
      nb += Server.transientServers.length;
    
    nodes = new A3Node[nb];
    
    if (Server.transientServers != null) {
      for (int i = Server.a3config.transientServers.length; i-- > 0;) {
	nodes[--nb] = new A3Node(
	  Server.a3config.transientServers[i].name,	// node name
	  Server.a3config.transientServers[i].sid,	// server's number
	  Server.a3config.transientServers[i].hostname,
	  Server.a3config.transientServers[i].port,
	  Server.ADMINISTRED,
	  Server.admin,
	  Server.a3config.transientServers[i].active);
      }
    }
    for (int i = Server.a3config.networkServers.length; i-- > 0;) {
      nodes[--nb] = new A3Node(
	Server.a3config.networkServers[i].name,		// node name
	Server.a3config.networkServers[i].sid,		// server's number
	Server.a3config.networkServers[i].hostname,
	Server.a3config.networkServers[i].port,
	Server.ADMINISTRED,
	Server.admin,
	Server.a3config.networkServers[i].active);
    }
  }
}
