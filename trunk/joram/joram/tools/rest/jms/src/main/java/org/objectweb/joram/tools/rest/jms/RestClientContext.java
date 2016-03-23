/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2016 ScalAgent Distributed Technologies
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
package org.objectweb.joram.tools.rest.jms;

import java.util.ArrayList;
import java.util.List;

import javax.jms.ConnectionFactory;
import javax.jms.JMSContext;

public class RestClientContext {

  private String clientId;
  private ConnectionFactory cf;
  private JMSContext jmsContext;
  private List<String> sessionCtxNames;
  private long lastActivity = 0;
  private long idleTimeout = -1;

  public RestClientContext(String clientId) {
    this.clientId = clientId;
    sessionCtxNames = new ArrayList<String>();
  }

  /**
   * @return the cf
   */
  public ConnectionFactory getConnectionFactory() {
    return cf;
  }

  /**
   * @param cf
   *          the cf to set
   */
  public void setConnectionFactory(ConnectionFactory cf) {
    this.cf = cf;
  }

  /**
   * @return the jmsContext
   */
  public JMSContext getJmsContext() {
    return jmsContext;
  }

  /**
   * @param jmsContext
   *          the jmsContext to set
   */
  public void setJmsContext(JMSContext jmsContext) {
    this.jmsContext = jmsContext;
  }

  /**
   * @return the clientId
   */
  public String getClientId() {
    return clientId;
  }
  
  /**
   * @return the sessionCtxNames
   */
  public List<String> getSessionCtxNames() {
    return sessionCtxNames;
  }
  
  public boolean addSessionCtxNames(String ctxName) {
    return sessionCtxNames.add(ctxName);
  }
  
  public boolean removeSessionCtxNames(String ctxName) {
    return sessionCtxNames.remove(ctxName);
  }
  
  /**
   * @return true if this list contains no elements.
   */
  public boolean isSessionCtxNamesEmpty() {
    return sessionCtxNames.isEmpty();
  }

  /**
   * @param activityTime the last activity time to set
   */
  public synchronized void setLastActivity(long activityTime) {
    this.lastActivity = activityTime;
  }

  /**
   * @return the last activity time
   */
  public long getLastActivity() {
    return lastActivity;
  }

  /**
   * @return the idleTimeout
   */
  public long getIdleTimeout() {
    return idleTimeout;
  }

  /**
   * @param idleTimeout the idleTimeout to set in second
   */
  public void setIdleTimeout(long idleTimeout) {
    this.idleTimeout = idleTimeout*1000;
  }
  
}
