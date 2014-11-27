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
package com.scalagent.ctrlgreen;

import javax.jms.JMSException;

import org.objectweb.joram.client.jms.ConnectionFactory;
import org.objectweb.joram.client.jms.FactoryParameters;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

public class ScopeBRFramework {
  ClientFramework framework = null;
  
  /**
   * Initialization of ScopeBR framework.
   * 
   * @param host          hostname of Joram server.
   * @param port          listening port of Joram server.
   * @param actionHandler Handler for actions.
   * @param CMDBHandler   Handler for CMDB inventory.
   * @throws Exception if the framework can not initialize.
   */
  public ScopeBRFramework(String host, int port,
                          ActionHandler actionHandler,
                          InventoryHandler CMDBHandler) throws Exception {
    ConnectionFactory cf = TcpConnectionFactory.create(host, port);
    FactoryParameters parameters = cf.getParameters();
    parameters.connectingTimer = 10;
    parameters.cnxPendingTimer = 5000;
    
    try {
      framework = new ClientFramework("ScopeBR", cf);
    } catch (Exception exc) {
      Trace.fatal("DNSFramework: Cannot initialize framework.", exc);
      throw exc;
    }
    try {
      framework.setActionHandler(actionHandler);
    } catch (Exception exc) {
      Trace.fatal("DNSFramework: Cannot initialize framework.", exc);
      close();
      throw exc;
    }
    try {
      framework.setInventoryCMDBHandler(CMDBHandler);
    } catch (Exception exc) {
      Trace.fatal("TuneFramework: Cannot initialize framework.", exc);
      close();
      throw exc;
    }
  }
  /**
   * Invokes an action trough the framework.
   * 
   * @param name the name of receiver
   * @param action The action to invoke
   * @return an object describing the result of the action.
   * @throws JMSException 
   */
  public ActionReturn invokeAction(String name, Action action) throws JMSException {
    return framework.invokeAction(name, action);
  }
  
  /**
   * Invokes an action trough the framework.
   * This call blocks until the reply arrives or the timeout expires.
   * If timeout is 0 the request is asynchronous and do not wait reply.
   * 
   * @param name    the name of receiver
   * @param action  The action to invoke
   * @param timeout the timeout value (in milliseconds)
   * @return an object describing the result of the action.
   * @throws JMSException 
   */
  public ActionReturn invokeAction(String name, Action action, long timeout) throws JMSException {
    return framework.invokeAction(name, action, timeout);
  }
  
  /**
   * Invokes an asynchronous action trough the framework.
   * 
   * @param name    the name of receiver
   * @param action  The action to invoke
   * @throws JMSException 
   */
  void throwAction(String name, Action action) throws JMSException {
    framework.throwAction(name, action);
  }

  /**
   * Close the framework.
   */
  public void close() {
    if (framework != null)
      framework.close();
    framework = null;
  }
}
