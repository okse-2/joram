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

public class SetField extends Notification {

  public static final String RCS_VERSION="@(#)$Id: SetField.java,v 1.7 2002-01-16 12:46:47 joram Exp $"; 

  public String name;	// Name of field to set.
  public Object value;

  // id of the agent that should receive a SetFieldAck
  public AgentId reply; 

  public SetField(String name, Object value){
    this(name,value,null);
  }

  public SetField(String name, Object value, AgentId reply) {
    super();
    this.name = name;
    this.value = value;
    this.reply = reply;
  }
  /**
   * Returns a string representation of this notification.
   *
   * @return	A string representation of this notification. 
   */
  public String toString() {
    return "(" + super.toString() +
      ",name=" + name +
      ",value=" + value +
      ",reply=" + reply +
      ")";
  }
}


