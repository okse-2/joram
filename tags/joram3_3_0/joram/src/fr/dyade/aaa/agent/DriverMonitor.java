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
 * The <code>DriverMonitor</code> class is used by <code>ProxyAgent</code>
 * instances managing multi-connections for holding the elements composing a
 * connection set.
 */
public class DriverMonitor {
  /** RCS version number of this file: $Revision: 1.6 $ */
  public static final String RCS_VERSION="@(#)$Id: DriverMonitor.java,v 1.6 2002-12-11 11:22:12 maistrfr Exp $";

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
