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
 * Notification reporting the end of a driver execution.
 *
 * @author	Lacourte Serge
 * @version	v1.0
 *
 * @see		Driver
 */
public class DriverDone extends Notification {

public static final String RCS_VERSION="@(#)$Id: DriverDone.java,v 1.2 2000-08-01 09:13:27 tachkeni Exp $"; 


  protected int driver;		/** identifies the terminated driver */

  /**
   * Creates a notification to be sent.
   *
   * @param driver	identifies the terminated driver
   */
  public DriverDone(int driver) {
    this.driver = driver;
  }

  /**
   * Accesses read only property.
   */
  public int getDriver() {
    return driver;
  }


  /**
   * Provides a string image for this object.
   */
  public String toString() {
    return "(" + super.toString() +
      ",driver=" + driver + ")";
  }
}
