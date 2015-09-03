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
package com.scalagent.testctrlgreen;

import javax.jms.JMSException;

import fr.dyade.aaa.util.management.MXWrapper;

import com.scalagent.ctrlgreen.Action;
import com.scalagent.ctrlgreen.ActionHandler;
import com.scalagent.ctrlgreen.ActionReturn;
import com.scalagent.ctrlgreen.CMDBFramework;
import com.scalagent.ctrlgreen.Inventory;
import com.scalagent.ctrlgreen.InventoryHandler;
import com.scalagent.ctrlgreen.Trace;

public class TestCMDBFramework implements ActionHandler, InventoryHandler, TestCMDBFrameworkMBean {
  static CMDBFramework framework = null;
  
  public static void main(String[] args) throws Exception {
    String host = null;
    int port = -1;
    
    if (args.length != 0) {
      if (args.length != 2) {
        System.err.println("usage: java ... TestCMDBFramework host port");
        System.exit(-1);
      }
      host = args[0];
      port = Integer.parseInt(args[1]);
    }
    
    TestCMDBFramework handler = new TestCMDBFramework();
    MXWrapper.registerMBean(handler, "CMDB", "name=test");
    
    framework = new CMDBFramework(host, port, handler, handler);
    System.in.read();
    framework.close();
    System.exit(0);
  }

  public String askInventory() {
    ActionReturn ret = null;
    try {
      ret = framework.invokeAction("Talend",
                                   new Action(Action.GetInventoryVMware));
    } catch (JMSException e) {
      e.printStackTrace();
      return null;
    }
    return ret.toString();
  }

  @Override
  public void onInventory(Inventory inventory) {
    System.out.println("CMDBFramework.onInventory: " + inventory);
    try {
      framework.publishInventoryCMDB(inventory);
    } catch (JMSException exc) {
      Trace.error("CMDBFramework.onInventory: Cannot publish inventory.", exc);
    }
  }

  @Override
  public ActionReturn onAction(Action action) {
    System.out.println("CMDBFramework.onAction: " + action);
    return null;
  }
}
