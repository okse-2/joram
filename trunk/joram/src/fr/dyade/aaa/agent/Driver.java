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
import fr.dyade.aaa.util.*;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

/**
 * Internal class to catch the end of the thread running the driver.
 */
class ThreadFinalizer implements Runnable {
  /** RCS version number of this file: $Revision: 1.11 $ */
  public static final String RCS_VERSION="@(#)$Id: Driver.java,v 1.11 2002-10-21 08:41:13 maistrfr Exp $";

  /** driver to start */
  Driver driver;

  /**
   * Allocates a new daemon for specified driver.
   *
   * @param driver	driver to start
   */
  ThreadFinalizer(Driver driver) {
    this.driver = driver;
  }

  /**
   * Starts the driver. Catches the driver thread termination and calls
   * the driver <code>end</code> function.
   */
  public void run() {
    try {
      driver.logmon.log(BasicLevel.DEBUG,
                        driver.getName() + " start");
      driver.run();
    } catch (ThreadDeath death) {
      // the thread has been killed, prints no error message
      throw death;
    } catch (Throwable exc) {
      driver.logmon.log(BasicLevel.ERROR,
                        driver.getName() + " failed", exc);
    } finally {
      driver.reset();
      driver.logmon.log(BasicLevel.DEBUG,
                        driver.getName() + "end");
      driver.end();
    }
  }
}


/**
 * Abstract base class for drivers such as <code>DriverIn</code> used by
 * <code>ProxyAgent</code>.
 * <p>
 * Multithreading in the agent server is special, as the code executed
 * outside of the main engine thread is no longer part of a transaction.
 * The class <code>Driver</code> has been designed to help programming in
 * a separate thread in the agent server.
 * <p>
 * A key difference between the main thread and a driver thread is the semantics
 * of sending a notification. When in the main thread of the agent server,
 * a notification is sent as part of an agent reaction. It is then part of
 * a transaction, and is actually sent only when the transaction commits.
 * When in a separate thread, the <code>directSendto</code> function of class
 * <code>Channel</code> is used instead, actually creating a new transaction
 * for the single purpose of sending the notification. When the function returns
 * the notification sending has been committed. The function <code>sendTo</code>
 * in this class encapsulates the call to the special <code>Channel</code>
 * function, and should be used by derived classes.
 * <p>
 * This class is designed to be derived. Derived classes should define the
 * function <code>run</code>, providing the actual code to be executed in the
 * separate thread, and may define the function <code>end</code>, providing
 * finalizing code executed when the thread terminates. The <code>end</code>
 * function is always called, even when the <code>run</code> function raises
 * an exception.
 *
 * @author	Lacourte Serge
 * @version	v1.1
 */
public abstract class Driver {
  /** RCS version number of this file: $Revision: 1.11 $ */
  public static final String RCS_VERSION="@(#)$Id: Driver.java,v 1.11 2002-10-21 08:41:13 maistrfr Exp $"; 

  /** separate thread running the driver */
  protected Thread thread;
  /** identifier local to the driver creator */
  protected int id;
  /**
   * Boolean variable used to stop the driver properly. If this variable is
   * true then it indicates that the driver is stopping.
   */ 
  volatile boolean isRunning = false;
  volatile boolean canStop = false;

  protected Logger logmon = null;
  protected String name = null;

  /**
   * Constructor.
   *
   * @param id		identifier local to the driver creator
   */
  protected Driver(int id) {
    thread = null;
    this.id = id;
    isRunning = true;

    // Get the logging monitor from current server MonologLoggerFactory
    // It should be overloaded in subclass in order to specialize traces.
    String classname = getClass().getName();
    logmon = Debug.getLogger(Debug.A3Proxy + '.' +
      classname.substring(classname.lastIndexOf('.') +1));

    this.name = classname + '#' + id;
  }

  /**
   * Constructor with default id.
   */
  protected Driver() {
    this(0);
    isRunning =true;
  }

  /**
   * Returns name of driver, actually classname and driver id. It should
   * be overloaded in subclass to take in account the proxy name.
   */
  public String getName() {
    return name;
  }

  /**
   * Provides a string image for this object.
   *
   * @return	printable image of this object
   */
  public String toString() {
    return "(" + getClass().getName() +
      ",name=" + getName() +
      ",id=" + id + ")" +
      ",isRunning=" + isRunning +
      ",canStop=" + canStop;
  }

  /**
   * Actually executes the driver code.
   * To be defined in derived classes.
   * <p>
   * Beware: this code is executed in a separate thread, outside from any
   * transaction. Notifications may be sent using function <code>sendTo</code>,
   * and they will actually be sent as soon as the function is called; there is
   * no atomic treatment as there is in an agent reaction.
   *
   * @exception Exception
   *	unspecialized exception
   */
  public abstract void run() throws Exception;

  /**
   * Finalizes driver; called in <code>finally</code> clause of driver thread.
   * May be overloaded in derived classes.
   */
  protected void end() {}

  /**
   * Starts the driver execution.
   */
  public void start() {
    thread = new Thread(new ThreadFinalizer(this), getName());
    thread.setDaemon(true);
    thread.start();
  }

  /**
   * Nullify thread variable. To be used by ThreadFinalizer.
   */
  synchronized void reset() {
    thread = null;
  }

  /**
   * Stops the driver execution.
   */
  public synchronized void stop() {
    if (thread == null) return;
    isRunning = false;
    if (canStop) {
	thread.interrupt();
	close();
    }
    thread = null;
  }

  public abstract void close();

  /**
   * Sends a notification to an agent.
   * <p>
   * Provides a similar function as the <code>sendTo</code> function in
   * <code>Agent</code> class, except that the notification is sent directly
   * via a <code>directSendTo</code> method.
   *
   * @param to		target agent
   * @param not		notification to send
   */
  protected final void sendTo(AgentId to, Notification not) throws IOException {
    Channel.sendTo(to, not);
  }
}
