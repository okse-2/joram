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
 * A <code>DriverNotification</code> instance is destinated to or comes from a
 * given pair of drivers in the context of proxies managing multiple
 * connections.
 * <p>
 * A <code>DriverNotification</code> wraps a <code>Notification</code> 
 * associated to a <code>driverKey</code> parameter identifying a pair of
 * drivers belonging to a <code>ProxyAgent</code>. The wrapped
 * <code>Notification</code> come either from the <code>DriverIn</code> or
 * is destinated to the <code>DriverOut</code> of this pair.
 * 
 */
public class DriverNotification extends Notification
{
  /** The wrapped notification. */
  private Notification not;
  /** Key identifying a pair of drivers in a multi connections proxy. */
  private int driverKey;

  /**
   * Constructs a <code>DriverNotification</code> instance wrapping a given
   * <code>Notification</code> for a given connection set.
   *
   * @param not  Notification to wrap.
   * @param driverKey  Key identifying a proxy connection set.
   */
  public DriverNotification(int driverKey, Notification not)
  {
    this.not = not;
    this.driverKey = driverKey;
  }


  /**
   * Returns the <code>Notification</code> wrapped by this
   * <code>DriverNotification</code>.
   */
  public Notification getNotification() 
  {
    return not;
  }


  /** Returns the connection key of this <code>DriverNotification</code>. */
  public int getDriverKey() 
  {
    return driverKey;
  }

}
