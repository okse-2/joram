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

import java.util.Date;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import javax.jms.JMSException;

import joram.ctrlgreen.Action;
import joram.ctrlgreen.ActionHandler;
import joram.ctrlgreen.ActionReturn;
import joram.ctrlgreen.ClientFramework;
import joram.ctrlgreen.Trace;

import org.objectweb.joram.client.jms.ConnectionFactory;
import org.objectweb.joram.client.jms.FactoryParameters;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

public class SimTalend implements ActionHandler {
  static ClientFramework framework = null;
  
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
    
    ConnectionFactory cf = TcpConnectionFactory.create(host, port);
    FactoryParameters parameters = cf.getParameters();
    parameters.connectingTimer = 10;
    parameters.cnxPendingTimer = 5000;
    
    framework = new ClientFramework("Talend", cf);
    SimTalend handler = new SimTalend();
    framework.setActionHandler(handler);
    
    Timer timer = new Timer();
    timer.schedule(new TimerTask() {
      public void run() {
        Properties parameters = new Properties();
        parameters.setProperty("par1", "value1");
        parameters.setProperty("par2", "value2");
        parameters.setProperty("par3", new Date().toString());
        try {
          framework.publishParameters(parameters);
        } catch (JMSException e) {
          e.printStackTrace();
        }
      }}, 10000, 5000);
    System.in.read();
    framework.close();
    System.exit(0);
  }
  
  @Override
  public ActionReturn onAction(Action action) {
    System.out.println("SimTalend.onAction: " + action);
    
    if (action.getType() == Action.VMWare_Inventory) {
      try {
        framework.publishInventoryVMWare(
          "<inventory name=\"Inventory\">\n" +
          "  <vm name=\"vm1\"\\>\n" +
          "  <vm name=\"vm2\"\\>\n" +
          "<\\inventory>\n");
      } catch (JMSException exc) {
        Trace.error("CMDBFramework.onInventory: Cannot publish inventory.", exc);
        return new ActionReturn(ActionReturn.ERROR, "Cannot publish inventory");
      }
    }
    
    return new ActionReturn();
  }
}
