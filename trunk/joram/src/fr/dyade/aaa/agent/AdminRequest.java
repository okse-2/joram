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
 * <code>Notification</code> for an administration request.
 *
 * @author	Freyssinet Andr*
 * @version	v1.0
 *
 * @see		AgentAdmin
 */
public class AdminRequest extends Notification {

public static final String RCS_VERSION="@(#)$Id: AdminRequest.java,v 1.12 2003-03-19 15:16:05 fmaistre Exp $"; 


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
