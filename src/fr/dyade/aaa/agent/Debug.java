/*
 * Copyright (C) 2001 - 2004 ScalAgent Distibuted Technologies
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
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
 */
package fr.dyade.aaa.agent;

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
public final class Debug extends fr.dyade.aaa.common.Debug {
  public static final String A3Debug = "fr.dyade.aaa.agent";
  public static final String A3Agent = A3Debug + ".Agent";
  public static final String A3Engine = A3Debug + ".Engine";
  public static final String A3Network = A3Debug + ".Network";
  public static final String A3Service = A3Debug + ".Service";
  public static final String A3Proxy = A3Agent + ".ProxyAgent";
  public static final String JGroups = A3Debug + ".JGroups";

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
}
