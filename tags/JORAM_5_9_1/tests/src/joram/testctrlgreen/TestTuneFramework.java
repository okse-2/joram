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

import java.util.Properties;

import javax.jms.JMSException;

import fr.dyade.aaa.util.management.MXWrapper;

import joram.ctrlgreen.Action;
import joram.ctrlgreen.ActionReturn;
import joram.ctrlgreen.InventoryHandler;
import joram.ctrlgreen.ParameterHandler;
import joram.ctrlgreen.TuneFramework;

public class TestTuneFramework implements InventoryHandler, ParameterHandler, TestTuneFrameworkMBean {
  static TuneFramework framework = null;
  
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

    TestTuneFramework handler = new TestTuneFramework();
    MXWrapper.registerMBean(handler, "Tune", "name=test");
    
    framework = new TuneFramework(host, port, handler, handler);
    
    System.out.println("Try to get Inventory..");
    ActionReturn ret = framework.invokeAction("Talend",
                                              new Action(Action.VMWare_Inventory, "Get VMWare Inventory"));
    System.out.println("Receives: " + ret);
    
    System.out.println("Request DNS..");
    ret = framework.invokeAction("DNS",
                                 new Action(Action.DNS_AddEntry, "vm1 192.168.1.12"));
    System.out.println("Receives: " + ret);
    
    System.out.println("Request ScopeBR..");
    ret = framework.invokeAction("ScopeBR",
                                 new Action(Action.ScopeBR_Validate, "Action plan.."));
    System.out.println("Receives: " + ret);

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

  public String askDNS() {
    ActionReturn ret = null;
    try {
      ret = framework.invokeAction("DNS",
                                   new Action(Action.DNS_DeleteEntry, "vm2 18.1.1.10"));
    } catch (JMSException e) {
      e.printStackTrace();
      return null;
    }
    return ret.toString();
  }

  public String askScopeBR() {
    ActionReturn ret = null;
    try {
      ret = framework.invokeAction("ScopeBR",
                                   new Action(Action.ScopeBR_Validate, "Action plan2.."));
    } catch (JMSException e) {
      e.printStackTrace();
      return null;
    }
    return ret.toString();
  }

  @Override
  public void onParameter(Properties parameters) {
    System.out.println("TuneFramework.onParameter: " + parameters);
  }

  @Override
  public void onInventory(String inventory) {
    System.out.println("TuneFramework.onInventory: " + inventory);
  }
}
