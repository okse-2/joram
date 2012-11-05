/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2007 ScalAgent Distributed Technologies
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
 * Initial developer(s): Nicolas Tachker (ScalAgent)
 * Contributor(s):
 */
package jms.main;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;

import jms.framework.JMSTestCase;


/**
 */
public class Tests {

  public static Class[] getClasses(String pckgname)
      throws ClassNotFoundException {
    ArrayList classes = new ArrayList();
    // Get a File object for the package
    File directory = null;
    try {
      ClassLoader cld = Thread.currentThread().getContextClassLoader();
      if (cld == null) {
        throw new ClassNotFoundException("Can't get class loader.");
      }
      String path = pckgname.replace('.', '/');
      URL resource = cld.getResource(path);
      if (resource == null) {
        throw new ClassNotFoundException("No resource for " + path);
      }
      directory = new File(resource.getFile());
    } catch (NullPointerException x) {
      throw new ClassNotFoundException(pckgname + " (" + directory
          + ") does not appear to be a valid package");
    }
    if (directory.exists()) {
      findClass(directory, classes, pckgname);
    }
    Class[] classesA = new Class[classes.size()];
    classes.toArray(classesA);
    return classesA;
  }

  public static void findClass(File directory, ArrayList classes,
      String pckgname) {
    // Get the list of the files contained in the package
    String[] files = directory.list();
    for (int i = 0; i < files.length; i++) {
      // we are only interested in .class files
      if (files[i].endsWith(".class")) {
        // removes the .class extension
        try {
          classes.add(Class.forName(pckgname + '.'
              + files[i].substring(0, files[i].length() - 6)));
        } catch (ClassNotFoundException e) {
          e.printStackTrace();
        }
      } else if (new File(directory, files[i]).isDirectory()) {
        findClass(new File(directory, files[i]), classes, pckgname + '.'
            + files[i]);
      }
    }
  }

  /**
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {

    Class[] classes = getClasses("jms.conform");
    Method method = null;
    for (int i = 0; i < classes.length; i++) {
      try {
        Object obj = classes[i].newInstance();
        if (obj instanceof JMSTestCase) {
          method = classes[i].getMethod("main", new Class[]{String[].class});
          if (method != null)
            method.invoke(null, new Object[]{args});
        }
      } catch (InstantiationException e) {
      }
    }

  }

}
