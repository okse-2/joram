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

import java.io.*;

abstract class Network {
  /** RCS version number of this file: $Revision: 1.3 $ */
  public static final String RCS_VERSION="@(#)$Id: Network.java,v 1.3 2000-10-05 15:15:21 tachkeni Exp $";

  /** Time between two activation of watch-dog thread (default 10 seconds) */
  final static long WDActivationPeriod = 10000L; // 10 seconds
  /** Number of try at stage 1 (default 6 times) */
  final static int  WDNbRetryLevel1 = 6;
  /** Time between two sending at stage 1 (default 15 seconds) */
  final static long WDRetryPeriod1 = 15000L;	 // 15 seconds
  /** Number of try at stage 2 (default 12 times) */
  final static int  WDNbRetryLevel2 = 12;
  /** Time between two sending at stage 2 (default 2 minutes) */
  final static long WDRetryPeriod2 = 120000L;	 // 2 minutes
  /** time between two sending at stage 3 (default 30 minutes) */
  final static long WDRetryPeriod3 = 1800000L;	 // 30 minutes

  /** the matrix clock associated to this network component */
  MatrixClock mclock;

  /**
   * Creates a new network component.
   */
  Network(MatrixClock mclock) {
    this.mclock = mclock;
  }

  /**
   *  Adds a message in network's receive list. This method is used by
   * <code>Server</code> during initialisation to restore the network state
   * from persistent storage.
   *
   * @param msg		the message
   */
  abstract void addRecvMessage(Message msg);

  /**
   * Causes this network component to begin execution.
   *
   * @param qin		the local message queue (input of engine).
   * @param qout	the output message queue (toward other servers).
   */
  abstract void start(MessageQueue qin,
		      MessageQueue qout) throws IOException;

  /**
   * Wakes up the watch-dog thread.
   */
  abstract void wakeup();

  /**
   * Forces the network component to stop executing.
   */
  abstract void stop();
}
