/*
 * Copyright (C) 2001 SCALAGENT
 */
package fr.dyade.aaa.util;

import org.objectweb.monolog.api.BasicLevel;
import org.objectweb.monolog.api.Monitor;

import fr.dyade.aaa.agent.*;

/**
 * The Daemon class represents a basic active component in an agent server.
 * It provides usefull code to start and safely stop inner Thread.
 * <p>
 * main loop of daemon:
 * <p><hr>
 * <blockquote><pre>
 *  try {
 *    while (isRunning) {
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
 *    isRunning = false;
 *    thread = null;
 *    // Close any ressources no longer needed, eventually stop the
 *    // enclosing component.
 *    shutdown();
 *  }
 * </pre></blockquote>
 */
public abstract class Daemon implements Runnable {
  /** RCS version number of this file: $Revision: 1.3 $ */
  public static final String RCS_VERSION="@(#)$Id: Daemon.java,v 1.3 2002-01-16 12:46:47 joram Exp $";

  /**
   * Tests if this daemon is alive.
   * @return	true if this daemon is alive; false otherwise.
   */
  public boolean isRunning() {
    return running;
  }

  /**
   * Boolean variable used to stop the daemon properly. The dameon tests
   * this variable between each request, and stops if it is false.
   * @see start
   * @see stop
   */
  protected volatile boolean running;
  /**
   * Boolean variable used to stop the daemon properly. If this
   * variable is true then the daemon is waiting and it can interupted,
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

  protected Monitor logmon = null;

  /**
   * Allocates a new Daemon object.
   *
   * @param name	the name of the new Daemon
   */
  protected Daemon(String name) {
    this.name = name;

    // Get the logging monitor from current server MonologMonitorFactory
    logmon = Debug.getMonitor(Debug.A3Daemon + '.' + name);
    logmon.log(BasicLevel.DEBUG, getName() + ", created.");

    running = false;
    canStop = false;
    thread = null;
  }

  public void setDaemon(boolean daemon) {
    this.daemon = daemon;
  }

  /**
   * Causes this daemon to begin execution. A new thread is created to
   * execute the run method.
   */
  public void start() {
    if (running) return;

    thread = new Thread(this, getName());
    thread.setDaemon(daemon);

    running = true;
    canStop = true;
    thread.start();

    logmon.log(BasicLevel.DEBUG, getName() + ", started.");
  }

  public abstract void shutdown();

  /**
   * Forces the daemon to stop executing. This method notifies thread that
   * it should stop running, if the thread is waiting it is first interupted
   * then the shutdown method is called to close all ressources.
   */
  public void stop() {
    running = false;

    logmon.log(BasicLevel.DEBUG, getName() + ", stopped.");

    if (thread == null)
      // The session is idle.
      return;

    if (canStop) {
      if (thread.isAlive()) thread.interrupt();
      shutdown();
    }

    thread = null;
  }
}
