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

package fr.dyade.aaa.util;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

/**
 * Centralizes the debug traces
 * application.
 */
public abstract class Trace {

  public static final String PROP_FILE_NAME = "a3trace.conf";
  public static final int NORMAL_MODE = 0;
  public static final int ENTER_MODE = 1;
  public static final int EXIT_MODE = 2;

  public static final int FATAL_LEVEL = 1;
  public static final int ERROR_LEVEL = 2;
  public static final int WARNING_LEVEL = 3;
  public static final int INFO_LEVEL = 4;
  public static final int DEBUG_LEVEL = 5;

  public static boolean objectTracePackage;
  public static boolean objectTraceInstance;
  public static boolean objectTraceParameters;
  public static int objectTraceParameterLevel;
  public static boolean classTraceParameters;
  public static boolean methodTraceLineNumber;
  public static boolean methodTraceType;
  public static String methodTraceEnterItem;
  public static String methodTraceExitItem;
  public static String methodTraceNormalItem;
  public static boolean traceObjectToStringActivation = false;

  private static boolean initialized = false;

  private static Vector ancesterInstance = new Vector();
  private static int currentTraceParameterLevel = 0;

  private static String traceFileName;
  private static File traceFile = null;
  private static FileOutputStream traceOutputStream = null;
  private static File directory;
  private static Properties properties;

  private static int indentLevel;
  private static int decalValue = 1;

  protected Trace () {
  }

  private static void initProperties() {
    String dirName = System.getProperty("A3TRACE_DIR", ".");
    directory = new File(dirName);

    properties = new Properties(System.getProperties());
    File propFile = new File(directory, System.getProperty ("A3TRACE_CONF", PROP_FILE_NAME));
    if (propFile.exists()) {
      InputStream propIn = null;
      try {
	propIn = new FileInputStream(propFile);
	properties.load(propIn);
	propIn.close();
      } catch (Exception exc) {
      } finally {
	if (propIn != null) {
	  try {
	    propIn.close();
	  } catch (Exception exc) {}
	}
      }
    }
  }

  protected abstract void readProperties (Properties properties);

  protected void init () throws Exception {
    if (! initialized) {
      initProperties ();
      objectTracePackage = new Boolean (properties.getProperty("Trace.objectTracePackage", "false")).booleanValue();
      objectTraceInstance = new Boolean (properties.getProperty("Trace.objectTraceInstance", "false")).booleanValue();
      objectTraceParameters = new Boolean (properties.getProperty("Trace.objectTraceParameters", "false")).booleanValue();
      objectTraceParameterLevel = new Integer (properties.getProperty("Trace.objectTraceParameterLevel", "0")).intValue();
      classTraceParameters = new Boolean (properties.getProperty("Trace.classTraceParameters", "false")).booleanValue();
      methodTraceLineNumber = new Boolean (properties.getProperty("Trace.methodTraceLineNumber", "false")).booleanValue();
      methodTraceType = new Boolean (properties.getProperty("Trace.methodTraceType", "false")).booleanValue();
      methodTraceEnterItem = properties.getProperty("Trace.methodTraceEnterItem", "=>");
      methodTraceExitItem = properties.getProperty("Trace.methodTraceExitItem", "<=");
      methodTraceNormalItem = properties.getProperty("Trace.methodTraceNormalItem", "==");
      traceFileName = properties.getProperty("Trace.traceFileName", null);
      if (traceFileName != null) {
        traceFile = new File (traceFileName);
        traceOutputStream = new FileOutputStream (traceFile);
      }
      initialized = true;
    }
    this.readProperties (properties);

  }

  private static boolean identicalAncesterInstance (Object object) {

    if (ancesterInstance.isEmpty ()) {
      return false;
    }
    for (int i = 0; i < ancesterInstance.size(); i++) {
      if (object == ancesterInstance.get(i))  {
        return true;
      }
    }
    return false;
  }

  private static String fieldToString (Field field, Object object) {
    String res = "";

    if ((Modifier.isFinal (field.getModifiers()) == false) &&
        (((object instanceof Class) == false) || (Modifier.isStatic (field.getModifiers())))) {
      Class type = field.getType();
      boolean accessibleField;

      res = res+field.getName()+"=";
      if ((accessibleField = field.isAccessible()) == false) {
        field.setAccessible (true);
      }
      try {
      Object fieldObject = field.get(object);

      if (identicalAncesterInstance (fieldObject) == false) {
        res = res+fieldObject;
      } else {
        res = res+"@"+Integer.toHexString(fieldObject.hashCode());
      }
      } catch (Exception e) {
          e.printStackTrace ();
          System.out.println ("Don't know how to access "+field.getName()+" value");
      }
      field.setAccessible (accessibleField);
    }
    return res;
  }

