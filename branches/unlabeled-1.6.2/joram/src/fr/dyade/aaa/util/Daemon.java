/*
 * Copyright (C) 2002 - ScalAgent Distributed Technologies
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
 * fr.dyade.aaa.ip, fr.dyade.aaa.joram, fr.dyade.aaa.mom, and
 * fr.dyade.aaa.util, released May 24, 2000.
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
 *
 * The present code contributor is ScalAgent Distributed Technologies.
 */
package fr.dyade.aaa.util;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

/**
 * The Daemon class represents a basic active component in a server. It
 * provides usefull code to start and safely stop inner Thread.
 * <p>
 * Main loop of daemon:
 * <p><hr>
 * <blockquote><pre>
 *  try {
 *    while (running) {
 *	canStop = true;
 *
 *	// Get a notification, then execute the right reaction.
 *	try {
 *	  // Get a request
 *	  ...
 *	} catch (InterruptedException exc) {
 *	  continue;
 *	}
 *	
 *	canStop = false;
 *
 *	// executes the request
 *	...
 *    }
 *  } finally {
 *    finish();
 *  }
 * </pre></blockquote>
 */
public abstract class Daemon implements Runnable {
  /** RCS version number of this file: $Revision: 1.6.2.1 $ */
  public static final String RCS_VERSION="@(#)$Id: Daemon.java,v 1.6.2.1 2002-06-04 08:17:49 jmesnil Exp $";

  /**
   * Tests if this daemon is alive.
   * @return	true if this daemon is alive; false otherwise.
   */
  public synchronized boolean isRunning() {
    return ((thread != null) && thread.isAlive());
  }

  /**
   * Boolean variable used to stop the daemon properly. The daemon tests
   * this variable between each request, and stops if it is false.
   * @see start
   * @see stop
   */
  protected volatile boolean running;
  /**
   * Boolean variable used to stop the daemon properly. If this variable
   * is true then the daemon is waiting for a long time and it can interupted,
   * else it handles a request and it will exit after (it tests the
   * <code>{@link #running running}</code> variable between
   * each reaction)
   */
  protected volatile boolean canStop;
  /** The active component of this daemon. */ 
  protected Thread thread = null;
  /** The <code>daemon</code>'s name. */ 
  private String name;
  /** The <code>daemon</code>'s nature. */
  private boolean daemon = false;

  /**
   * Returns this <code>daemon</code>'s name.
   *
   * @return this <code>daemon</code>'s name.
   */
  public final String getName() {
    return name;
  }

  /**
   * Returns a string representation of this daemon.
   *
   * @return	A string representation of this daemon. 
   */
  public String toString() {
    StringBuffer strbuf = new StringBuffer();

    strbuf.append(getName())
      .append(" [").append(running).append("/")
      .append(canStop).append("]");

    if (thread != null) strbuf.append(" -> ").append(thread.isAlive());

    return strbuf.toString();
  }

  protected Logger logmon = null;

  /**
   * Allocates a new Daemon object.
   *
   * @param name	the name of the new Daemon
   */
  protected Daemon(String name) {
    this.name = name;

    // Get the logging monitor from current server MonologMonitorFactory
    logmon = Debug.getLogger(getClass().getName() + '.' + name);
    logmon.log(BasicLevel.DEBUG, getName() + ", created.");

    running = false;
    canStop = false;
    thread = null;
  }

  public void setDaemon(boolean daemon) {
    if (running || ((thread != null) && thread.isAlive())) {
      throw new IllegalThreadStateException("already started");
    }
    this.daemon = daemon;
  }

  /**
   * Causes this daemon to begin execution. A new thread is created to
   * execute the run method.
   *
   * @exception IllegalThreadStateException
   *		If the daemon was already started.
   */
  public synchronized void start() {
    if ((thread != null) && thread.isAlive()) {
      logmon.log(BasicLevel.WARN, getName() + ", already started.");
      throw new IllegalThreadStateException("already started");
    }

    thread = new Thread(this, getName());
    thread.setDaemon(daemon);

    running = true;
    canStop = true;
    thread.start();

    logmon.log(BasicLevel.DEBUG, getName() + ", started.");
  }

  /**
   * Releases any resources attached to this daemon. Be careful, its method
   * should be called more than one time.
   */
  protected abstract void close();

  /**
   * Interupts a thread that waits for long periods. In some cases, we must
   * use application specific tricks. For example, if a thread is waiting on
   * a known socket, we have to close the socket to cause the thread to return
   * immediately. Unfortunately, there really isn't any technique that works
   * in general.
   */
  protected abstract void shutdown();

  final protected void finish() {
    running = false;
    close();
    logmon.log(BasicLevel.DEBUG, getName() + ", ended");
  }

  /**
   * Forces the daemon to stop executing. This method notifies thread that
   * it should stop running, if the thread is waiting it is first interupted
   * then the shutdown method is called to close all ressources.
   */
  public synchronized void stop() {
    logmon.log(BasicLevel.DEBUG, getName() + ", stops.");
    running = false;
    if (thread != null) {
      while (thread.isAlive()) {
        if (canStop) {

          if (thread.isAlive())
            thread.interrupt();

          shutdown();
        }
        try {
          thread.join(1000L);
        } catch (InterruptedException exc) {
          continue;
        }
      }
      finish();
      thread = null;
    }
  }
}
