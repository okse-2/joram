/*
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA.
 */


package fr.dyade.aaa.agent;

import java.io.*;
import java.net.*;
import fr.dyade.aaa.agent.*;


/**
 * The type of listener of an event, can be a Role or a couple <@ip,port>
 * @author	Laumay Philippe
 * @version	v1.0
 */
public class AdminEventListenerId implements Serializable{
public static final String RCS_VERSION="@(#)$Id: AdminEventListenerId.java,v 1.16 2004-02-13 10:15:21 fmaistre Exp $";
    
    /* attributes of the type */
    public final static short NULL_LISTENER = 0;
    public final static short DISTANT_LISTENER = 1;
    public final static short LOCAL_LISTENER = 2;
    
    /**
     * the type of listener: AgentId or distant Agent (= @ip, port number)
     */
    public short type;
    
    /**
     * the AgentId of the listener (if LOCAL_LISTENER)
     */
    public AgentId id = null;
    
    /**
     * the InetAdress of the listener (if DISTANT_LISTENER)
     */ 
    public InetAddress address = null;
    
    /**
     * the port number of the listener (if DISTANT_LISTENER)
     */
    public int port;
    
    /**
     * construct a listener (can be an AgentId or a couple <@ip, port number>)
     *
     * @param type DISTANT_LISTENER or LOCAL_LISTENER
     */
    public AdminEventListenerId(short type){
	this.type=type;
    }

    /**
     * Sets the listener id.
     * If the listener type is <code>DISTANT_LISTENER</code>, returns an
     * exception.
     *
     * @param id	the agent id of the listener
     *
     * @exception Exception
     *	when type is not <code>LOCAL_LISTENER</code>
     */
    public void setListener(AgentId id) throws Exception{
	if (this.type != LOCAL_LISTENER)
	  throw new Exception("Illegal type: " + this.type);
	this.id = id;
    }
    
    /**
     * Sets the listener address.
     * If the listener type is <code>LOCAL_LISTENER</code>, returns an
     * exception.
     *
     * @param address	the IP address of the distant listener
     * @param port	the port number of the distant listener
     *
     * @exception Exception
     *	when type is not <code>DISTANT_LISTENER</code>
     */
    public void setListener(InetAddress address, int port) throws Exception{
	if (this.type != DISTANT_LISTENER)
	  throw new Exception("Illegal type: " + this.type);
	this.address = address;
	this.port = port;
    }

    /**
     * Provides a string image for this object.
     *
     * @return	a string image for this object
     */
    public String toString(){
	if (type == LOCAL_LISTENER) return ("LOCAL_LISTENER: ID: " + id);
	if (type == DISTANT_LISTENER) return ("DISTANT_LISTENER: @IP: " + address + " port: " + port);
	if (type == NULL_LISTENER) return ("NULL_LISTENER");
	return ("ERROR");
    }

    /**
     * Compares two objects for equality.
     *
     * @param obj	the reference object with which to compare
     * @return		<code>true</code> if this object is the same as the
     *			<code>obj</code> argument; <code>false</code> otherwise
     */
    public boolean equals(Object obj){
	boolean bool = false;
	try{
	    AdminEventListenerId aeli = (AdminEventListenerId) obj;
	    return((aeli.type == this.type) &&
		   ((aeli.id == this.id) ||
		    ((aeli.address == this.address) &&
		    (aeli.port == this.port))));
	} catch(ClassCastException e){
	    return false;
	}
    }
}
