/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 - 2012 ScalAgent Distributed Technologies
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

import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Timer;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Debug;
import fr.dyade.aaa.util.management.MXWrapper;

/**
 * The <code>MonitoringTimerTask</code> class allows to periodically watch JMX attributes
 * and store the corresponding values to various support.
 */
public abstract class MonitoringTimerTask extends java.util.TimerTask implements MonitoringTimerTaskMBean {
  /** Time between two monitoring events */
  protected long period;
  
  /**
   * Returns the period value of this task, -1 if not set.
   * 
   * @return the period value of this task; -1 if not set.
   */
  public long getPeriod() {
    return period;
  }

  Properties attlist = null;
  
  /**
   * Name use to register/unregister MBean
   */
  public String MBean_name = null;
  
  public static Logger logger = Debug.getLogger(MonitoringTimerTask.class.getName());
  
  /**
   * Initializes the <code>MonitoringTimerTask</code> component.
   * 
   * @param period  Period value of the resulting task
   * @param attlist List of JMX attributes to periodically watch.
   */
  public MonitoringTimerTask(long period, Properties attlist) {
    this.period = period;
    this.attlist = attlist;
    
  }
  
  /**
   * Initializes the <code>MonitoringTimerTask</code> component.
   * 
   * @param period  Period value of the resulting task
   * @param attlist List of JMX attributes to periodically watch.
   */
  public MonitoringTimerTask() {
  }
  
  public abstract void init(Timer timer, long period, Properties attlist, Properties taskProps);
  
  /**
   * Starts the resulting task.
   * 
   * @param timer Timer to use to schedule the resulting task.
   */
  protected final void start(Timer timer) {
    timer.scheduleAtFixedRate(this, 0, period);
  }
  
  /**
   * Initialize the record for the current collect time.
   */
  protected abstract void initializeRecords();
  
  /**
   * Records information about the specified attribute.
   * 
   * @param mbean The name of the related mbean.
   * @param att   The name of the related attribute.
   * @param value The value of the related attribute.
   */
  protected abstract void addRecord(String mbean, String att, Object value);
  
  /**
   * Finalize the record for the current time.
   */
  protected abstract void finalizeRecords();
  
// Joram#0:type=User,*=NbMsgsDeliveredSinceCreation,NbMsgsSentToDMQSinceCreation,PendingMessageCount
// Joram#0:type=Destination,*=NbMsgsDeliverSinceCreation,NbMsgsReceiveSinceCreation,NbMsgsSentToDMQSinceCreation

  /**
   * When the task is waken up, collect the monitoring information required and saves it.
   *
   * @see fr.dyade.aaa.common.TimerTask#run()
   */
  public void run() {
    initializeRecords();
    
    Enumeration mbeans = attlist.keys();
    while (mbeans.hasMoreElements()) {
      String name = (String) mbeans.nextElement();

      Set<String> mBeans = null;
      try {
        mBeans = MXWrapper.queryNames(name);
      } catch (Exception exc) {
        logger.log(BasicLevel.ERROR, "MonitoringTimerTask.run, bad name: " + name, exc);
      }

      if (mBeans != null) {
        for (Iterator<String> iterator = mBeans.iterator(); iterator.hasNext();) {
          String mBean = (String) iterator.next();
          StringTokenizer st = new StringTokenizer((String) attlist.get(name), ",");
          while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (token.equals("*")) {
              // Get all mbean's attributes
              try {
                List<String> attributes = MXWrapper.getAttributeNames(mBean);
                if (attributes != null) {
                  for (int i = 0; i < attributes.size(); i++) {
                    String attname = (String) attributes.get(i);
                    try {
                      addRecord(mBean, attname, MXWrapper.getAttribute(mBean, attname));
                    } catch (Exception exc) {
                      logger.log(BasicLevel.ERROR,
                                 "MonitoringTimerTask.run, bad attribute : " + mBean + ":" + attname, exc);
                    }
                  }
                }
              } catch (Exception exc) {
                logger.log(BasicLevel.ERROR, "MonitoringTimerTask.run", exc);
              }
            } else {
              // Get the specific attribute
              String attname = token.trim();
              try {
                addRecord(mBean, attname, MXWrapper.getAttribute(mBean, attname));
              } catch (Exception exc) {
                logger.log(BasicLevel.ERROR,
                           "MonitoringTimerTask.run, bad attribute : " + mBean + ":" + attname, exc);
              }
            }
          }
        }
      }
    }
    
    finalizeRecords();
  }

  /**
   * Returns the comma separated list of all monitored attributes.
   * 
   * @return the comma separated list of all monitored attributes.
   */
  public String[] getMonitoredAttributes() {
    int i = 0;
    String[] ret = new String[attlist.size()];
    for (Enumeration e = attlist.keys(); e.hasMoreElements();) {
      String mbean = (String) e.nextElement();
      ret[i++] = mbean + '=' + attlist.getProperty(mbean);
    }
    return ret;
  }
  
  /**
   * Add the specified attributes to the list of monitored attributes.
   * If the Mbean is already monitored, the specified list of attributes
   * overrides the existing one.
   * 
   * @param MBeanName   the name of the MBean.
   * @param attributes  the comma separated list of attributes to monitor.
   */
  public void addMonitoredAttributes(String MBeanName, String attributes) {
    attlist.put(MBeanName, attributes);
  }
  
  /**
   * Removes all the attributes of the specified MBean in the list of
   * monitored attributes.
   * 
   * @param MBeanName the name of the MBean.
   */
  public void delMonitoredAttributes(String MBeanName) {
    attlist.remove(MBeanName);
  }
  
  /**
   * Cancels this timer task.
   */
  public void cancelTask() {
    cancel();
  }

}
