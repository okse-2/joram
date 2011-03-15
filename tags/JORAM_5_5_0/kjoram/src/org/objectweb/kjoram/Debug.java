/*
 * Copyright (C) 2008 ScalAgent Distributed Technologies
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA.
 *
 * Original developer: ScalAgent Distributed Technologies
 * Contributor(s): 
 */
package org.objectweb.kjoram;

import java.io.PrintStream;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Level;
import org.objectweb.util.monolog.api.Logger;
import org.objectweb.util.monolog.api.LoggerFactory;

/**
 * This class handles the debug traces.
 */
public class Debug {
  /** flag used to remove huge logging */
  public final static boolean debug = true;

  /** */
  protected static LoggerFactory factory;

  private static PrivateLogger logger = null;

  public static Logger getLogger(String topic) {
    if (logger == null)
      logger = new PrivateLogger();
    return logger;
  }

  public static void setLoggerLevel(String topic, int level) {
    getLogger(topic).setIntLevel(level);
  }

  /**
   * Set the monolog Loggerfactory
   * @param loggerFactory the monolog LoggerFactory
   */
  public static void setLoggerFactory(LoggerFactory loggerFactory) {
    factory = loggerFactory;
  }

  private static class PrivateLogger implements Logger {
    PrivateLogger() {
      BasicLevel.FATAL = 1000;
      BasicLevel.ERROR = 800;
      BasicLevel.WARN = 600;
      BasicLevel.INFO = 400;
      BasicLevel.DEBUG = 200;

      level = BasicLevel.WARN;
      out = System.err;
    }

    int level;

    PrintStream out = null;

    // Interface Handler

    /**
     * It retrieves the name of the handler
     */
    public String getName() {
      return toString();
    }

    /**
     * It assigns the name of the handler
     */
    public void setName(String name) {}

    /**
     * It retrieves the Handler type
     */
    public String getType() {
      return "console";
    }

    /**
     * It retrieves the attributes of the handler
     */
    public String[] getAttributeNames() {
      return null;
    }

    /**
     * It retrieves the value of an attribute value of the handler.
     * @param key is an attribute name
     */
    public Object getAttribute(String name) {
      return null;
    }

    /**
     * It assigns an attributte to the handler.
     * @param key is the attribute name
     * @param value is the attribute value
     * @return the old value is the attribute was already defined
     */
    public Object setAttribute(String name, Object value) {
      return null;
    }

    // Interface Logger

    /**
     *  Sets the IntLevel attribute of the LoggerImpl object
     *
     * @param  l  The new IntLevel value
     */
    public void setIntLevel(int l) {
      level = l;
    }

    /**
     *  Sets the Level attribute of the LoggerImpl object
     *
     * @param  l  The new Level value
     */
    public void setLevel(Level l) {
      level = l.getIntValue();
    }

    /**
     *  Gets the CurrentIntLevel attribute of the LoggerImpl object
     *
     * @return    The CurrentIntLevel value
     */
    public int getCurrentIntLevel() {
      return level;
    }

    /**
     *  Gets the CurrentLevel attribute of the LoggerImpl object
     *
     * @return    The CurrentLevel value
     */
    public Level getCurrentLevel() {
      return null;
    }

    /**
     *  Gets the Loggable attribute of the LoggerImpl object
     *
     * @param  l  Description of Parameter
     * @return    The Loggable value
     */
    public boolean isLoggable(int level) {
      if (level >= getCurrentIntLevel())
        return true;
      return false;
    }

    /**
     *  Gets the Loggable attribute of the LoggerImpl object
     *
     * @param  l  Description of Parameter
     * @return    The Loggable value
     */
    public boolean isLoggable(Level l) {
      if (l.getIntValue() >= getCurrentIntLevel())
        return true;
      return false;
    }

    /**
     *  Gets the On attribute of the LoggerImpl object
     *
     * @return    The On value
     */
    public boolean isOn() {
      return true;
    }
	
    /**
     * Log a message, with no arguments.
     * If the logger is currently enabled for the given message level then the
     * given message is treated
     */
    public void log(int level, Object o) {
      if (level >= getCurrentIntLevel())
        out.println(o.toString());
    }
    /**
     * Log a message, with no arguments.
     * If the logger is currently enabled for the given message level then the
     * given message is treated
     */
    public void log(Level l, Object o) {
      if (l.getIntValue() >= getCurrentIntLevel())
        out.println(o.toString());
    }

    /**
     * Log a message, with a throwable arguments which can represent an 
     * error or a context..
     */
    public void log(int level, Object o, Throwable t) {
      if (level >= getCurrentIntLevel())
        out.println(o.toString() + ":" + t.toString());
    }
    /**
     * Log a message, with a throwable arguments which can represent an 
     * error or a context..
     */
    public void log(Level l, Object o, Throwable t) {
      if (l.getIntValue() >= getCurrentIntLevel())
        out.println(o.toString() + ":" + t.toString());
    }


    /**
     * Log a message, with a location and method arguments. The location
     * parameter can be the object instance which logs the event, or a string
     * representation of the object.
     * The method argument can be a java.lang.reflect.Method or a string which
     * represents the method name.
     */
    public void log(int level, Object o, Object location, Object method) {
      if (level >= getCurrentIntLevel())
        out.println(location.toString() + "." + method.toString() +
                           "(...) :" + o.toString());
    }
    /**
     * Log a message, with a location and method arguments. The location
     * parameter can be the object instance which logs the event, or a string
     * representation of the object. 
     * The method argument can be a java.lang.reflect.Method or a string which
     * represents the method name.
     */
    public void log(Level l, Object o, Object location,
                    Object method) {
      if (l.getIntValue() >= getCurrentIntLevel())
        out.println(location.toString() + "." + method.toString() +
                           "(...) :" + o.toString());
    }

    /**
     * Log a message, with a location, method and throwable arguments. 
     * The location parameter can be the object instance which logs the 
     * event, or a string representation of the object.. 
     * The method argument can be a java.lang.reflect.Method or a string which
     * represents the method name.
     * The throwable parameter permits to log an Exception. 
     */
    public void log(int level, Object o, Throwable t, Object location, Object method) {
      if (level >= getCurrentIntLevel())
        out.println(location.toString() + "." + method.toString() +
                           "(...) :" + o.toString() + " " + t.toString());
    }
    /**
     * Log a message, with a location, method and throwable arguments. 
     * The location parameter can be the object instance which logs the 
     * event, or a string representation of the object.. 
     * The method argument can be a java.lang.reflect.Method or a string which
     * represents the method name.
     * The throwable parameter permits to log an Exception. 
     */
    public void log(Level l, Object o, Throwable t, Object location,
                    Object method) {
      if (l.getIntValue() >= getCurrentIntLevel())
        out.println(location.toString() + "." + method.toString() +
                           "(...) :" + o.toString() + " " + t.toString());
    }

    /** Enables this logger */
    public void turnOn() {}

    /** Disables this logger */
    public void turnOff() {}

    public String toString() {
      return "Private ScalAgent D.T. default implementation";
    }
  }
}
