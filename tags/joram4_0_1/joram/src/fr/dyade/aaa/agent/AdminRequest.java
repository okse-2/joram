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

/**
 * <code>Notification</code> for an administration request.
 *
 * @author	Freyssinet Andr*
 * @version	v1.0
 *
 * @see		AgentAdmin
 */
public class AdminRequest extends Notification {

public static final String RCS_VERSION="@(#)$Id: AdminRequest.java,v 1.17 2004-03-16 10:03:45 fmaistre Exp $"; 


  public static final int GetServers = 1;
  public static final int GetProperties = 2;
  
  /* final attribut for Administred Servers */
  public static final int Subscribe_SET = 3;
  public static final int UnSubscribe_SET = 4;
  public static final int GetServerList = 5;
  public static final int GetServerProperties = 6;
  public static final int GetAgentsList = 7;
  
  /**
   * define the type of AdminRequest
   */ 
  private int request;
  
  /**
  * Type of the EventSource if the Request > 2
  */
  private int set;
  
  /**
   * the sender id
   */
  public AdminEventListenerId aeli;
 
  /**
   * constructor used by old version
   * @param request the type of request (Please Use the predefined finals Attributs)
   */			
  public AdminRequest(int request) {
    this(request,new AdminEventListenerId(AdminEventListenerId.NULL_LISTENER));
  }
  
  /**
   * constructor used for Administred Server
   * @param request the type of request (Please Use the predefined finals Attributs)
   * @param ael the AdminEventListenerId
   */			
  public AdminRequest(int request, AdminEventListenerId aeli) {
    this.request = request;
    this.aeli = aeli;
  }
    
  /**
   * Set the ServerEventId.
   * return false if AdminRequest is not a Request for Administred Server (: request < 2).
   */
  public boolean setServerEventType(int set) {
    if (request < 2) return false;
    else {
      this.set=set;
      return true;
    }
  }
  
  /**
   * return the ServerEventId (null if this is not a Request for Administred Server).
   */
  public int getServerEventType(){
    return set;
  }
  
  /**
   * return the number of the AdminRequest.
   */
  public int getRequest(){
    return this.request;
  }
}
