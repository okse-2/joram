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
 * Description of a remote server (A3Node).
 * A3Node class is similar to ServerDesc class. This class should 
 * be used only by the admin agent.
 * @version	1.1, 29/7/98
 * @author	Noel De Palma
 */
public final class A3Node implements Serializable {

public static final String RCS_VERSION="@(#)$Id: A3Node.java,v 1.1.1.1 2000-05-30 11:45:24 tachkeni Exp $"; 

  /**
   * Node name.
   */
  private String Name;
  /**
   * Node number.
   */
  private int Num;
  /**
   * Host name.
   */
  private String HostName;
  /**
   * Node port.
   */
  private int Port;
  
  /**
  * monitored server ?
  */
  private boolean administred;
  
  /**
   * actually monitored server ?
   */
  private boolean admin;

  /**
  * Server State (active or not);
  */
  private boolean active;
  
    public A3Node(){
	this(0,"",0,false,Server.ADMINISTRED,false);
    }

  /**
   * Constructs a new node with the specified parameters.
   * @param	HostName	Host Name
   * @param     Num		Node Number
   * @param	Port		Node port
   * @param	admin		monitored server ?
   * @param	active		server active ?
   */

  public A3Node(int Num,
		String HostName,
		int Port,
		boolean administred,
		boolean admin,
		boolean active) {
    this(HostName+":" + Port, Num,HostName, Port);
    this.administred = administred;
    this.admin = admin;
    this.active	= active;
  }
  
  /**
   * Constructs a new node with the specified parameters.
   * @param	Name	        Server Name
   * @param	HostName	Host   Name
   * @param     Num		Node   Number
   * @param	Port		Node   port
   * @param	admin		monitored server ?
   * @param	active		server active ?
   */

  public A3Node(String Name,
        	int Num,
                String HostName,
		int Port,
		boolean administred,
		boolean admin,
		boolean active) {
    this.Name			= Name;
    this.Num			= Num;
    this.HostName		= HostName;
    this.Port			= Port;
    this.administred   		= administred;
    this.admin			= admin;
    this.active			= active;
  }
 

 /**
   * Constructs a new node with the specified parameters.
   * @param	HostName	Host Name
   * @param     Num		Node Number
   * @param	Port		Node port
   */

  public A3Node(int Num,
		String HostName,
		int Port) {
    this(HostName+":" + Port, Num,HostName, Port);
  }

/**
   * Constructs a new node with the specified parameters.
   * @param	Name	        Server Name
   * @param	HostName	Host   Name
   * @param     Num		Node   Number
   * @param	Port		Node   port
   */

  public A3Node(String Name,
        	int Num,
                String HostName,
		int Port) {
    this.Name			= Name;
    this.Num			= Num;
    this.HostName		= HostName;
    this.Port			= Port;
    this.administred 		= Server.ADMINISTRED;
    this.admin 			= false;
    this.active			= false;
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
   * Return the Port of the host were the node run.
   */
  public int GetPort(){
    return Port;
  }

  /**
   * Return true if the server can be administred
   */
  public boolean canBeAdmin(){
    return administred;
  }  
  
  /**
   * Return true if the server is actually administred
   */
  public boolean isAdmin(){
    return admin;
  }

  /**
   * Return true if the server is active
   */
  public boolean isActive(){
    return active;
  }

  /**
   * Return the the node in a string whose can be display.
   */
  public String toString(){
     return new String("Node : "+Integer.toString(this.GetNodeNumber())+" Name : "+this.GetName()+" Running on : "
		       +this.GetHostName()+" At port :" +Integer.toString(GetPort()) + ", Administred: "
		       + this.admin + ", Active: " + this.active);
  }
  
  /**
  * Return True if the Remote Server is transient.
  */
  public boolean isTransient(){
  	return (this.Num >= Server.MIN_TRANSIENT_ID);
  }
 
}

