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

import java.util.Vector;

public final class A3ServersList extends Notification {

public static final String RCS_VERSION="@(#)$Id: A3ServersList.java,v 1.5 2001-05-14 16:26:37 tachkeni Exp $";

 /**
  * A3Node contains informations about an agent server.
  */
  public A3Node nodes[];

  public A3ServersList() {
    ServerDesc server = null;
    Vector servers = new Vector();
    for (int i=0; i<AgentServer.getServerNb(); i++) {
      server = AgentServer.getServerDesc((short) i);
      if (server != null) {
	servers.addElement(new A3Node(server));
      }
    }
    nodes = new A3Node[servers.size()];
    nodes = (A3Node []) servers.toArray(nodes);
  }

  /**
   * Returns a string representation of this object, including alls A3Nodes.
   *
   * @return	A string representation of this object.
   */
  public String toString(){
    StringBuffer strBuf = new StringBuffer();
    strBuf.append("A3ServersList[").append(nodes.length).append(',');
    for (int i=0; i<nodes.length; i++) {
      strBuf.append(nodes[i]);
      if (i< (nodes.length -1)) strBuf.append(',');
    }
    strBuf.append(']');
    return strBuf.toString();
  }
}
