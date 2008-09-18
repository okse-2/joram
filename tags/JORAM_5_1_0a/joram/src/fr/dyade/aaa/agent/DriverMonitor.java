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

/** 
 * The <code>DriverMonitor</code> class is used by <code>ProxyAgent</code>
 * instances managing multi-connections for holding the elements composing a
 * connection set.
 */
public class DriverMonitor {

  /** The DriverIn of this connection set. */
  DriverIn drvIn;
  /** The DriverOut of this connection set. */
  DriverOut drvOut;
  /** The Queue (out) of this connection set. */
  fr.dyade.aaa.util.Queue qout;
  /** The DriverConnect object of this connection set. */
  DriverConnect drvCnx;
  /** The NotificationInputStream of this connection set. */
  NotificationInputStream ois;
  /** The NotificationOutputStream of this connection set. */
  NotificationOutputStream oos;

  /**
   * Constructor.
   */
  public DriverMonitor(DriverIn drvIn,
                       DriverOut drvOut, 
                       fr.dyade.aaa.util.Queue qout,
                       NotificationInputStream ois,
                       NotificationOutputStream oos,
                       DriverConnect drvCnx)  {
    this.drvIn = drvIn;
    this.drvOut = drvOut;
    this.qout = qout;
    this.ois = ois;
    this.oos = oos;
    this.drvCnx = drvCnx;
  }

  /** Method returning the qout object. */ 
  public fr.dyade.aaa.util.Queue getQout() {
    return qout;
  }

  /** Method returning the ois object. */ 
  public NotificationInputStream getOis() {
    return ois;
  }

  /** Method returning the oos object. */ 
  public NotificationOutputStream getOos() {
    return oos;
  }
}
