/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
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
package org.objectweb.joram.mom.dest;

import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.management.MBeanAttributeInfo;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

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
      logger.log(BasicLevel.DEBUG, "MonitoringHelper.getJMXValues()");
    
    logger.log(BasicLevel.FATAL, "MonitoringHelper.getJMXValues() -> " + elements.size());
    for (int i=0; i<elements.size(); i++) {
      MonitoringElement element = (MonitoringElement) elements.elementAt(i);

      logger.log(BasicLevel.FATAL, "MonitoringHelper.getJMXValues() -> " + element.mbean);
      try {
        Set mBeans = MXWrapper.queryNames(new ObjectName(element.mbean));
        if (mBeans != null) {
          for (Iterator iterator = mBeans.iterator(); iterator.hasNext();) {
            ObjectName mBean = (ObjectName) iterator.next();
            
            for (int j=0; j<element.attributes.length; j++) {
              try {
                if (element.attributes[j].equals("*")) {
                  MBeanAttributeInfo[] attributes = MXWrapper.getAttributes(mBean);
                  if (attributes != null) {
                    for (int k=0; k<attributes.length; k++) {
                      setMessageProperty(message, mBean, attributes[k].getName());
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
      } catch (MalformedObjectNameException exc) {
        logger.log(BasicLevel.ERROR, "Invalid MBean name : " + element.mbean, exc);
      }
    }
  }
  
  private static void setMessageProperty(Message message,
                                         ObjectName mbeanName,
                                         String attrName) throws Exception {
    Object value = MXWrapper.getAttribute(mbeanName, attrName);
    if (value != null)
      message.setProperty(mbeanName + ":" + attrName, value);
  }
}
