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

import org.objectweb.util.monolog.api.BasicLevel;

/** 
 * The <code>DriverConnect</code> class is used by <code>ProxyAgent</code>
 * instances for managing their connection steps.
 */
class DriverConnect extends Driver {
  protected ProxyAgent proxy = null;
  protected boolean blockingCnx;
  protected boolean multipleCnx;

  DriverConnect(ProxyAgent proxy, boolean blockingCnx, boolean multipleCnx) {
    this.proxy = proxy;
    this.blockingCnx = blockingCnx;
    this.multipleCnx = multipleCnx;
    this.name = proxy.getName() + ".DriverConnect";
    // Get the proxy logging monitor
    logmon = proxy.logmon;
  }

  public void start() {
    if (! blockingCnx)
      run();
    else
      super.start();
  }

  public void run() {
    try {
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, getName() + ", started");

      do {
        proxy.createDrivers();
      } while (isRunning && multipleCnx);
    }
    catch (java.net.SocketException exc) {
      if (! proxy.finalizing) {
        logmon.log(BasicLevel.WARN,
                   "connection closed in createDrivers()",
                   exc);
      }
    }
    catch (java.io.EOFException exc) {
      if (! proxy.finalizing) {
        logmon.log(BasicLevel.WARN,
                   "connection closed in createDrivers()",
                   exc);
      }
    }
    catch (Exception exc) { 
      if (! proxy.finalizing)
        logmon.log(BasicLevel.ERROR, "error in createDrivers()", exc);
    }
  }
  
  public void close() {}
}
