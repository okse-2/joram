/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2012 ScalAgent Distributed Technologies
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
 *
 * Initial developer(s): ScalAgent Distributed Technologies
 * Contributor(s):
 */
package joram.ctrlgreen;

import java.io.Serializable;

import javax.jms.Message;

public class Action implements Serializable {
  public final static int NoAction = 0;
  
  public final static int DNS_AddEntry = 1;
  public final static int DNS_DeleteEntry = 2;
  public final static int ScopeBR_Validate = 3;
  public final static int FW_AddRule = 4;
  public final static int FW_DeleteRule = 5;
  public final static int VMWare_Add = 6;
  public final static int VMWare_Start = 7;
  public final static int VMWare_Stop = 8;
  public final static int VMWare_Configure = 9;
  public final static int VMWare_Delete = 10;
  public final static int VMWare_Inventory = 11;
  public final static int Centreon_Add = 12;
  public final static int Centreon_Del = 13;
  
  public final static int MaxAction = 14;
  
  final static String[] actionsStr = {
    "NoAction",
    "DNS_AddEntry",
    "DNS_DeleteEntry",
    "ScopeBR_Validate",
    "FW_AddRule",
    "FW_DeleteRule",
    "VMWare_Add",
    "VMWare_Start",
    "VMWare_Stop",
    "VMWare_Configure",
    "VMWare_Delete",
    "VMWare_Inventory",
    "Centreon_Add",
    "Centreon_Del"
  };
  
  private int type = NoAction;
  private String parameters = null;
  
  public int getType() {
    return type;
  }
  
  public String getParameters() {
    return parameters;
  }
  
  public Action(int type) {
    this.type = type;
  }
  
  public Action(int type, String parameters) {
    this.type = type;
    this.parameters = parameters;
  }
  
  protected Message toJMSMessage() {
    return null;
  }
  
  static Action fromJMSMessage(Message msg) {
    return null;
  }
  
  public String toString() {
    String action = null;
    if ((type < NoAction) || (type >= MaxAction))
      action = "Unknown#" + type;
    else
      action = actionsStr[type];
    
    return action + '[' + parameters + ']';
  }
}
