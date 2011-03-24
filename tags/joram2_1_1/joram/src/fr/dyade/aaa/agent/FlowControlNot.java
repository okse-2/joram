/*
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
 *
 * The contents of this file are subject to the Joram Public License,
 * as defined by the file JORAM_LICENSE.TXT 
 * 
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License on the Objectweb web site
 * (www.objectweb.org). 
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific terms governing rights and limitations under the License. 
 * 
 * The Original Code is Joram, including the java packages fr.dyade.aaa.agent,
 * fr.dyade.aaa.util, fr.dyade.aaa.ip, fr.dyade.aaa.mom, and fr.dyade.aaa.joram,
 * released May 24, 2000. 
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
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

  /** RCS version number of this file: $Revision: 1.6 $ */
  public static final String RCS_VERSION="@(#)$Id: FlowControlNot.java,v 1.6 2001-08-31 08:13:57 tachkeni Exp $";

  /** id of <code>DriverIn</code> agent issuing notification, when applicable */
  int driverId;

  /** 
   * Key of the <code>DriverIn</code> issuing the notification,
   * in a multi-connections context.
   */
  int driverKey;


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