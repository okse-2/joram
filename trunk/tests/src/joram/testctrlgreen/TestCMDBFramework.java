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
package joram.testctrlgreen;

import javax.jms.JMSException;

import fr.dyade.aaa.util.management.MXWrapper;

import joram.ctrlgreen.Action;
import joram.ctrlgreen.ActionReturn;
import joram.ctrlgreen.CMDBFramework;
import joram.ctrlgreen.InventoryHandler;
import joram.ctrlgreen.Trace;

public class TestCMDBFramework implements InventoryHandler, TestCMDBFrameworkMBean {
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
    
    framework = new CMDBFramework(host, port, handler);
    System.in.read();
    framework.close();
    System.exit(0);
  }

  public String askInventory() {
    ActionReturn ret = null;
    try {
      ret = framework.invokeAction("Talend",
                                   new Action(Action.VMWare_Inventory, "Get VMWare Inventory"));
    } catch (JMSException e) {
      e.printStackTrace();
      return null;
    }
    return ret.toString();
  }

  @Override
  public void onInventory(String inventory) {
    System.out.println("CMDBFramework.onInventory: " + inventory);
    try {
      framework.publishInventoryCMDB("<!-- From CMDB -->\n" + inventory);
    } catch (JMSException exc) {
      Trace.error("CMDBFramework.onInventory: Cannot publish inventory.", exc);
    }
  }
}
