/*
 * Copyright (C) 2009 ScalAgent Distributed Technologies
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
 * Initial developer(s): ScalAgent Distributed Technologies
 * Contributor(s): 
 */
package fr.dyade.aaa.common;

import java.util.Properties;

/**
 * This class holds a static list of properties accessible from anywhere. It is
 * used to avoid multiplying system-wide properties.
 */
public class Configuration {

  private static Properties properties = new Properties(System.getProperties());

  /**
   * Searches for the property with the specified key in the property list.
   * 
   * @param key
   *          the hashtable key.
   * @return the value with the specified key value.
   */
  public static String getProperty(String key) {
    return properties.getProperty(key);
  }

  /**
   * Searches for the property with the specified key in the property list.
   * 
   * @param key
   *          the hashtable key.
   * @param value
   *          a default value.
   * @return the value with the specified key value.
   */
  public static String getProperty(String key, String value) {
    return properties.getProperty(key, value);
  }

  /**
   * Determines the integer value of the property with the specified name.
   * 
   * @param key
   *          property name.
   * @return the Integer value of the property.
   */
  public static Integer getInteger(String key) {
    try {
      return Integer.valueOf(properties.getProperty(key));
    } catch (Exception exc) {
      return null;
    }
  }

  /**
   * Determines the integer value of the property with the specified name.
   * 
   * @param key
   *          property name.
   * @param value
   *          a default value.
   * @return the Integer value of the property.
   */
  public static Integer getInteger(String key, int value) {
    Integer result = getInteger(key);
    return (result == null) ? new Integer(value) : result;
  }

  /**
   * Determines the integer value of the property with the specified name.
   * 
   * @param key
   *          property name.
   * @return the Integer value of the property.
   */
  public static Long getLong(String key) {
    try {
      return Long.valueOf(properties.getProperty(key));
    } catch (Exception exc) {
      return null;
    }
  }

  /**
   * Determines the long value of the property with the specified name.
   * 
   * @param key
   *          property name.
   * @param value
   *          a default value.
   * @return the Integer value of the property.
   */
  public static Long getLong(String key, long value) {
    Long result = getLong(key);
    return (result == null) ? new Long(value) : result;
  }

  /**
   * Determines the boolean value of the property with the specified name.
   * 
   * @param key
   *          property name.
   * @return the boolean value of the property.
   */
  public static boolean getBoolean(String key) {
    return Boolean.valueOf(properties.getProperty(key)).booleanValue();
  }

  /**
   * Adds a new property to the configuration.
   * 
   * @param name
   *          property name.
   * @param value
   *          property value.
   */
  public static void putProperty(String name, String value) {
    properties.put(name, value);
  }
}
