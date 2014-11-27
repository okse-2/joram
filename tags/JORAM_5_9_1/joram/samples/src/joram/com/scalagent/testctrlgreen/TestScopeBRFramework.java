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

import com.scalagent.ctrlgreen.Action;
import com.scalagent.ctrlgreen.ActionHandler;
import com.scalagent.ctrlgreen.ActionReturn;
import com.scalagent.ctrlgreen.Inventory;
import com.scalagent.ctrlgreen.InventoryHandler;
import com.scalagent.ctrlgreen.ScopeBRFramework;

public class TestScopeBRFramework implements InventoryHandler, ActionHandler {
  static ScopeBRFramework framework = null;
  
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

    TestScopeBRFramework handler = new TestScopeBRFramework();
    
    framework = new ScopeBRFramework(host, port, handler, handler);
    System.in.read();
    framework.close();
    System.exit(0);
  }

  @Override
  public void onInventory(Inventory inventory) {
    System.out.println("TestScopeBRFramework.onInventory: " + inventory);
  }
  
  @Override
  public ActionReturn onAction(Action action) {
    System.out.println("TestScopeBRFramework.onAction: " + action);
    if (action.getType() == Action.ValidatePlan) {
      try {
        System.out.println("TestScopeBRFramework.invokeAction(ExecutePlan)");
        framework.invokeAction("Tune", new MyAction(Action.ExecutePlan, ((MyAction) action).parameters));
        System.out.println("TestScopeBRFramework.invokeAction return");
      } catch (JMSException e) {
        e.printStackTrace();
      }
    }
    return new ActionReturn();
  }
}
