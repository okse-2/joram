/*
 * Copyright (C) 2002 - 2003 ScalAgent Distributed Technologies
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
 * Contributor(s): Douglas S. Jackson
 */
package fr.dyade.aaa.util;

import java.io.*;
import java.util.*;

import org.objectweb.util.monolog.api.*;

import org.objectweb.util.monolog.wrapper.common.Configurable;

/**
 * This class handles the debug traces.
 */
public class Debug {
  /** RCS version number of this file: $Revision: 1.9 $ */
  public static final String RCS_VERSION="@(#)$Id: Debug.java,v 1.9 2003-09-11 09:54:24 fmaistre Exp $";

  /** */
  public final static String DEBUG_CONFIGURE_PROPERTY = "fr.dyade.aaa.DEBUG_CONFIGURE";
  /** */
  public final static String DEFAULT_DEBUG_CONFIGURE = "true";
  /** Property name for monolog logger factory implementation class */
  public final static String LOGGER_FACTORY_PROPERTY = "LOGGER_FACTORY";
  /** Default classname for monolog logger factory implementation */
  public final static String DEFAULT_LOGGER_FACTORY = "org.objectweb.util.monolog.wrapper.log4j.MonologLoggerFactory";
  /**
   * Property name for A3 debug configuration directory.
   * If not defined, the configuration file is searched from the search
   * path used to load classes.
   */
  public final static String DEBUG_DIR_PROPERTY = "fr.dyade.aaa.DEBUG_DIR";
  /** Property name for A3 debug configuration filename */
  public final static String DEBUG_FILE_PROPERTY = "fr.dyade.aaa.DEBUG_FILE";
  /** Default filename for A3 debug configuration */
  public final static String DEFAULT_DEBUG_FILE = "a3debug.cfg";

  /** */
  protected static LoggerFactory factory;

  public static void reinit() throws Exception {
    initialize();
  }

  protected static void init() throws Exception {
    try {
      initialize();
    } catch(Exception exc){
      System.err.println("Monolog configuration file not found, use defaults");
      exc.printStackTrace();
      try {
        ((Configurable) factory).configure(null);
        Logger[] loggers = factory.getLoggers();
        for (int i=0; i<loggers.length; i++) {
          loggers[i].setIntLevel(BasicLevel.ERROR);
        }
      } catch (Exception e) {
        System.err.println("Unable to configure monolog wrapper");
        throw new Exception("Unable to configure monolog wrapper");
      }
    }
  }

  private static PrivateLogger logger = null;

  private static class PrivateLogger implements Logger {
    PrivateLogger() {}

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
    public byte getType() {
      return CONSOLE_HANDLER_TYPE;
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
    public void setIntLevel(int l) {}

    /**
     *  Sets the Level attribute of the LoggerImpl object
     *
     * @param  l  The new Level value
     */
    public void setLevel(Level l) {}

    /**
     *  Gets the CurrentIntLevel attribute of the LoggerImpl object
     *
     * @return    The CurrentIntLevel value
     */
    public int getCurrentIntLevel() {
      return BasicLevel.DEBUG;
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
    public boolean isLoggable(int l) {
      return true;
    }

    /**
     *  Gets the Loggable attribute of the LoggerImpl object
     *
     * @param  l  Description of Parameter
     * @return    The Loggable value
     */
    public boolean isLoggable(Level l) {
      return true;
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
      System.err.println(o.toString());
    }
    /**
     * Log a message, with no arguments.
     * If the logger is currently enabled for the given message level then the
     * given message is treated
     */
    public void log(Level l, Object o) {
      System.err.println(o.toString());
    }

    /**
     * Log a message, with a throwable arguments which can represent an 
     * error or a context..
     */
    public void log(int level, Object o, Throwable t) {
      System.err.println(o.toString() + ":" + t.toString());
    }
    /**
     * Log a message, with a throwable arguments which can represent an 
     * error or a context..
     */
    public void log(Level l, Object o, Throwable t) {
      System.err.println(o.toString() + ":" + t.toString());
    }


    /**
     * Log a message, with a location and method arguments. The location
     * parameter can be the object instance which logs the event, or a string
     * representation of the object.
     * The method argument can be a java.lang.reflect.Method or a string which
     * represents the method name.
     */
    public void log(int level, Object o, Object location, Object method) {
      System.err.println(location.toString() + "." + method.toString() + "(...) :"
                         + o.toString());
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
      System.err.println(location.toString() + "." + method.toString() + "(...) :"
                         + o.toString());
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
      System.err.println(location.toString() + "." + method.toString() + "(...) :"
                         + o.toString() + " " + t.toString());
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
      System.err.println(location.toString() + "." + method.toString() + "(...) :"
                         + o.toString() + " " + t.toString());
    }

    /** Enables this logger */
    public void turnOn() {}

    /** Disables this logger */
    public void turnOff() {}

    public String toString() {
      return "Private ScalAgent D.T. default implementation";
    }
  }

  /**
   * Initializes the package.
   */
  private static void initialize() throws Exception {
    boolean doConfiguration =
      System.getProperty(DEBUG_CONFIGURE_PROPERTY,
                         DEFAULT_DEBUG_CONFIGURE).equals("true");
    String debugDir = System.getProperty(DEBUG_DIR_PROPERTY);
    String debugFileName = System.getProperty(DEBUG_FILE_PROPERTY,
                                              DEFAULT_DEBUG_FILE);
    if (doConfiguration && (debugDir != null)) {
      File debugFile = new File(debugDir, debugFileName);
      try {
        if ((debugFile != null) &&
            (debugFile.length() != 0) &&
            debugFile.exists() &&
            debugFile.isFile()) {
          debugFileName = debugFile.getPath();
        } else {
          throw new IOException();
        }
      } catch (IOException exc) {
        // debug configuration file seems not exist, search it from the
        // search path used to load classes.
        System.err.println("Unable to find \"" + debugFile.getPath() + "\".");
        debugDir = null;
      }
    }

    // Instanciate the MonologLoggerFactory
    String loggerFactory = System.getProperty(LOGGER_FACTORY_PROPERTY,
                                              DEFAULT_LOGGER_FACTORY);

//     org.objectweb.util.monolog.wrapper.common.AbstractFactory.debug = true;

    try {
      factory = (LoggerFactory) Class.forName(loggerFactory).newInstance();
    } catch(Exception exc) {
      System.err.println("Unable to instantiate monolog wrapper");
      exc.printStackTrace();
    }

    if (doConfiguration) {
      Properties prop = new Properties();
      prop.put(Configurable.LOG_CONFIGURATION_TYPE, Configurable.PROPERTY);
      prop.put(Configurable.LOG_CONFIGURATION_FILE, debugFileName);
      if (debugDir == null) {
        prop.put(Configurable.LOG_CONFIGURATION_FILE_USE_CLASSPATH, "true");
      } else {
        prop.put(Configurable.LOG_CONFIGURATION_FILE_USE_CLASSPATH, "false");
      }
      ((Configurable) factory).configure(prop);
    }
  }

  public static Logger getLogger(String topic) {
    if (factory == null) {
      try {
        init();
      } catch (Exception exc) {
        if (logger == null)
          logger = new PrivateLogger();
        return logger;
      }
    }
    return factory.getLogger(topic);
  }

  /**
   * Set the monolog Loggerfactory
   * @param loggerFactory the monolog LoggerFactory
   */
  public static void setLoggerFactory(LoggerFactory loggerFactory) {
    factory = loggerFactory;
  }
}
