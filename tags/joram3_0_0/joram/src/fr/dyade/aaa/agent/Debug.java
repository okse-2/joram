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
import java.util.*;
import java.lang.reflect.*;

import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Category;
import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;

import org.objectweb.monolog.wrapper.log4j.MonologMonitorFactory;

import org.objectweb.monolog.api.BasicLevel;
import org.objectweb.monolog.api.Monitor;
import org.objectweb.monolog.api.MonitorFactory;

/**
 * This class controls the debug traces printed to the audit file.
 * <p>
 * Debug traces are controled in the source code by package specific variables.
 * Those variables may be dynamically set from a special property file,
 * or from the environment.
 * <p>
 * To set debug variable <code>myvar</code> in class <code>myclass</code>,
 * the variable must be declared <code>public</code> and <code>static</code>,
 * and the following property should be defined:
 * <code>
 * <br> Debug.var.myclass.myvar=true
 * </code>.
 * <br> However this works only when the class garbage collection is disabled.
 * <p>
 * The <code>Debug</code> debug variables themselves are special, as they
 * are statically set in the <code>init</code> function. The debug variables
 * of the other packages must be dynamically set to ensure this is done after
 * the debug property file has been read.
 * <p>
 * Currently only boolean variables may be dynamically set this way.
 */
public final class Debug {
  /** RCS version number of this file: $Revision: 1.8 $ */
  public static final String RCS_VERSION="@(#)$Id: Debug.java,v 1.8 2002-03-06 16:50:00 joram Exp $";

  /** directory holding the debug files */
  public static File directory = null;

  static MonitorFactory factory = null;

  /** Property name for A3 debug configuration directory */
  public final static String DEBUG_DIR_PROPERTY = "fr.dyade.aaa.agent.A3DEBUG_DIR";
  /** Default directory for A3 debug configuration file */
  public final static String DEFAULT_DEBUG_DIR = ".";
  /** Property name for A3 debug configuration filename */
  public final static String DEBUG_FILE_PROPERTY = "fr.dyade.aaa.agent.A3DEBUG_FILE";
  /** Default filename for A3 debug configuration */
  public final static String DEFAULT_DEBUG_FILE = "a3debug.cfg";
  /** Property name for A3 debug produced files */ 
  public final static String AUDIT_DIR_PROPERTY = "fr.dyade.aaa.agent.A3AUDIT_DIR";
  /** Default directory for A3 debug produced files */
  public final static String DEFAULT_AUDIT_DIR = ".";

  /**
   * Initializes the package.
   * Creates and/or opens the debug trace file, sets the debug variables
   * from the debug property file.
   *
   * @param serverId	this server id
   */
  public static void init(short serverId) {
    boolean basic = false;

    String debugDir = System.getProperty(DEBUG_DIR_PROPERTY,
                                         DEFAULT_DEBUG_DIR);
    String debugFileName = System.getProperty(DEBUG_FILE_PROPERTY,
                                              DEFAULT_DEBUG_FILE);

    File debugFile = new File(debugDir, debugFileName);
    if ((debugFile == null) ||
        (!debugFile.exists()) ||
        (!debugFile.isFile()) ||
        (debugFile.length() == 0)) {
      BasicConfigurator.configure();
      basic = true;
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
    } else if (serverId >= 0) {
      try {
        String auditDir = System.getProperty(AUDIT_DIR_PROPERTY,
                                             DEFAULT_AUDIT_DIR);
        // Try to create local appender if defined...
        FileAppender local = (FileAppender) root.getAppender("local");
        File auditFile = new File(auditDir, "server#" + serverId + ".audit");
        local.setFile(auditFile.getCanonicalPath());
      } catch (Exception exc) { }
    }

    // Instanciate the MonologMonitorFactory
    factory = (MonitorFactory) new MonologMonitorFactory();
  }

