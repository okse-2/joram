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
public static final String RCS_VERSION="@(#)$Id: Debug.java,v 1.4 2001-05-04 14:54:49 tachkeni Exp $";

  /** directory holding the debug files */
  public static File directory = null;

  /** name of the debug property file */
  private static final String PROP_FILE_NAME = "a3debug.cfg";

  /** debug properties, publicly accessible */
  public static Properties properties = null;

  /** stream to the debug trace file */
  private static PrintWriter stream;

  /**
   * Initializes the properties variable.
   * This function may be used outside of an agent server to get the definition
   * of debug variables in the debug property file.
   */
  public static void initProperties() {
    // finds the debug directory
    String dirName = System.getProperty("A3DEBUG_DIR", ".");
    directory = new File(dirName);

    properties = new Properties(System.getProperties());
    // looks up for the debug property file
    File propFile = new File(directory, PROP_FILE_NAME);
    if (propFile.exists()) {
      InputStream propIn = null;
      try {
	propIn = new FileInputStream(propFile);
	properties.load(propIn);
	propIn.close();
      } catch (Exception exc) {
	// ignores exceptions
      } finally {
	if (propIn != null) {
	  try {
	    propIn.close();
	  } catch (Exception exc) {}
	}
      }
    }
  }

  /**
   * Initializes the package.
   * Creates and/or opens the debug trace file, sets the debug variables
   * from the debug property file.
   *
   * @param serverId	this server id
   */
  public static void init(short serverId) {
    initProperties();

    // creates the debug trace file
    File file = new File(directory, "server" + serverId + ".audit");
    try {
      stream = new PrintWriter(
	new BufferedWriter(new FileWriter(file.getPath(), true)));
    } catch (IOException exc) {
      System.err.println("cannot open audit file " + file.getPath());
      System.exit(1);
    }

    // sets Debug debug variables
    createAgent = new Boolean(
      properties.getProperty("Debug.createAgent", "false"))
      .booleanValue();
    garbageAgent = new Boolean(
      properties.getProperty("Debug.garbageAgent", "false"))
      .booleanValue();
    loadAgent = new Boolean(
      properties.getProperty("Debug.loadAgent", "false"))
      .booleanValue();
    saveAgent = new Boolean(
      properties.getProperty("Debug.saveAgent", "false"))
      .booleanValue();
    loadAgentObject = new Boolean(
      properties.getProperty("Debug.loadAgentObject", "false"))
      .booleanValue();
    saveAgentObject = new Boolean(
      properties.getProperty("Debug.saveAgentObject", "false"))
      .booleanValue();

    if (new Boolean(properties.getProperty("Debug.AgentLifeCycle", "false"))
	.booleanValue()) {
      createAgent = true;
      garbageAgent = true;
      saveAgent = true;
      loadAgentObject = true;
      saveAgentObject = true;
    }

    channelSend = new Boolean(
      properties.getProperty("Debug.channelSend", "false"))
      .booleanValue();
    engineLoop = new Boolean(
      properties.getProperty("Debug.engineLoop", "false"))
      .booleanValue();

    if (new Boolean(properties.getProperty("Debug.A3Engine", "false"))
	.booleanValue()) {
      channelSend = true;
      engineLoop = true;
    }

    if (new Boolean(properties.getProperty("Debug.A3Server", "false"))
	.booleanValue()) {
      A3Server = true;
    }

    network = new Boolean(
      properties.getProperty("Debug.network", "false"))
      .booleanValue();
    message = new Boolean(
      properties.getProperty("Debug.message", "false"))
      .booleanValue();

    if (new Boolean(properties.getProperty("Debug.Network.all", "false"))
	.booleanValue()) {
      network = true;
      message = true;
    }

    restoreServer = new Boolean(
      properties.getProperty("Debug.restoreServer", "false"))
      .booleanValue();
    agentError = new Boolean(
      properties.getProperty("Debug.agentError", "false"))
      .booleanValue();
    agentInit = new Boolean(
      properties.getProperty("Debug.agentInit", "false"))
      .booleanValue();

    dumpMatrixClock = new Boolean(
      properties.getProperty("Debug.dumpMatrixClock", "false"))
      .booleanValue();
    printThread = new Boolean(
      properties.getProperty("Debug.printThread", "false"))
      .booleanValue();

    drivers = new Boolean(
      properties.getProperty("Debug.drivers", "false"))
      .booleanValue();
    driversControl = new Boolean(
      properties.getProperty("Debug.drivers.control", "false"))
      .booleanValue();
    driversData = new Boolean(
      properties.getProperty("Debug.drivers.data", "false"))
      .booleanValue();
    if (new Boolean(properties.getProperty("Debug.drivers.all", "false"))
	.booleanValue()) {
      drivers = true;
      driversControl = true;
      driversData = true;
    }

    // sets dynamic debug variables for other packages
    final String dynvarMarker = "Debug.var.";
    int markerLength = dynvarMarker.length();
    for (Enumeration list = properties.propertyNames();
	 list.hasMoreElements();) {
      String key = (String) list.nextElement();
      if (key.regionMatches(0, dynvarMarker, 0, markerLength)) {
	// finds variable class and name
	int pindex = key.lastIndexOf('.');
	if (pindex <= markerLength) {
	  // bad formed property name, ignores
	  Debug.trace("bad formed property name: " + key, false);
	  continue;
	}
	String varClassName = key.substring(markerLength, pindex);
	String varName = key.substring(pindex + 1);
	// finds variable value
	String varValue = properties.getProperty(key);
	try {
	  // finds variable
	  Class varClass = Class.forName(varClassName);
	  Field var = varClass.getField(varName);
	  // sets variable according to its type
	  String varType = var.getType().getName();
	  if (varType.equals("boolean") ||
	      varType.equals("java.lang.Boolean")) {
	    var.set(null, new Boolean(varValue));
	  } else if (varType.equals("int") ||
	      varType.equals("java.lang.Integer")) {
	    var.set(null, new Integer(varValue));
	  } else if (varType.equals("java.lang.String")) {
	    var.set(null, varValue);
	  } else {
	    Debug.trace("error setting debug variable " +
			varClassName + "." + varName +
			": unexpected type " + varType, null);
	    continue;
	  }
	} catch (Exception exc) {
	  Debug.trace("error setting debug variable " +
		      varClassName + "." + varName, exc);
	  continue;
	}
      }
    }
  }

  static final boolean debug = true;

  static boolean printThread = false;

  static int debugLevel = 1;

  static boolean config = false;
  static boolean configParse = false;
  static boolean configRoute = false;

  static boolean A3Server = false;

  static boolean createAgent = false;
  static boolean garbageAgent = false;
  static boolean loadAgent = false;
  static boolean saveAgent = false;
  static boolean loadAgentObject = false;
  static boolean saveAgentObject = false;

  static boolean channelSend = false;
  static boolean engineLoop = false;

  /**
   * Traces catch'd exception, normally should always be true.
   */
  static boolean error = true;

  static boolean network = false;
  static boolean message = false;

  static boolean restoreServer = false;
  static boolean agentError = false;
  static boolean agentInit = false;

  static boolean dumpMatrixClock = false;

  public static boolean drivers = false;
  public static boolean driversControl = false;
  public static boolean driversData = false;

  public static boolean admin = false;

  static Object lock = new Object();

  public static void trace(String msg, boolean stack) {
    synchronized (lock) {
      stream.print("+-- " + new Date().getTime() + " ----- ");
      if (printThread)
	stream.print(Thread.currentThread());
      else
	stream.print("*************************");
      stream.println(" -----");
      stream.println("+ " + msg);
      if (stack) new Exception("Stack trace").printStackTrace(stream);
      stream.flush();
    }
  }

  public static void trace(String msg, Throwable exc) {
    synchronized (lock) {
      trace(msg + ": " + exc, false);
      exc.printStackTrace(stream);
      stream.flush();
    }
  }

  static void dump(byte[] buf, int size) {
    int idx = 0;

    synchronized (lock) {
      while (idx < size) {
	if (idx < 10)
	  stream.print("\n [    " + idx +"] | ");
	else if (idx < 100)
	  stream.print("\n [   " + idx +"] | ");
	else if (idx < 1000)
	  stream.print("\n [  " + idx +"] | ");
	else if (idx < 10000)
	  stream.print("\n [ " + idx +"] | ");
	else
	stream.print("\n [" + idx +"] | ");
	for (int i=0; i<16; i++) {
	  if ((idx+i) < size) {
	    if (buf[idx+i] < 0) {
	      stream.print(Integer.toHexString(256 + buf[idx+i]));
	    } else {
	      if (buf[idx+i] < 16)
		stream.print('0');
	      stream.print(Integer.toHexString(buf[idx+i]));
	    }
	    stream.print(' ');
	  } else
	    stream.print("   ");
	}
	stream.print(" | ");
	for (int i=0; i<16; i++) {
	  if ((idx+i) >= size)
	    stream.print(' ');
	  else if (!Character.isLetterOrDigit((char) buf[idx+i]))
	    stream.print('.');
	  else
	    stream.print((char) buf[idx+i]);
	}
	idx += 16;
      }
      stream.println();
      stream.flush();
    }
  }
}
