/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2012 ScalAgent Distributed Technologies
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
package joram.ctrlgreen;

import java.net.ConnectException;
import java.util.Properties;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Queue;

import org.objectweb.joram.client.jms.ConnectionFactory;
import org.objectweb.joram.client.jms.FactoryParameters;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminException;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

public class ClientFramework implements JoramFramework, ClientFrameworkMBean {
  private static ClientFramework framework = null;

  private String name = null;
  private Connection cnx = null;
  
  Queue actionQueue = null;
  Topic CMDBTopic = null;
  Topic VMWareTopic = null;
  Queue parameterQueue = null;

  public ClientFramework(String name, ConnectionFactory cf) throws Exception {
    this.name = name;
    try {
      AdminModule.connect(cf, "root", "root");
    } catch (ConnectException | AdminException exc) {
      Trace.fatal("Cannot connect.", exc);
      throw exc;
    }

    try {
      actionQueue = org.objectweb.joram.client.jms.Queue.create("Action");
      CMDBTopic = org.objectweb.joram.client.jms.Topic.create("CMDB");
      VMWareTopic = org.objectweb.joram.client.jms.Topic.create("VMWare");
      parameterQueue= org.objectweb.joram.client.jms.Queue.create("Parameter");
    } catch (ConnectException | AdminException exc) {
      Trace.fatal("Cannot get destinations.", exc);
      AdminModule.disconnect();
      throw exc;
    }
    
    AdminModule.disconnect();
    
    try {
      cnx = cf.createConnection("anonymous", "anonymous");
    } catch (JMSException exc) {
      Trace.fatal("Cannot connect.", exc);
      throw exc;
    }
    cnx.start();
  }
  
  public void close() {
    if (actionListener != null) actionListener.close();
    actionListener = null;
    if (requestor != null) requestor.close();
    requestor = null;
    if (parameterListener != null) parameterListener.close();
    parameterListener = null;
    if (parameterPublisher != null) parameterPublisher.close();
    parameterPublisher = null;
    if (cmdbListener != null) cmdbListener.close();
    cmdbListener = null;
    if (cmdbPublisher != null) cmdbPublisher.close();
    cmdbPublisher = null;
    if (vmwareListener != null) vmwareListener.close();
    vmwareListener = null;
    if (vmwarePublisher != null) vmwarePublisher.close();
    vmwarePublisher = null;

    try {
      if (cnx != null)
        cnx.close();
    } catch (JMSException exc) {
      Trace.error("ClientFramework: Cannot close connexion.");
    } finally {
      cnx = null;
    }
  }

  /**
   * Instantiates and initializes the Joram Framework.
   * 
   * @return The created framework.
   * @throws Exception 
   */
  public static ClientFramework createFramework(String name, String host, int port) throws Exception {
    Trace.debug("ClientFramework.createFramework()");

    if (framework != null)
      throw new IllegalStateException("Framework is already initialized");

    // Initializes the ConnectionFactory.
    ConnectionFactory cf = TcpConnectionFactory.create(host, port);
    FactoryParameters parameters = cf.getParameters();
    parameters.connectingTimer = 5;
    parameters.cnxPendingTimer = 5000;

    framework = new ClientFramework(name, cf);
    return framework;
  }

  ActionListener actionListener = null;
  
  @Override
  public synchronized void setActionHandler(ActionHandler handler) throws Exception {
    if (actionListener != null) {
      Trace.error("Action handler is already set.");
      throw new Exception("Action handler is already set.");
    }
    try {
      actionListener = new ActionListener(handler, cnx, actionQueue, name);
    } catch (JMSException exc) {
      Trace.error("Cannot initialize action framework.");
      throw exc;
    }
  }

  ActionRequestor requestor = null;
  
  synchronized ActionRequestor getActionRequestor() throws JMSException {
    try {
      if (requestor == null)
        requestor = new ActionRequestor(cnx);
      return requestor;
    } catch (JMSException exc) {
      Trace.error("Cannot initialize the requestor.", exc);
      throw exc;
    }
  }
  
  @Override
  public ActionReturn invokeAction(String name, Action action) throws JMSException {
    ActionReturn actionReturn = getActionRequestor().request(actionQueue, name, action);
    return actionReturn;
  }

  ParameterListener parameterListener = null;
  
  @Override
  public synchronized void setParameterHandler(ParameterHandler handler) throws Exception {
    if (parameterListener != null) {
      Trace.error("Parameter handler is already set.");
      throw new Exception("Parameter handler is already set.");
    }
    try {
      parameterListener = new ParameterListener(handler, cnx, parameterQueue);
    } catch (JMSException exc) {
      Trace.error("Cannot initialize parameter framework.");
      throw exc;
    }
  }

  ParameterPublisher parameterPublisher = null;
  
  synchronized ParameterPublisher getParameterPublisher() throws JMSException {
    try {
      if (parameterPublisher == null)
        parameterPublisher = new ParameterPublisher(cnx, parameterQueue);
      return parameterPublisher;
    } catch (JMSException exc) {
      Trace.error("Cannot initialize a parameter publisher.", exc);
      throw exc;
    }
  }
  
  @Override
  public void publishParameters(Properties parameters) throws JMSException {
    getParameterPublisher().publish(parameters);
  }

  InventoryListener cmdbListener = null;
  
  @Override
  public synchronized void setInventoryCMDBHandler(InventoryHandler handler) throws Exception {
    if (cmdbListener != null) {
      Trace.error("CMDB inventory handler is already set.");
      throw new Exception("CMDB inventory handler is already set.");
    }
    try {
      cmdbListener = new InventoryListener(handler, cnx, CMDBTopic);
    } catch (JMSException exc) {
      Trace.error("Cannot initialize CMDB inventory framework.");
      throw exc;
    }
  }

  InventoryPublisher cmdbPublisher = null;
  
  synchronized InventoryPublisher getCMDBPublisher() throws JMSException {
    try {
      if (cmdbPublisher == null)
        cmdbPublisher = new InventoryPublisher(cnx, CMDBTopic);
      return cmdbPublisher;
    } catch (JMSException exc) {
      Trace.error("Cannot initialize a CMDB Inventory publisher.", exc);
      throw exc;
    }
  }
  
  @Override
  public void publishInventoryCMDB(String inventory) throws JMSException {
    getCMDBPublisher().publish(inventory);
  }

  InventoryListener vmwareListener = null;
  
  @Override
  public synchronized void setInventoryVMWareHandler(InventoryHandler handler) throws Exception {
    if (vmwareListener != null) {
      Trace.error("VMWare inventory handler is already set.");
      throw new Exception("VMWare inventory handler is already set.");
    }
    try {
      vmwareListener = new InventoryListener(handler, cnx, VMWareTopic);
    } catch (JMSException exc) {
      Trace.error("Cannot initialize VMWare inventory framework.");
      throw exc;
    }
  }

  InventoryPublisher vmwarePublisher = null;
  
  synchronized InventoryPublisher getVMWarePublisher() throws JMSException {
    try {
      if (vmwarePublisher == null)
        vmwarePublisher = new InventoryPublisher(cnx, VMWareTopic);
      return vmwarePublisher;
    } catch (JMSException exc) {
      Trace.error("Cannot initialize a VMWare Inventory publisher.", exc);
      throw exc;
    }
  }

  @Override
  public void publishInventoryVMWare(String inventory) throws JMSException {
    getVMWarePublisher().publish(inventory);
  }
}
