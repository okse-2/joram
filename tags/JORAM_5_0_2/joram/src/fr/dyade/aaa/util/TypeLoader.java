/*
 * Copyright (C) 2000 ScalAgent Distributed Technologies
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
 */
package fr.dyade.aaa.util;

import java.lang.reflect.*;

public class TypeLoader {

  /**
   * If the specified type is primitive,
   * the associated wrapper class is returned 
   * (e.g. java.lang.Long for a primitive long).
   *
   * @return the type or <code>NULL</code> if
   * it has not been found.
   */
  public static Class loadType(String typeName,
                               ClassLoader classLoader) {
    try {
      return Class.forName(typeName);
    } catch (ClassNotFoundException exc) {
      try {
        if (classLoader != null) {
          return classLoader.loadClass(typeName);
        }
      } catch (ClassNotFoundException exc2) {}
    }
    
    // This may be either:
    // - a primitive type
    // - an array type if no instance of this 
    //   array of this type has been created yet.
    // - an unknown type
    if (typeName.equals("int")) {
      return Integer.class;
    } else if (typeName.equals("long")) {
      return Long.class;
    } else if (typeName.equals("boolean")) {
      return Boolean.class;
    } else if (typeName.equals("short")) {
      return Short.class;
    } else if (typeName.equals("double")) {
      return Double.class;
    } else if (typeName.equals("float")) {
      return Float.class;
    } else if (typeName.length() > 4 &&
               typeName.charAt(0) == '[' &&
               typeName.charAt(1) == 'L' &&
               typeName.charAt(typeName.length() - 1) == ';') {
      String eltClassName = typeName.substring(
        2, typeName.length() - 1);
      Class eltClass = loadType(eltClassName, classLoader);
      if (eltClass == null) return null;
      // Have to create an instance to get the type (class)
      Object arrayInstance = Array.newInstance(eltClass, 0);
      return arrayInstance.getClass();
    } else return null;
  }
}
