/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2009 - 2011 ScalAgent Distributed Technologies
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
package org.objectweb.joram.mom.dest;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.objectweb.joram.shared.messages.Message;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Debug;
import fr.dyade.aaa.util.management.MXWrapper;

/**
 *
 */
public class MonitoringHelper {
  static Logger logger = Debug.getLogger(MonitoringHelper.class.getName());
  
  public static void getJMXValues(Message message, Vector elements) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "MonitoringHelper.getJMXValues() -> " + elements.size());
    
    for (int i=0; i<elements.size(); i++) {
      MonitoringElement element = (MonitoringElement) elements.elementAt(i);

      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "MonitoringHelper.getJMXValues() -> " + element.mbean);
      
      try {
        Set mBeans = MXWrapper.queryNames(element.mbean);
        if (mBeans != null) {
          for (Iterator iterator = mBeans.iterator(); iterator.hasNext();) {
            String mBean = (String) iterator.next();
            
            for (int j=0; j<element.attributes.length; j++) {
              try {
                if (element.attributes[j].equals("*")) {
                  List attributes = MXWrapper.getAttributeNames(mBean);
                  if (attributes != null) {
                    for (int k = 0; k < attributes.size(); k++) {
                      setMessageProperty(message, mBean, (String) attributes.get(k));
                    }
                  }
                } else {
                  setMessageProperty(message, mBean, element.attributes[j]);
                }
              } catch (Exception exc) {
                logger.log(BasicLevel.ERROR, " getAttributes  on " + mBean + " error.", exc);
              }
            }
          }
        }
      } catch (Exception exc) {
        logger.log(BasicLevel.ERROR, "Invalid MBean name : " + element.mbean, exc);
      }
    }
  }
  
  private static void setMessageProperty(Message message, String mbeanName, String attrName) throws Exception {
    Object value = MXWrapper.getAttribute(mbeanName, attrName);
    if (value != null)
      message.setProperty(mbeanName + ":" + attrName, value);
  }
  
  /**
   * Returns the comma separated list of all monitored attributes.
   * 
   * @param elements the various elements to monitor.
   * @return the comma separated list of all monitored attributes.
   */
  public static String[] getMonitoredAttributes(Vector elements) {
    String[] ret = new String[elements.size()];
    
    for (int i=0; i<ret.length; i++) {
      StringBuffer strbuf = new StringBuffer();
      MonitoringElement element = (MonitoringElement) elements.elementAt(i);
      strbuf.append(element.mbean).append('=').append(element.attributes[0]);
      for (int j=1; j<element.attributes.length; j++)
        strbuf.append(',').append(element.attributes[j]);
      ret[i] = strbuf.toString();
      strbuf.setLength(0);
    }

    return ret;
  }
  
  /**
   * Add the specified attributes to the list of monitored attributes.
   * If the Mbean is already monitored, the specified list of attributes
   * overrides the existing one.
   * 
   * @param elements    the various elements to monitor.
   * @param MBeanName   the name of the MBean.
   * @param attributes  the comma separated list of attributes to monitor.
   */
  public static void addMonitoredAttributes(Vector elements, String MBeanName, String attributes) {
    elements.add(new MonitoringElement(MBeanName, attributes));
  }
  
  /**
   * Removes all the attributes of the specified MBean in the list of
   * monitored attributes.
   * 
   * @param elements the various elements to monitor.
   * @param mbean     the name of the MBean.
   */
  public static void delMonitoredAttributes(Vector elements, String mbean) {
    for (int i=0; i<elements.size();) {
      MonitoringElement element = (MonitoringElement) elements.elementAt(i);
      if (element.mbean.equals(mbean)) {
        elements.removeElementAt(i);
      } else {
        i++;
      }
    }
  }
}
