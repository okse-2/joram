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

import java.io.*;
import java.net.*;
import java.util.Properties;

/**
 * Description of a remote server.
 * @version	1.1, 29/7/98
 * @author	Noel De Palma
 */
public final class A3Node implements Serializable {
  public static final String RCS_VERSION="@(#)$Id: A3Node.java,v 1.5 2001-05-14 16:26:36 tachkeni Exp $"; 

  /**  Node name. */
  private String Name;
  /** Node number.  */
  private int Num;
  /** Host name. */
  private String HostName;
  /** Is the server transient? */
  boolean isTransient;
  /** Server State (active or not). */
  private boolean active;
  
  /**
   * Constructs a new node with default values.
   */
  public A3Node(){
    this(null, 0, "localhost", false, false);
  }

  /**
   * Constructs a new node with the specified parameters.
   * @param Name	Server Name
   * @param Num		Node Number
   * @param HostName	Host Name
   * @param isTransient	Is the server transient?
   * @param active	Is the server active?
   */
  public A3Node(String Name,
        	int Num,
                String HostName,
		boolean isTransient,
		boolean active) {
    this.Name = Name;
    this.Num = Num;
    this.HostName = HostName;
    this.isTransient = isTransient;
    this.active = active;
  }
 
  /**
   * Constructs a new node with the specified server descriptor.
   *
   * @param	desc	Agent server descriptor.
   */
  public A3Node(ServerDesc desc) {
    this.Name = desc.name;
    this.Num = desc.sid;
    this.HostName = desc.hostname;
    this.isTransient = desc.isTransient;
    this.active = desc.active;
  }

  /**
   * Return the name of the node.
   */
  public String GetName(){
    return Name;
  }

  /**
   * Return the number that identifie the node.
   */
  public int GetNodeNumber(){
    return Num;
  }

  /**
   * Return the name of the Host where the node run.
   */
  public String GetHostName(){
    return HostName;
  }

  /**
   * Return True if the Remote Server is transient.
   */
  public boolean isTransient(){
    return isTransient;
  }

  /**
   * Return true if the server is active
   */
  public boolean isActive() {
    return active;
  }

  /**
   * Return the the node in a string whose can be display.
   */
  public String toString(){
    StringBuffer strBuf = new StringBuffer();
    strBuf.append("A3Node[").append(GetNodeNumber()).append(", ");
    strBuf.append(Name).append(", ");
    strBuf.append(HostName).append(", ");
    strBuf.append(isTransient).append(", ");
    strBuf.append(active).append("]");
    return strBuf.toString();
  }
}

