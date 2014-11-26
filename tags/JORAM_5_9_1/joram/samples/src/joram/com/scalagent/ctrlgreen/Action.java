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
package com.scalagent.ctrlgreen;

import java.io.Serializable;

import javax.jms.Message;

public class Action implements Serializable {
  public final static int NoAction = 0;
  
  public final static int AddEntry = 1;
  public final static int DeleteEntry = 2;
  public final static int AddRule = 3;
  public final static int DeleteRule = 4;
  public final static int ValidatePlan = 5;
  public final static int ExecutePlan = 6;
  public final static int CloneVM = 7;
  public final static int StartVM = 8;
  public final static int StopVM = 9;
  public final static int MigrateVM = 10;
  public final static int DeleteVM = 11;
  public final static int StartHypervisor = 12;
  public final static int StopHypervisor = 13;
  public final static int PowerOffHypervisor = 14;
  public final static int ModifyCPU = 15;
  public final static int ModifyRAM = 16;
  public final static int ModifyStorage = 17;
  public final static int AddMonitoring = 18;
  public final static int RemoveMonitoring = 19;
  public final static int GetInventoryCMDB = 20;
  public final static int GetInventoryVMware = 21;
  
  public final static int MaxAction = 21;
  
  final static String[] actionsStr = {
    "NoAction",
    "AddEntry",
    "DeleteEntry",
    "AddRule",
    "DeleteRule",
    "ValidatePlan",
    "ExecutePlan",
    "CloneVM",
    "StartVM",
    "StopVM",
    "MigrateVM",
    "DeleteVM",
    "StartHypervisor",
    "StopHypervisor",
    "PowerOffHypervisor",
    "ModifyCPU",
    "ModifyRAM",
    "ModifyStorage",
    "AddMonitoring",
    "RemoveMonitoring",
    "GetInventoryCMDB",
    "GetInventoryVMware"
  };
  
  private int type = NoAction;
  
  public int getType() {
    return type;
  }
  
  public Action(int type) {
    this.type = type;
  }
  
  protected Message toJMSMessage() {
    return null;
  }
  
  public String toString() {
    String action = null;
    if ((type < NoAction) || (type >= MaxAction))
      action = "Unknown#" + type;
    else
      action = actionsStr[type];
    
    return action;
  }
}
