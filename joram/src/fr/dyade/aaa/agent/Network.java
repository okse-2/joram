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
import java.util.Vector;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.util.Arrays;

abstract class Network implements MessageConsumer {
  /** RCS version number of this file: $Revision: 1.9 $ */
  public static final String RCS_VERSION="@(#)$Id: Network.java,v 1.9 2002-03-26 16:08:39 joram Exp $";

  /** Time between two activation of watch-dog thread (default 10 seconds) */
  final static long WDActivationPeriod = 10000L; // 10 seconds
  /** Number of try at stage 1 (default 30 times) */
  final static int  WDNbRetryLevel1 = 30;
  /** Time between two sending at stage 1 (default 10 seconds) */
  final static long WDRetryPeriod1 = 10000L;	 // 10 seconds
  /** Number of try at stage 2 (default 25 times) */
  final static int  WDNbRetryLevel2 = 55;
  /** Time between two sending at stage 2 (default 1 minutes) */
  final static long WDRetryPeriod2 = 60000L;	 // 1 minutes
  /** time between two sending at stage 3 (default 15 minutes) */
  final static long WDRetryPeriod3 = 900000L;	 // 15 minutes

  protected Logger logmon = null;

  /**
   * Reference to the current network component in order to be used
   * by inner daemon's.
   */
  Network network;
  /** The domain name. */
  String name;
  /** The communication port. */
  int port;
  /** The <code>MessageQueue</code> associated with this network component. */
  MessageQueue qout;
  /**
   * List of id. for all servers in the domain, this list is sorted.
   * Be careful, this array is shared with the <code>MatrixClock</code>
   * components.
   */
  short[] servers;

  /**
   * Returns this session's name.
   *
   * @return this session's name.
   */
  public final String getName() {
    return name;
  }

  /**
   * Returns a string representation of this consumer.
   *
   * @return	A string representation of this consumer. 
   */
  public String toString() {
    StringBuffer strbuf = new StringBuffer();

    strbuf.append(getName()).append("[")
      .append(getClass().getName()).append("]\t")
      .append("qout=[").append(qout.size()).append("]");

    return strbuf.toString();
  }

  /**
   * Creates a new network component. This simple constructor is required in
   * order to use <code>Class.newInstance()</code> method during configuration.
   * The configuration of component is then done by <code>init</code> method.
   */
  Network() {
    network = this;
    qout = new MessageQueue();
  }

  /**
   * Initializes a new network component. This method is used in order to
   * easily creates and configure a Network component from a class name.
   * So we can use the <code>Class.newInstance()</code> method for create
   * (whitout any parameter) the component, then we can initialize it with
   * this method.<br>
   * This method initializes the logical clock for the domain.
   *
   * @param name	The domain name.
   * @param port	The listen port.
   * @param servers	The list of servers directly accessible from this
   *			network interface.
   */
  public abstract void init(String name, int port, short[] servers) throws Exception;

  /**
   * Validates all messages pushed in queue during transaction session.
   */
  public void validate() {
    qout.validate();
  }

  /**
   * Invalidates all messages pushed in queue during transaction session.
   */
  public void invalidate() {
    qout.invalidate();
  }

  public MessageQueue getQueue() {
    return qout;
  }

  /**
   * Returns the index of the specified server.
   */
  final int index(short id) {
    return Arrays.binarySearch(servers, id);
  }

  /**
   * Wakes up the watch-dog thread.
   */
  public abstract void wakeup();
}
