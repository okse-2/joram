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

import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Category;
import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;

import org.objectweb.util.monolog.wrapper.log4j.MonologLoggerFactory;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;
import org.objectweb.util.monolog.api.LoggerFactory;

/**
 * This class handles the debug traces produced by 
 * a program which is not working inside an
 * agent server.
 */
public class ClientDebug {
  public final static String DEBUG_DIR_PROPERTY = "fr.dyade.aaa.client.DEBUG_DIR";
  public final static String DEFAULT_DEBUG_DIR = ".";
  public final static String DEBUG_FILE_PROPERTY = "fr.dyade.aaa.client.DEBUG_FILE";
  public final static String DEFAULT_DEBUG_FILE = "clientDebug.cfg";

  private static LoggerFactory factory;

  public static void init() {
    String debugDir = System.getProperty(DEBUG_DIR_PROPERTY,
                                         DEFAULT_DEBUG_DIR);
    String debugFileName = System.getProperty(DEBUG_FILE_PROPERTY,
                                              DEFAULT_DEBUG_FILE);
    
    File debugFile = new File(debugDir, debugFileName);
    boolean basic = false;
    if ((debugFile == null) ||
        (!debugFile.exists()) ||
        (!debugFile.isFile()) ||
        (debugFile.length() == 0)) {
      BasicConfigurator.configure();
    } else {
      try {
        PropertyConfigurator.configure(debugFile.getCanonicalPath());
      } catch (Exception exc) {
        BasicConfigurator.configure();
        basic = true;
      }
    }
    
    Category root = Category.getRoot();
    if (basic) {
      root.setPriority(org.apache.log4j.Priority.ERROR);
    }

    // Instanciate the MonologLoggerFactory
    factory = (LoggerFactory) new MonologLoggerFactory();
  }

  public static Logger getLogger(String topic) {
    if (factory == null) init();
    return factory.getLogger(topic);
  }
}
