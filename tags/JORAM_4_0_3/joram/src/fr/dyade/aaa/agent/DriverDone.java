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
 * Notification reporting the end of a driver execution.
 *
 * @author	Lacourte Serge
 * @version	v1.0
 *
 * @see		Driver
 */
public class DriverDone extends Notification {

public static final String RCS_VERSION="@(#)$Id: DriverDone.java,v 1.17 2004-03-16 10:03:45 fmaistre Exp $"; 


  protected int driver;		/** identifies the terminated driver (OUT or IN) */

  /** Identifies the driver in the context of multi-connections. */
  protected int driverKey = 0;


  /**
   * Creates a notification to be sent.
   *
   * @param driver	identifies the terminated driver
   */
  public DriverDone(int driver) {
    this.driver = driver;
  }

  /**
   * Constructor used in a multi-connections context.
   *
   * @param driver  identifies the terminated driver (IN or OUT).
   * @param driverKey  identifies the driver among other drivers.
   */
  public DriverDone(int driver, int driverKey) {
    this.driver = driver;
    this.driverKey = driverKey;
  }


  /**
   * Accesses read only property.
   */
  public int getDriver() {
    return driver;
  }

  /**
   * Method returning the driverKey identifying
   * the closing driver.
   */
  public int getDriverKey() {
    return driverKey;
  }

  /**
   * Provides a string image for this object.
   */
  public String toString() {
    return "(" + super.toString() +
      ",driver=" + driver + 
      ",driverKey=" + driverKey + ")";
  }
}
