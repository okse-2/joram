/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2010 ScalAgent Distributed Technologies
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
package fr.dyade.aaa.common.monitoring;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import javax.management.MBeanAttributeInfo;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Debug;
import fr.dyade.aaa.common.Strings;
import fr.dyade.aaa.util.management.MXWrapper;

/**
 * The <code>DumpAttributes</code> class allows to watch all JMX attributes of a
 * specified domain and store the corresponding values in a file.
 */
public class DumpAttributes {
  public static Logger logger = Debug.getLogger(DumpAttributes.class.getName());

  /**
   * Records information about the specified attribute.
   * 
   * @param strbuf A StringBuffer to write into.
   * @param mbean  The name of the related mbean.
   * @param att    The name of the related attribute.
   * @param value  The value of the related attribute.
   */
  static void addRecord(StringBuffer strbuf,
                        ObjectName mbean, String att, Object value) {
    strbuf.append(mbean).append(':').append(att).append('=');
    Strings.toString(strbuf, value);
    strbuf.append('\n');
  }

  public static void dumpAttributes(String name, String path) {
    FileWriter writer = null;
    try {
      writer = new FileWriter(path, true);
    } catch (IOException exc) {
      logger.log(BasicLevel.ERROR,
                 "FileMonitoringTimerTask.<init>, cannot open file \"" + path + "\"", exc);
    }
    StringBuffer strbuf = new StringBuffer();

    Set mBeans = null;
    try {
      mBeans = MXWrapper.queryNames(new ObjectName(name));
    } catch (MalformedObjectNameException exc) {
      logger.log(BasicLevel.ERROR,
                 "DumpAttributes.dumpAttributes, bad name: " + name, exc);
    }

    if (mBeans != null) {
      for (Iterator iterator = mBeans.iterator(); iterator.hasNext();) {
        ObjectName mBean = (ObjectName) iterator.next();

        // Get all mbean's attributes
        try {
          MBeanAttributeInfo[] attributes = MXWrapper.getAttributes(mBean);
          if (attributes != null) {
            for (int i = 0; i < attributes.length; i++) {
              String attname = attributes[i].getName();
              try {
                addRecord(strbuf, mBean, attname, MXWrapper.getAttribute(mBean, attname));
              } catch (Exception exc) {
                logger.log(BasicLevel.ERROR,
                           "DumpAttributes.dumpAttributes, bad attribute : " + mBean + ":" + attname, exc);
              }
            }
          }
          writer.write(strbuf.toString());
          strbuf.setLength(0);
        } catch (Exception exc) {
          logger.log(BasicLevel.ERROR, "DumpAttributes.dumpAttributes", exc);
        }
      }
    }
  }
}
