/*
 * Copyright (C) 2011 ScalAgent Distributed Technologies
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
package fr.dyade.aaa.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Configuration;
import fr.dyade.aaa.common.Debug;

public abstract class BaseTransaction implements Transaction {
  // Logging monitor
  protected static Logger logmon = Debug.getLogger(Transaction.class.getName());
  
  private Properties props = null;
  
  public BaseTransaction() {
    props = new Properties();
  }

  public void loadProperties(File dir) throws InvalidPropertiesFormatException, FileNotFoundException, IOException {
    // Gets default Transaction properties from disk
    File tpf = new File(dir, "TPF");
    if (tpf.exists()) {
      props.loadFromXML(new FileInputStream(tpf));
    }
  }
  
  public void saveProperties(File dir) throws IOException {
    // Saves default Transaction properties if needed
    File tpf = new File(dir, "TPF");
    if (! tpf.exists()) {
      FileOutputStream fos = null;
      try {
        fos = new FileOutputStream(tpf);
        props.storeToXML(fos, "Transaction properties");
      } catch (Exception exc) {
        logmon.log(BasicLevel.ERROR,
                   "Transaction, Cannot save Transaction properties", exc);
      } finally {
        fos.close();
      }
    }
  }
  
  /**
   * Searches for the property with the specified key in the specific Transaction
   * property list. If the key is not found in this property list, the Configuration
   * property list is then checked.
   * The method returns <code>null</code> if the property is not found.
   *
   * @param   key   the property key.
   * @return  the value corresponding to the specified key value.
   */
  public String getProperty(String key) {
    String value = props.getProperty(key);
    if (value == null) {
      value = Configuration.getProperty(key);
      if (value != null) props.setProperty(key, value);
    }
    return value;
  }
  
  /**
   * Searches for the property with the specified key in the specific Transaction
   * property list. If the key is not found in this property list, the Configuration
   * property list is then checked. The method returns the default value argument
   * if the property is not found.
   *
   * @param   key            the property key.
   * @param   defaultValue   a default value.
   *
   * @return  the value corresponding to the specified key value.
   */
  public String getProperty(String key, String defaultValue) {
    String value = getProperty(key);
    return (value == null) ? defaultValue : value;
  }

  /**
   * Determines the integer value of the property with the specified name.
   * 
   * @param key
   *          property name.
   * @return the Integer value of the property.
   */
  public Integer getInteger(String key) {
    try {
      return Integer.valueOf(getProperty(key));
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
  public Integer getInteger(String key, int value) {
    Integer result = getInteger(key);
    return (result == null) ? new Integer(value) : result;
  }
  
  /**
   * Returns <code>true</code> if and only if the corresponding property exists
   * and is equal to the string {@code "true"}.
   *
   * @param   name   the property name.
   * @return  the <code>boolean</code> value of the property.
   */
  public boolean getBoolean(String key) {
    String result = getProperty(key);
    if ((result != null) && result.equalsIgnoreCase("true"))
      return true;
    return false;
  }
}
