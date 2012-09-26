/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - 2008 ScalAgent Distributed Technologies
 * Copyright (C) 2004 - Bull SA
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
 * Initial developer(s): Frederic Maistre (Bull SA)
 * Contributor(s): Nicolas Tachker (Bull SA)
 *                 ScalAgent Distributed Technologies
 */
package org.objectweb.joram.client.connector;

import javax.jms.ConnectionFactory;
import javax.jms.IllegalStateException;
import javax.jms.JMSException;
import javax.jms.JMSSecurityException;
import javax.jms.QueueConnectionFactory;
import javax.jms.TopicConnectionFactory;
import javax.resource.ResourceException;
import javax.resource.spi.CommException;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.SecurityException;

import org.objectweb.joram.client.jms.admin.AbstractConnectionFactory;
import org.objectweb.joram.client.jms.tcp.QueueTcpConnectionFactory;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;
import org.objectweb.joram.client.jms.tcp.TopicTcpConnectionFactory;
import org.objectweb.util.monolog.api.BasicLevel;

/** 
 * The <code>DefaultConnectionManager</code> class is the default connection
 * manager provided with JORAM resource adapter, which intercepts connections
 * requests coming from non managed client applications.
 */
public class DefaultConnectionManager
             implements javax.resource.spi.ConnectionManager,
                        java.io.Serializable
{
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  /**
   * Static reference to the local <code>DefaultConnectionManager</code>
   * instance.
   */
  private static DefaultConnectionManager ref = null;


  /**
   * Creates a <code>DefaultConnectionManager</code> instance.
   */
  public DefaultConnectionManager()
  {}


  /**
   * Returns a <code>javax.jms.Connection</code> connection instance for
   * a non managed application.
   *
   * @exception CommException      If connecting fails.
   * @exception SecurityException  If connecting is not authorized.
   * @exception ResourceException  Generic exception.
   */
  public Object allocateConnection(ManagedConnectionFactory mcf,
                                   ConnectionRequestInfo cxRequest)
    throws ResourceException {
    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
      AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG, 
                                    this + " allocateConnection(" + mcf + "," + cxRequest + ")");
    
    String userName;
    String password;
    String identityClass;
    
    if (cxRequest == null) {
      userName = ((ManagedConnectionFactoryImpl) mcf).getUserName();
      password = ((ManagedConnectionFactoryImpl) mcf).getPassword();
      identityClass = ((ManagedConnectionFactoryImpl) mcf).getIdentityClass();
    } else {
      userName = ((ConnectionRequest) cxRequest).getUserName();
      password = ((ConnectionRequest) cxRequest).getPassword();
      identityClass = ((ConnectionRequest) cxRequest).getIdentityClass();
    }
    
    String hostName = ((ManagedConnectionFactoryImpl) mcf).getHostName();
    int serverPort =
      ((ManagedConnectionFactoryImpl) mcf).getServerPort().intValue();
    
    try {
      if (cxRequest instanceof QueueConnectionRequest) {
        QueueConnectionFactory factory = QueueTcpConnectionFactory.create(hostName, serverPort);
        setFactoryParameters((AbstractConnectionFactory) factory, (ManagedConnectionFactoryImpl) mcf);
        ((AbstractConnectionFactory) factory).setIdentityClassName(identityClass);
        return factory.createQueueConnection(userName, password);
      } else if (cxRequest instanceof TopicConnectionRequest) {
        TopicConnectionFactory factory = TopicTcpConnectionFactory.create(hostName, serverPort);
        setFactoryParameters((AbstractConnectionFactory) factory, (ManagedConnectionFactoryImpl) mcf);
        ((AbstractConnectionFactory) factory).setIdentityClassName(identityClass);
        return factory.createTopicConnection(userName, password);
      } else {
        ConnectionFactory factory = TcpConnectionFactory.create(hostName, serverPort);
        setFactoryParameters((AbstractConnectionFactory) factory, (ManagedConnectionFactoryImpl) mcf);
        ((AbstractConnectionFactory) factory).setIdentityClassName(identityClass);
        return factory.createConnection(userName, password);
      }
    } catch (IllegalStateException exc) {
      throw new CommException("Could not access the JORAM server: " + exc);
    } catch (JMSSecurityException exc) {
      throw new SecurityException("Invalid user identification: " + exc);
    } catch (JMSException exc) {
      throw new ResourceException("Failed connecting process: " + exc);
    }
  }

  private void setFactoryParameters(AbstractConnectionFactory factory , ManagedConnectionFactoryImpl mcf) {
    if (AdapterTracing.dbgAdapter.isLoggable(BasicLevel.DEBUG))
      AdapterTracing.dbgAdapter.log(BasicLevel.DEBUG, 
                                    this + " setFactoryParameters(" + factory + "," + mcf + ")");   
    factory.getParameters().connectingTimer = mcf.getConnectingTimer();
    factory.getParameters().cnxPendingTimer = mcf.getCnxPendingTimer();
    factory.getParameters().txPendingTimer = mcf.getTxPendingTimer();
    factory.getParameters().asyncSend = mcf.isAsyncSend();
    factory.getParameters().multiThreadSync = mcf.isMultiThreadSync();
    factory.getParameters().multiThreadSyncDelay = mcf.getMultiThreadSyncDelay();
    factory.getParameters().outLocalAddress = mcf.getOutLocalAddress();
    factory.getParameters().outLocalPort = mcf.getOutLocalPort().intValue();
    
  }

  /**
   * Returns the reference to the <code>DefaultConnectionManager</code>
   * instance, creates it if needed.
   */
  static DefaultConnectionManager getRef()
  {
    if (ref == null)
      ref = new DefaultConnectionManager();

    return ref;
  }
}