    // sets dynamic debug variables for other packages
//     final String dynvarMarker = "Debug.var.";
//     int markerLength = dynvarMarker.length();
//     for (Enumeration list = properties.propertyNames();
// 	 list.hasMoreElements();) {
//       String key = (String) list.nextElement();
//       if (key.regionMatches(0, dynvarMarker, 0, markerLength)) {
// 	// finds variable class and name
// 	int pindex = key.lastIndexOf('.');
// 	if (pindex <= markerLength) {
// 	  // bad formed property name, ignores
// 	  logmon.log(BasicLevel.ERROR,
//                      "AgentServer#" + AgentServer.getServerId() +
//                      ".Debug, bad formed property name: " + key);
// 	  continue;
// 	}
// 	String varClassName = key.substring(markerLength, pindex);
// 	String varName = key.substring(pindex + 1);
// 	// finds variable value
// 	String varValue = properties.getProperty(key);
// 	try {
// 	  // finds variable
// 	  Class varClass = Class.forName(varClassName);
// 	  Field var = varClass.getField(varName);
// 	  // sets variable according to its type
// 	  String varType = var.getType().getName();
// 	  if (varType.equals("boolean") ||
// 	      varType.equals("java.lang.Boolean")) {
// 	    var.set(null, new Boolean(varValue));
// 	  } else if (varType.equals("int") ||
// 	      varType.equals("java.lang.Integer")) {
// 	    var.set(null, new Integer(varValue));
// 	  } else if (varType.equals("java.lang.String")) {
// 	    var.set(null, varValue);
// 	  } else {
//             logmon.log(BasicLevel.ERROR,
//                        "AgentServer#" + AgentServer.getServerId() +
//                        ".Debug, error setting debug variable " +
//                        varClassName + "." + varName +
//                        ": unexpected type " + varType);
// 	    continue;
// 	  }
// 	} catch (Exception exc) {
// 	  logmon.log(BasicLevel.ERROR,
//                      "AgentServer#" + AgentServer.getServerId() +
//                      ".Debug, error setting debug variable " +
//                      varClassName + "." + varName, exc);
// 	  continue;
// 	}
//       }
//     }

  static final boolean debug = true;

  public static final String A3Debug = "fr.dyade.aaa.agent";
  public static final String A3Agent = A3Debug + ".Agent";
  public static final String A3Engine = A3Debug + ".Engine";
  public static final String A3Network = A3Debug + ".Network";
  public static final String A3Service = A3Debug + ".Service";
  public static final String A3Daemon = A3Debug + ".Daemon";
  public static final String A3Proxy = A3Agent + ".ProxyAgent";

  public static Monitor getMonitor(String topic) {
    if (factory == null)
      init((short) -1);

    return factory.getMonitor(topic);
  }

//   static void dump(byte[] buf, int size) {
//     int idx = 0;

//     synchronized (lock) {
//       while (idx < size) {
// 	if (idx < 10)
// 	  stream.print("\n [    " + idx +"] | ");
// 	else if (idx < 100)
// 	  stream.print("\n [   " + idx +"] | ");
// 	else if (idx < 1000)
// 	  stream.print("\n [  " + idx +"] | ");
// 	else if (idx < 10000)
// 	  stream.print("\n [ " + idx +"] | ");
// 	else
// 	stream.print("\n [" + idx +"] | ");
// 	for (int i=0; i<16; i++) {
// 	  if ((idx+i) < size) {
// 	    if (buf[idx+i] < 0) {
// 	      stream.print(Integer.toHexString(256 + buf[idx+i]));
// 	    } else {
// 	      if (buf[idx+i] < 16)
// 		stream.print('0');
// 	      stream.print(Integer.toHexString(buf[idx+i]));
// 	    }
// 	    stream.print(' ');
// 	  } else
// 	    stream.print("   ");
// 	}
// 	stream.print(" | ");
// 	for (int i=0; i<16; i++) {
// 	  if ((idx+i) >= size)
// 	    stream.print(' ');
// 	  else if (!Character.isLetterOrDigit((char) buf[idx+i]))
// 	    stream.print('.');
// 	  else
// 	    stream.print((char) buf[idx+i]);
// 	}
// 	idx += 16;
//       }
//       stream.println();
//       stream.flush();
//     }
//   }
}
