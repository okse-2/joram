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

import fr.dyade.aaa.util.*;
import java.io.*;


/**
 * A <code>TransientMonitor</code> object collects data for managing a separate
 * transient agent server. It is part of a <code>TransientManager</code> agent
 * which is responsible for handling communications with all transient agent
 * servers associated with the local persistent agent server.
 * <p>
 * A <code>TransientMonitor</code> object declares two threads for handling
 * incoming and outgoing notifications, from and to the transient agent server.
 * Those threads are encapsulated into a <code>TransDriverIn</code> object and
 * a <code>TransDriverOut</code> object.
 *
 * @author	Lacourte Serge
 * @version	v1.0
 *
 * @see		TransientManager
 * @see		TransDriverIn
 * @see		TransDriverOut
 */
class TransientMonitor implements Serializable {

  /** RCS version number of this file: $Revision: 1.3 $ */
  public static final String RCS_VERSION="@(#)$Id: TransientMonitor.java,v 1.3 2000-10-05 15:15:24 tachkeni Exp $";

  /** id of transient agent server */
  short serverId;

  /** input stream from transient agent server */
  transient ObjectInputStream ois;

  /** input driver */
  transient TransDriverIn driverIn;

  /** output stream to transient agent server */
  transient ObjectOutputStream oos;

  /** output driver */
  transient TransDriverOut driverOut;

  /** communication queue with output driver */
  protected Queue qout;


  /**
   * Constructor.
   *
   * @param serverId	id of transient agent server
   */
  public TransientMonitor(short serverId) {
    this.serverId = serverId;
    ois = null;
    driverIn = null;
    oos = null;
    driverOut = null;
    qout = new Queue();
  }

  /**
   * Provides a string image for this object.
   *
   * @return	printable image of this object
   */
  public String toString() {
    return "(" + getClass().getName() +
      ",serverId=" + serverId +
      ",qout=" + qout + ")";
  }

  /**
   * Creates and starts the monitor drivers.
   *
   * @param inId	input driver id
   * @param outId	output driver id
   * @param manager	id of manager agent
   */
  void createDrivers(int inId, int outId, AgentId manager) throws Exception {
    driverIn = new TransDriverIn(inId, manager, ois, 100);
    driverOut = new TransDriverOut(outId, manager, qout, oos);
    driverIn.start();
    driverOut.start();
  }

  /**
   * Sends a <code>TransientMessage</code> notification onto the driver output
   * stream.
   */
  void sendTo(TransientMessage msg) throws Exception {
    qout.push(msg);
  }

  /**
   * Reacts to the end of a driver execution.
   *
   * @param direction	TransientManager.INPUT_ID or TransientManager.OUTPUT_ID
   */
  void driverDone(int direction) throws Exception {
    switch (direction) {
    case TransientManager.INPUT_ID:
      if (ois != null) {
	ois.close();
	ois = null;
      }
      driverIn = null;
      // stops out driver
      if (driverOut != null) {
	driverOut.stop();
	driverOut = null;
      }
      break;
    case TransientManager.OUTPUT_ID:
      if (oos != null) {
	oos.close();
	oos = null;
      }
      driverOut = null;
      break;
    }
  }

  /**
   * Stops a transient agent server by closing its connection.
   */
  void stop() throws Exception {
    if (ois != null) {
      ois.close();
      ois = null;
    }
    if (driverIn != null) {
      driverIn.stop();
      driverIn = null;
    }
    if (oos != null) {
      oos.close();
      oos = null;
    }
    if (driverOut != null) {
      driverOut.stop();
      driverOut = null;
    }
  }
}
