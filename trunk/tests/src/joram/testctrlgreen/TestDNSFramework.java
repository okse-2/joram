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

import joram.ctrlgreen.Action;
import joram.ctrlgreen.ActionHandler;
import joram.ctrlgreen.ActionReturn;
import joram.ctrlgreen.DNSFramework;

public class TestDNSFramework implements ActionHandler {
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
    
    TestDNSFramework handler = new TestDNSFramework();
    
    DNSFramework framework = new DNSFramework(host, port, handler);
    System.in.read();
    framework.close();
    System.exit(0);
  }
  
  @Override
  public ActionReturn onAction(Action action) {
    System.out.println("TestDNSFramework.onAction: " + action);
    return new ActionReturn();
  }
}
