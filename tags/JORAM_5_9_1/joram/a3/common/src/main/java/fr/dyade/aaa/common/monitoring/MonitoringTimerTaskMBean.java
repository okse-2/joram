/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2009 - 2010 ScalAgent Distributed Technologies
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

/**
 *
 */
public interface MonitoringTimerTaskMBean {
  /**
   * Returns the period value of this queue, -1 if not set.
   * 
   * @return the period value of this queue; -1 if not set.
   */
  long getPeriod();

//  /**
//   * Sets or unsets the period for this queue.
//   * 
//   * @param period
//   *          The period value to be set or -1 for unsetting previous value.
//   */
//  void setPeriod(long period);

  /**
   * Returns the comma separated list of all monitored attributes.
   * 
   * @return the comma separated list of all monitored attributes.
   */
  String[] getMonitoredAttributes();
  
  /**
   * Add the specified attributes to the list of monitored attributes.
   * If the Mbean is already monitored, the specified list of attributes
   * overrides the existing one.
   * 
   * @param MBeanName   the name of the MBean.
   * @param attributes  the comma separated list of attributes to monitor.
   */
  void addMonitoredAttributes(String MBeanName, String attributes);
  
  /**
   * Removes all the attributes of the specified MBean in the list of
   * monitored attributes.
   * 
   * @param MBeanName the name of the MBean.
   */
  void delMonitoredAttributes(String MBeanName);
  
  /**
   * Cancels this timer task.
   */
  void cancelTask();
}