  public static String objectToString (Object object) {
    String res;

    if (object instanceof Class) {
      res = ((Class) object).getName()+"[class]";
    } else {
      res = object.getClass().getName();
      if (objectTraceInstance) {
        res = res+"@"+Integer.toHexString(object.hashCode());
      }
    }

    if (objectTracePackage == false) {
      if (res.lastIndexOf ('.') >= 0) {
        res = res.substring (res.lastIndexOf ('.') + 1);
      }
    }

    if (((classTraceParameters) && (object instanceof Class)) ||
        ((objectTraceParameters) && (! (object instanceof Class)))) {
      Vector allFields = new Vector ();
      Class objectClass;
      Field [] fp;

      if (object instanceof Class) {
        objectClass = (Class) object;
      } else {
        objectClass = object.getClass();
      }

      while (objectClass != null) {
        fp = objectClass.getDeclaredFields();
        for (int i = 0; i < fp.length; i++) {
          allFields.add (fp[i]);
        }
        objectClass = objectClass.getSuperclass();
      }
      fp = new Field [allFields.size()];
      allFields.copyInto (fp);

      ancesterInstance.addElement (object);
      if ((objectTraceParameterLevel == 0) || (currentTraceParameterLevel < objectTraceParameterLevel)) {
        currentTraceParameterLevel++;
        String indentString = "";
        indentLevel = indentLevel + decalValue;
        for (int i = 0; i < indentLevel; i++) {
          indentString = indentString + " ";
        }
        res = res+" (";
        for (int i = 0; i < fp.length; i++) {
          String inter;

          if ((inter = fieldToString (fp[i], object)).equals("") ==  false) {
            res = res+"\n"+indentString+inter;
          }
        }
        res = res+"\n"+indentString.substring (0, indentLevel - decalValue)+")";
        indentLevel = indentLevel - decalValue;
        currentTraceParameterLevel--;
      } else {
        res = res+"()";
      }
      ancesterInstance.remove (object);
    } else {
      res = res+"()";
    }
    return res;
  }

  public static String  callerName (String className) {
    String callerName = "";
    Exception e = new Exception ();
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    PrintWriter pw = new PrintWriter (buffer);
    e.printStackTrace (pw);
    pw.flush ();
    StringTokenizer st = new StringTokenizer (buffer.toString(), "\n");
    st.nextToken();

    boolean takeNextOne;
    takeNextOne = false;
    while (st.hasMoreElements()) {
      String callStackLine = st.nextToken ();
      StringTokenizer st1 = new StringTokenizer (callStackLine, " ");
      st1.nextToken();
      callStackLine = st1.nextToken();
      st1 = new StringTokenizer (callStackLine, "(");
      callerName = st1.nextToken();
      String currentClassName = callerName.substring (0, callerName.lastIndexOf('.'));
      callerName = callerName.substring(callerName.lastIndexOf ('.') + 1);
      if ((takeNextOne) && (callerName.equals("traceMessage") == false))
        break;
      // if ((className.equals (currentClassName))  && (callerName.equals ("traceMessage") == false)){
      takeNextOne = callerName.equals ("traceMessage");
    }
    String res = callerName;
    if (methodTraceLineNumber == true) {
      String lineNumber = st.nextToken();
      st = new StringTokenizer (lineNumber, ":");
      st.nextToken ();
      lineNumber = st.nextToken ();
      res = res+"["+lineNumber.substring (0, lineNumber.indexOf(')'))+"]";
    }
    return res;
  }

  public static String methodToString (Object object, Object [] parameters) {
    String methodName;

    if (object instanceof Class) {
      methodName = callerName (((Class) object).getName());
    } else {
      methodName = callerName (object.getClass().getName());
    }
    if (parameters != null) {
      String indentString = "";
      indentLevel += decalValue;
      for (int i = 0; i < indentLevel; i++) {
        indentString += " ";
      }
      methodName = methodName + " (";
      for (int i = 0; i < parameters.length; i++) {
        methodName += "\n" + indentString;
        if (methodTraceType) {
          if (objectTracePackage) {
            methodName += parameters[i].getClass().getName();
          } else {
            String typeOfParameter = parameters[i].getClass().getName();
            typeOfParameter = typeOfParameter.substring (typeOfParameter.lastIndexOf ('.') + 1);
            methodName += typeOfParameter;
          }
          methodName += " ";
        }
        methodName += objectToString(parameters[i]);
      }
      methodName = methodName + "\n" + indentString.substring (0, indentLevel-decalValue) + ")";
      indentLevel -= decalValue;
    } else {
      methodName += "()";
    }

    return methodName;
  }

  public static void  traceMessage (int level, int subject, Object object, Object [] parameters, String message) {
    traceMessage (level, subject, object, parameters, message, NORMAL_MODE);
  }

  public synchronized static void  traceMessage (int level, int subject, Object object, Object [] parameters, String message, int mode) {
    String objectName;
    String methodName;
    String res;

    indentLevel = 1;
    try {
    if (level > subject)
      return;

    traceObjectToStringActivation = true;
    if (mode == ENTER_MODE) {
      indentLevel += methodTraceEnterItem.length();
    } else if (mode == EXIT_MODE) {
      indentLevel += methodTraceExitItem.length();
    } else {
      indentLevel += methodTraceNormalItem.length();
    }
    objectName = objectToString (object);
    methodName = methodToString (object, parameters);
    traceObjectToStringActivation = false;
    res = objectName+"."+methodName;
    if (mode == ENTER_MODE) {
      res = methodTraceEnterItem + " " + res;
    } else if (mode == EXIT_MODE) {
      res = methodTraceExitItem + " " + res;
    } else {
      res = methodTraceNormalItem + " " + res;
    }
    if (message != null) {
      String starString = "";

      for (int i = 0; i < indentLevel - 1; i++)
        starString += "*";
      res = res + ":\n" + starString + " " + message + "\n";
    } else {
      res += "\n";
    }
    if (traceOutputStream != null) {
      traceOutputStream.write ((res+"\n").getBytes());
      traceOutputStream.flush ();
    } else {
      System.out.println (res);
      System.out.flush();
    }
    } catch (Exception e) {
    e.printStackTrace();
    }
  }
}
