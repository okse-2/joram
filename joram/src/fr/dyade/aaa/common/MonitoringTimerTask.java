/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 ScalAgent Distributed Technologies
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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import javax.management.MBeanAttributeInfo;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.util.management.MXWrapper;

/**
 * The <code>MonitoringTopicImpl</code> class implements the monitoring
 * behavior, regularly delivering monitoring messages to subscribers.
 */
public class MonitoringTimerTask extends java.util.TimerTask {
  /** Time between two monitoring events */
  protected long period;

  Properties attlist = null;

  FileWriter writer;
  
  public static Logger logger = Debug.getLogger(MonitoringTimerTask.class.getName());
  
  public MonitoringTimerTask(java.util.Timer timer, long period, String path, Properties attlist) {
    this.period = period;
    this.attlist = attlist;
    
    try {
      writer = new FileWriter(path, true);
    } catch (IOException exc) {
      logger.log(BasicLevel.ERROR,
                 "MonitoringTimerTask.<init>, cannot open file \"" + path + "\"", exc);
    }
    
    timer.scheduleAtFixedRate(this, period, period);
  }
  
// Joram#0:type=User,*=NbMsgsDeliveredSinceCreation,NbMsgsSentToDMQSinceCreation,PendingMessageCount
// Joram#0:type=Destination,*=NbMsgsDeliverSinceCreation,NbMsgsReceiveSinceCreation,NbMsgsSentToDMQSinceCreation

  /**
   * When the task is waken up, collect the monitoring information required and
   * saves it.
   * @see fr.dyade.aaa.common.TimerTask#run()
   */
  public void run() {
    StringBuffer strbuf = new StringBuffer();
    strbuf.append(System.currentTimeMillis()).append(';');
    
    Enumeration mbeans = attlist.keys();
    while (mbeans.hasMoreElements()) {
      String name = (String) mbeans.nextElement();

      Set mBeans = null;
      try {
        mBeans = MXWrapper.queryNames(new ObjectName(name));
      } catch (MalformedObjectNameException exc) {
        logger.log(BasicLevel.ERROR, "MonitoringTimerTask.run, bad name: " + name, exc);
      }

      if (mBeans != null) {
        for (Iterator iterator = mBeans.iterator(); iterator.hasNext();) {
          ObjectName mBean = (ObjectName) iterator.next();
          StringTokenizer st = new StringTokenizer((String) attlist.get(name), ",");
          while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (token.equals("*")) {
              // Get all mbean's attributes
              try {
                MBeanAttributeInfo[] attributes = MXWrapper.getAttributes(mBean);
                if (attributes != null) {
                  for (int i = 0; i < attributes.length; i++) {
                    String attname = attributes[i].getName();
                    strbuf.append(mBean).append(':').append(attname).append(';');
                    try {
                      strbuf.append(MXWrapper.getAttribute(mBean, attname));
                    } catch (Exception exc) {
                      logger.log(BasicLevel.ERROR,
                                 "MonitoringTimerTask.run, bad attribute : " + mBean + ":" + attname, exc);
                    }
                    strbuf.append(';');
                  }
                }
              } catch (Exception exc) {
                logger.log(BasicLevel.ERROR, "MonitoringTimerTask.run", exc);
              }
            } else {
              // Get the specific attribute
              String attname = token.trim();
              strbuf.append(mBean).append(':').append(attname).append(';');
              try {
                strbuf.append(MXWrapper.getAttribute(mBean, attname));
              } catch (Exception exc) {
                logger.log(BasicLevel.ERROR,
                           "MonitoringTimerTask.run, bad attribute : " + mBean + ":" + attname, exc);
              }
              strbuf.append(';');
            }
          }
        }
      }
    }
    strbuf.append('\n');
    
    try {
      writer.write(strbuf.toString());
      writer.flush();
    } catch (IOException exc) {
    }
  }
}
