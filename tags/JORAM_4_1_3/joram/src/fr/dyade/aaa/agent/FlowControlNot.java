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
 * Flow control notification.
 * <p>
 * This notification is used to control the flow of notifications from a
 * <code>DriverIn</code> object to its associated agent.
 *
 * @author	Freyssinet Andre
 * @version	v1.0
 *
 * @see		DriverIn
 */
public class FlowControlNot extends Notification {


  /** id of <code>DriverIn</code> agent issuing notification, when applicable */
  int driverId;

  /** 
   * Key of the <code>DriverIn</code> issuing the notification, in a multi
   * connections context.
   */
  int driverKey = 0;


  /**
   * Constructor.
   *
   * @param driverId	id of <code>Driver</code> issuing notification
   */
  FlowControlNot(int driverId) {
    this.driverId = driverId;
  }

  /**
   * Constructor.
   *
   * @param driverId  id of <code>Driver</code> issuing notification
   * @param driverKey  key of driver issuing the notification.
   */
  FlowControlNot(int driverId, int driverKey) {
    this.driverId = driverId;
    this.driverKey = driverKey;
  }



  /**
   * Constructor with default id.
   */
  FlowControlNot() {
    this(0);
  }

  /**
   * Provides a string image for this object.
   *
   * @return	printable image of this object
   */
  public String toString() {
    return "(" + super.toString() +
      ",driverKey=" + driverKey +
      ",driverId=" + driverId + ")";
  }
}
