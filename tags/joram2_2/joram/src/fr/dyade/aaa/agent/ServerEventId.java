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

public class ServerEventId implements Serializable {
public static final String RCS_VERSION="@(#)$Id: ServerEventId.java,v 1.7 2002-01-16 12:46:47 joram Exp $";

  public final static int MAX_TYPE = 10000;
  /**
  * The Server id
  */
  private short sid;

  /**
  * type of the Event
  */
  private int type;
  
  /**
   * the unique key for the Hastable.
   */
  private int key;


  public ServerEventId(short sid, int type){
  	this.sid = sid;
	this.type = type;
	// estimation: the type < 10000; 
	this.key=(sid * MAX_TYPE) + type;
  }
  
  /**
   * return the serverId of the ServerEventId
   */
  public short getServerId(){
    return this.sid;
  }

  /**
   * return the type of the SEI
   * @see ServerEventType
   */  
  public int getType(){
  	return this.type;
  }

  /*
   * convert the SEI to a String
   */
  public String toString(){
    return ("Server"+sid+"/SET"+type);
  }
  
  /**
   * get the unique key of the SEI
   */
  public int hashCode(){
    return this.key;
  }

  public boolean equals(Object obj){
    try {
      return this.key==((ServerEventId)obj).hashCode();
    }
    catch(ClassCastException ex) {
      return false; }
  }




}
