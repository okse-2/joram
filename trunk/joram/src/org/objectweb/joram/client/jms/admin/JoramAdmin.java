/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2005 - 2009 ScalAgent Distributed Technologies
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
 * Contributor(s): Benoit Pelletier (Bull SA)
 */
package org.objectweb.joram.client.jms.admin;

import java.net.ConnectException;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.JMSSecurityException;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.util.Debug;
import fr.dyade.aaa.util.management.MXWrapper;

/**
 * JoramAdmin is the implementation of the interface JoramAdminMBean.
 * It must only be used to allow administration through JMX.
 * 
 * @see AdminModule
 * @see AdminWrapper
 */
public class JoramAdmin extends AdminWrapper implements JoramAdminMBean {

  public static Logger logger = Debug.getLogger(JoramAdmin.class.getName());

  /**
   * Creates a MBean to administer Joram using the default basename for JMX
   * registering ({@link #JoramAdmin(Connection, String)}).
   * Be careful, if the connection is not started this method will failed with
   * a ConnectException.
   * 
   * @param cnx A valid connection to the Joram server.
   * @throws JMSException A problem occurs during initialization.
   * 
   * @see AdminWrapper#AdminWrapper(Connection)
   */
  public JoramAdmin(Connection cnx) throws ConnectException, AdminException, JMSException {
    this(cnx, "joramClient");
  }

  /**
   * Creates a MBean to administer Joram using the given basename for JMX registering.
   * Be careful, if the connection is not started this method will failed with a
   * ConnectException.
   * 
   * @param cnx   A valid connection to the Joram server.
   * @param base  the basename for registering the MBean.
   * 
   * @throws JMSException A problem occurs during initialization.
   * 
   * @see AdminWrapper#AdminWrapper(Connection)
   */
  public JoramAdmin(Connection cnx, String base) throws ConnectException, AdminException, JMSException {
    super(cnx);
    registerMBean(base);
  }

  public void exit() {
    // TODO (AF): Close the internal AdminRequestor if it is defined
    unregisterMBean();
  }

  transient protected String JMXBaseName = null;

  public void registerMBean(String base) {
    if (MXWrapper.mxserver == null) return;

    JMXBaseName = base;
    
    try {
      MXWrapper.registerMBean(this, JMXBaseName, "type=JoramAdmin");
    } catch (Exception e) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "JoramAdmin.registerMBean", e);
    }
  }

  public void unregisterMBean() {
    try {
      MXWrapper.unregisterMBean(JMXBaseName, "type=JoramAdmin");
    } catch (Exception e) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "JoramAdmin.unregisterMBean",e);
    }
  }

  /**
   * Opens a connection dedicated to administering with the Joram server
   * which parameters are wrapped by a given <code>ConnectionFactory</code>.
   *
   * @param cf       The Joram's ConnectionFactory to use for connecting.
   * @param name          Administrator's name.
   * @param password      Administrator's password.
   * @param identityClass identity class name.
   *
   * @exception ConnectException  If connecting fails.
   * @exception AdminException  If the administrator identification is incorrect.
   */
  static JoramAdmin doCreate(AbstractConnectionFactory cf,
                             String name,
                             String password,
                             String identityClass) throws ConnectException, AdminException {
    Connection cnx = null;

    //  set identity className
    cf.setIdentityClassName(identityClass);
    try {
      cnx = cf.createConnection(name, password);
      cnx.start();

      return new JoramAdmin(cnx);
    } catch (JMSSecurityException exc) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "JoramAdmin - error during creation", exc);
      throw new AdminException(exc.getMessage());
    } catch (JMSException exc) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "JoramAdmin - error during creation", exc);
      throw new ConnectException("Connecting failed: " + exc);
    }
  }

  /**
   * This method execute the XML script file that the location is given
   * in parameter.
   * <p>
   * Be careful, currently this method use the static administration connection
   * through the AdminModule Class.
   *
   * @param cfgDir        The directory containing the file.
   * @param cfgFileName   The script filename.
   */
  public void executeXMLAdmin(String cfgDir,
                                 String cfgFileName) throws Exception {
    AdminModule.executeXMLAdmin(cfgDir, cfgFileName);
  }

  /**
   * This method execute the XML script file that the pathname is given
   * in parameter.
   * <p>
   * Be careful, currently this method use the static administration connection
   * through the AdminModule Class.
   *
   * @param path    The script pathname.
   */
  public void executeXMLAdmin(String path) throws Exception {
    AdminModule.executeXMLAdmin(path);
  }

  /**
   * Export the repository content to an XML file
   * - only the destinations objects are retrieved in this version
   * - xml script format of the admin objects (joramAdmin.xml)
   * <p>
   * Be careful, currently this method use the static administration connection
   * through the AdminModule Class.
   * 
   * @param exportDir       target directory where the export file will be put
   * @param exportFilename  filename of the export file
   * @throws AdminException if an error occurs
   */
  public void exportRepositoryToFile(String exportDir,
                                     String exportFilename) throws AdminException {
    AdminModule.exportRepositoryToFile(exportDir, exportFilename);
  }
}
