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

import org.apache.log4j.Category;
import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;

import org.objectweb.util.monolog.wrapper.log4j.MonologLoggerFactory;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;
import org.objectweb.util.monolog.api.LoggerFactory;

import org.objectweb.util.monolog.wrapper.common.Configurable;

/**
 * This class handles the debug traces.
 */
public class Debug {
  /** RCS version number of this file: $Revision: 1.2 $ */
  public static final String RCS_VERSION="@(#)$Id: Debug.java,v 1.2 2002-06-06 10:27:13 jmesnil Exp $";

  /** Property name for A3 debug configuration filename */
  public final static String DEBUG_FILE_PROPERTY = "fr.dyade.aaa.DEBUG_FILE";
  /** Default filename for A3 debug configuration */
  public final static String DEFAULT_DEBUG_FILE = "a3debug.cfg";

  /** Directory holding the debug files */
  public static File directory = null;
  /** */
  private static LoggerFactory factory;

  /**
   * Initializes the package.
   *
   * @param serverId	this server id
   */
  public static void init(short serverId) {
    boolean basic = false;

    String debugFileName = System.getProperty(DEBUG_FILE_PROPERTY,
                                              DEFAULT_DEBUG_FILE);

    // Instanciate the MonologLoggerFactory
    factory = (LoggerFactory) new MonologLoggerFactory();

    try {
      Properties prop = new Properties();
      prop.put(Configurable.LOG_CONFIGURATION_TYPE,
               Configurable.PROPERTY);
      prop.put(Configurable.LOG_CONFIGURATION_FILE,
               debugFileName);
      prop.put(Configurable.LOG_CONFIGURATION_FILE_USE_CLASSPATH, "true");
      ((MonologLoggerFactory) factory).configure(prop);
    } catch (Exception exc) {
      try {
        ((MonologLoggerFactory)factory).configure(null);
      } catch (Exception e) {
        System.err.println("Unable to configure monolog wrapper");
        System.exit(1);
      }
    }
    
    Category root = Category.getRoot();
    if (serverId >= 0) {
      try {
        // Try to create local appender if defined...
        FileAppender local = (FileAppender) root.getAppender("local");
        File auditFile = new File("server#" + serverId + ".audit");
        local.setFile(auditFile.getCanonicalPath());
      } catch (Exception exc) { }
    }
  }

  public static Logger getLogger(String topic) {
    if (factory == null) init((short) -1);
    return factory.getLogger(topic);
  }
}
