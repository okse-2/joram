/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2010 - 2011 ScalAgent Distributed Technologies
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

import java.util.Properties;
import java.util.Timer;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

/**
 * The <code>FileMonitoringTimerTask</code> class allows to periodically watch JMX attributes
 * and write the corresponding values in the logging mechanism.
 */
public class LogMonitoringTimerTask extends MonitoringTimerTask {
  /**
   *  Name of property allowing to fix the scanning period for the log monitoring
   * task in the server.
   * <p>
   *  This property can be fixed either from <code>java</code> launching command,
   * or in <code>a3servers.xml</code> configuration file.
   * 
   * @see fr.dyade.aaa.common.monitoring.LogMonitoringTimerTask
   */
  public final static String MONITORING_CONFIG_PERIOD_PROPERTY = "LOG_MONITORING_CONFIG_PERIOD";
  /**
   *  Default value for the scanning period for the  log monitoring task in the
   * server, value is <code>60000L</code> (60 seconds).
   * 
   * @see fr.dyade.aaa.common.monitoring.MonitoringTimerTask
   */
  public final static long DEFAULT_MONITORING_CONFIG_PERIOD = 60000L;

  /**
   *  Name of property allowing to fix the pathname of a configuration file for a
   * log monitoring task in the server.
   * <p>
   *  This property can be fixed either from <code>java</code> launching command,
   * or in <code>a3servers.xml</code> configuration file.
   * 
   * @see fr.dyade.aaa.common.monitoring.LogMonitoringTimerTask
   */
  public final static String MONITORING_CONFIG_PATH_PROPERTY = "LOG_MONITORING_CONFIG_PATH";
  /**
   *  Default value for the pathname of a configuration file for a log monitoring
   * task in the server, value is <code>logMonitoring.props</code>.
   * <p>
   *  If the file does not exist the timer task is not launched.
   * 
   * @see fr.dyade.aaa.common.monitoring.LogMonitoringTimerTask
   */
  public final static String DEFAULT_MONITORING_CONFIG_PATH = "logMonitoring.props";

  /**
   *  Name of property allowing to fix the logger name of the results for the
   * log monitoring task in the server.
   * <p>
   *  This property can be fixed either from <code>java</code> launching command,
   * or in <code>a3servers.xml</code> configuration file.
   * 
   * @see fr.dyade.aaa.common.monitoring.LogMonitoringTimerTask
   */
  public final static String MONITORING_RESULT_LOGGER_PROPERTY = "LOG_MONITORING_RESULT_LOGGER";
  /**
   *  Default value for the logger name of the results for the log monitoring task
   * in the server, value is <code>fr.dyade.aaa.agent.Monitoring</code>.
   * 
   * @see fr.dyade.aaa.common.monitoring.LogMonitoringTimerTask
   */
  public final static String DEFAULT_MONITORING_RESULT_LOGGER = "fr.dyade.aaa.Monitoring";
  
  /**
   *  Name of property allowing to fix the logging level of the results for the
   * log monitoring task in the server.
   * <p>
   *  This property can be fixed either from <code>java</code> launching command,
   * or in <code>a3servers.xml</code> configuration file.
   * 
   * @see fr.dyade.aaa.common.monitoring.LogMonitoringTimerTask
   */
  public final static String MONITORING_RESULT_LEVEL_PROPERTY = "LOG_MONITORING_RESULT_LEVEL";
  /**
   *  Default value for the logging level of the results for the log monitoring task
   * in the server, value is <code>WARN</code>.
   * 
   * @see fr.dyade.aaa.common.monitoring.LogMonitoringTimerTask
   */
  public final static int DEFAULT_MONITORING_RESULT_LEVEL = BasicLevel.WARN;
  
  /**
   *  Name of property allowing to set the logging message of the results for the
   * log monitoring task in the server.
   * <p>
   *  This property can be fixed either from <code>java</code> launching command,
   * or in <code>a3servers.xml</code> configuration file.
   * 
   * @see fr.dyade.aaa.common.monitoring.LogMonitoringTimerTask
   */
  public final static String MONITORING_RESULT_MESSAGE_PROPERTY = "LOG_MONITORING_RESULT_MESSAGE";
  /**
   *  Default value for the logging message of the results for the log monitoring task
   * in the server, value is <code>"JMX Monitoring "</code>.
   * 
   * @see fr.dyade.aaa.common.monitoring.LogMonitoringTimerTask
   */
  public final static String DEFAULT_MONITORING_RESULT_MESSAGE = "JMX Monitoring ";

  /** Logger to write results. */
  Logger monitoringLogger;
  /** Message to log. */
  String msg;
  /** Level of monitoring messages. */
  int level;
  
  StringBuffer strbuf = null;

  /**
   * Initializes the <code>LogMonitoringTimerTask</code> component.
   * 
   * @param timer   Timer to use to schedule the resulting task.
   * @param period  Period value of the resulting task
   * @param attlist List of JMX attributes to periodically watch.
   * @param logger  Logger to write results.
   * @param msg     Message to log.
   * @param level   Level of monitoring messages.
   */
  public LogMonitoringTimerTask(Timer timer, long period, Properties attlist,
                                Logger logger, String msg, int level) {
    super(period, attlist);
    
    this.monitoringLogger = logger;
    this.msg = msg;
    this.level = level;
    strbuf = new StringBuffer();

    start(timer);
  }

  /**
   * Initialize the record for the current collect time.
   * For the FileMonitoringTimer, it consists to initialize a StringBuffer to collect
   * informations about all attributes.
   * 
   * @see fr.dyade.aaa.common.monitoring.MonitoringTimerTask#initializeRecords()
   */
  protected void initializeRecords() {
    strbuf.append(msg).append('[');
  }

  /**
   * Records information about the specified attribute.
   * 
   * @param mbean The name of the related mbean.
   * @param att   The name of the related attribute.
   * @param value The value of the related attribute.
   * 
   * @see fr.dyade.aaa.common.monitoring.MonitoringTimerTask#addRecord(javax.management.ObjectName, java.lang.String, java.lang.Object)
   */
  protected void addRecord(String mbean, String att, Object value) {
    strbuf.append('(').append(mbean).append(':').append(att).append('=').append(value).append(')');
  }

  /**
   * Finalize the record for the current time.
   * 
   * @see fr.dyade.aaa.common.monitoring.MonitoringTimerTask#finalizeRecords()
   */
  protected void finalizeRecords() {
    strbuf.append(']');
    
    if (monitoringLogger.isLoggable(level))
      monitoringLogger.log(level, strbuf.toString());

    strbuf.setLength(0);
  }
}
