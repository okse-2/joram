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

import java.io.*;
import java.util.*;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;
import org.objectweb.util.monolog.api.LoggerFactory;

import org.objectweb.util.monolog.wrapper.common.Configurable;

/**
 * This class handles the debug traces.
 */
public class Debug {
  /** RCS version number of this file: $Revision: 1.3 $ */
  public static final String RCS_VERSION="@(#)$Id: Debug.java,v 1.3 2002-10-21 08:41:14 maistrfr Exp $";

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

  /**
   * Initializes the package.
   */
  protected static void init() {
    String debugDir = System.getProperty(DEBUG_DIR_PROPERTY);
    String debugFileName = System.getProperty(DEBUG_FILE_PROPERTY,
                                              DEFAULT_DEBUG_FILE);
    if (debugDir != null) {
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
    try {
      factory = (LoggerFactory) Class.forName(loggerFactory).newInstance();
    } catch (Exception exc) {
      System.err.println("Unable to instantiate monolog wrapper");
      System.exit(1);
    }

    try {
      Properties prop = new Properties();
      prop.put(Configurable.LOG_CONFIGURATION_TYPE,
               Configurable.PROPERTY);
      prop.put(Configurable.LOG_CONFIGURATION_FILE,
               debugFileName);
      if (debugDir == null) {
        prop.put(Configurable.LOG_CONFIGURATION_FILE_USE_CLASSPATH, "true");
      } else {
        prop.put(Configurable.LOG_CONFIGURATION_FILE_USE_CLASSPATH, "false");
      }
      ((Configurable) factory).configure(prop);
    } catch (Exception exc) {
      try {
        ((Configurable) factory).configure(null);
        Logger[] loggers = factory.getLoggers();
        for (int i=0; i<loggers.length; i++) {
          loggers[i].setIntLevel(BasicLevel.ERROR);
        }
      } catch (Exception e) {
        System.err.println("Unable to configure monolog wrapper");
        System.exit(1);
      }
    }
  }

  public static Logger getLogger(String topic) {
    if (factory == null) init();
    return factory.getLogger(topic);
  }
}
