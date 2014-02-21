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

import java.util.Properties;

import javax.jms.JMSException;

public interface JoramFramework {
  /**
   * Set an handler listening Action event for this framework.
   * 
   * @param handler   The action handler.
   * @throws Exception 
   */
  void setActionHandler(ActionHandler handler) throws Exception;
  
  /**
   * Invokes an action trough the framework.
   * 
   * @param name the name of receiver
   * @param action The action to invoke
   * @return an object describing the result of the action.
   * @throws JMSException 
   */
  ActionReturn invokeAction(String name, Action action) throws JMSException;
  
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
  ActionReturn invokeAction(String name, Action action, long timeout) throws JMSException;
  
  /**
   * Invokes an asynchronous action trough the framework.
   * 
   * @param name    the name of receiver
   * @param action  The action to invoke
   * @throws JMSException 
   */
  void throwAction(String name, Action action) throws JMSException;
  
  /**
   * Set an handler listening parameters for this framework.
   * 
   * @param handler The parameters handler.
   * @throws Exception 
   */
  void setParameterHandler(ParameterHandler handler) throws Exception;
  
  /**
   * Set an handler listening CMDB inventory for this framework.
   * 
   * @param handler The CMDB inventory handler.
   * @throws Exception 
   */
  
  void setInventoryCMDBHandler(InventoryHandler handler) throws Exception;
  
  /**
   * Publish a CMDB inventory for this framework.
   * 
   * @param inventory The inventory.
   * @throws JMSException 
   */
  void publishInventoryCMDB(Inventory inventory) throws JMSException;
  
  /**
   * Set an handler listening VMWare inventory for this framework.
   * 
   * @param handler The VMWare inventory handler.
   * @throws Exception 
   */
  void setInventoryVMWareHandler(InventoryHandler handler) throws Exception;
  
  /**
   * Publish a VMWare inventory for this framework.
   * 
   * @param inventory The inventory.
   * @throws JMSException 
   */
  void publishInventoryVMWare(Inventory inventory) throws JMSException;
  
  /**
   * Publish parameters.
   * 
   * @param parameters
   * @throws JMSException
   */
  void publishParameters(Parameters parameters) throws JMSException;
}
