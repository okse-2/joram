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

import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Properties;
import java.util.Timer;

import org.objectweb.util.monolog.api.BasicLevel;

/**
 * The <code>FileMonitoringTimerTask</code> class allows to periodically watch JMX attributes
 * and store the corresponding values to a file in CSV format.
 */
public class FileMonitoringTimerTask extends MonitoringTimerTask {
  /**
   *  Name of property allowing to fix the scanning period for the csv file monitoring
   * task in the server.
   * <p>
   *  This property can be fixed either from <code>java</code> launching command,
   * or in <code>a3servers.xml</code> configuration file.
   * 
   * @see fr.dyade.aaa.common.monitoring.FileMonitoringTimerTask
   */
  public final static String MONITORING_CONFIG_PERIOD_PROPERTY = "FILE_MONITORING_CONFIG_PERIOD";
  /**
   *  Default value for the scanning period for the  csv file monitoring task in the
   * server, value is <code>60000L</code> (60 seconds).
   * 
   * @see fr.dyade.aaa.common.monitoring.FileMonitoringTimerTask
   */
  public final static long DEFAULT_MONITORING_CONFIG_PERIOD = 60000L;

  /**
   *  Name of property allowing to fix the pathname of a configuration file for a
   * csv file monitoring task in the server.
   * <p>
   *  This property can be fixed either from <code>java</code> launching command,
   * or in <code>a3servers.xml</code> configuration file.
   * 
   * @see fr.dyade.aaa.common.monitoring.FileMonitoringTimerTask
   */
  public final static String MONITORING_CONFIG_PATH_PROPERTY = "FILE_MONITORING_CONFIG_PATH";
  /**
   *  Default value for the pathname of a configuration file for a csv file monitoring
   * task in the server, value is <code>fileMonitoring.props</code>.
   * <p>
   *  If the file does not exist the timer task is not launched.
   * 
   * @see fr.dyade.aaa.common.monitoring.FileMonitoringTimerTask
   */
  public final static String DEFAULT_MONITORING_CONFIG_PATH = "fileMonitoring.props";
  
  /**
   *  Name of property allowing to fix the pathname of the results file for the
   * csv file monitoring task in the server.
   * <p>
   *  This property can be fixed either from <code>java</code> launching command,
   * or in <code>a3servers.xml</code> configuration file.
   * 
   * @see fr.dyade.aaa.common.monitoring.FileMonitoringTimerTask
   */
  public final static String MONITORING_RESULT_PATH_PROPERTY = "FILE_MONITORING_RESULT_PATH";
  /**
   *  Default value for the pathname of the results file for the monitoring task
   * in the server, value is <code>monitoringStats.csv</code>.
   * 
   * @see fr.dyade.aaa.common.monitoring.FileMonitoringTimerTask
   */
  public final static String DEFAULT_MONITORING_RESULT_PATH = "monitoringStats.csv";

  FileWriter writer;
  StringBuffer strbuf = null;
  Timer timer;
  
  /**
   * Initializes the <code>FileMonitoringTimerTask</code> component.
   * 
   * @param timer   Timer to use to schedule the resulting task.
   * @param period  Period value of the resulting task
   * @param attlist List of JMX attributes to periodically watch.
   * @param path    Pathname of resulting CSV file.
   */
  public FileMonitoringTimerTask(Timer timer, long period, Properties attlist, String path) {
    super(period, attlist);
    
    try {
      writer = new FileWriter(path, true);
    } catch (IOException exc) {
      logger.log(BasicLevel.ERROR,
                 "FileMonitoringTimerTask.<init>, cannot open file \"" + path + "\"", exc);
    }
    strbuf = new StringBuffer();
    
    start(timer);
  }
  
  /**
   * Instantiates the <code>FileMonitoringTimerTask</code> component.
   *
   */
  public FileMonitoringTimerTask() {}
  
  /**
   * Pathname of the result file.
   */
  public String path = null;
  
  /**
   * Initializes the <code>FileMonitoringTimerTask</code> component.
   *
   */
  public void init(Timer timer, long period, Properties attlist, Properties taskProps){
  	super.period = period;
  	super.attlist = (Properties) attlist.clone();

    path = taskProps.getProperty("resultPath");
    
    try {
        writer = new FileWriter(path, true);
      } catch (Exception exc) {
        logger.log(BasicLevel.ERROR,
                   "FileMonitoringTimerTask.<init>, cannot open file \"" + path + "\"", exc);
      }
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
  	Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(System.currentTimeMillis());
		strbuf.append(cal.get(Calendar.YEAR)).append('/')
				.append(cal.get(Calendar.MONTH)+1).append('/')
				.append(cal.get(Calendar.DATE)).append(" ")
				.append(cal.get(Calendar.HOUR_OF_DAY)).append(":")
				.append(cal.get(Calendar.MINUTE)).append(':')
				.append(cal.get(Calendar.SECOND)).append(',')
				.append(cal.get(Calendar.MILLISECOND)).append(';');
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
    strbuf.append(mbean).append(':').append(att).append(';').append(value).append(';');
  }
  
  /**
   * Finalize the record for the current time.
   * 
   * @see fr.dyade.aaa.common.monitoring.MonitoringTimerTask#finalizeRecords()
   */
  protected void finalizeRecords() {
    strbuf.append('\n');
    
    try {
      writer.write(strbuf.toString());
      writer.flush();
    } catch (IOException exc) {
      logger.log(BasicLevel.ERROR,
                 "FileMonitoringTimerTask.finalizeRecords, cannot write records.", exc);
    }

    strbuf.setLength(0);
  }

  /**
   * Close the result file, be careful you have to call this method only if the
   * monitoring task is stopped.
   */
  public void close() {
    try {
      writer.flush();
      writer.close();
    } catch (IOException exc) {
      logger.log(BasicLevel.ERROR,
                 "FileMonitoringTimerTask.close, cannot close file.", exc);
    }
  }
}
