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

public class SetField extends Notification {

  public static final String RCS_VERSION="@(#)$Id: SetField.java,v 1.13 2003-06-23 13:37:51 fmaistre Exp $"; 

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


