/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
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
 */
package org.objectweb.joram.client.connector;


/**
 * The <code>LocalServerMBean<code> interface defines the administration
 * methods provided by the <code>LocalServer</code> class.
 */
public interface LocalServerMBean {
  public String getPlatformConfiguration();
  
  public java.util.List getPlatformServersIds();
  
  public String getLocalServerId();
  
  public String getRelease();
  
  public String getRunningMode();
  
  public String getHost();
  
  public String getPort();
  
  public java.util.List getLocalQueuesNames();
  
  public java.util.List getLocalTopicsNames();
  
  public java.util.List getLocalUsersNames();
  
  
  public void createLocalQueue(String jndiName) throws Exception;
  
  public void createLocalTopic(String jndiName) throws Exception;
  
  public String getobjectName();
  public boolean iseventProvider();
  public boolean isstateManageable();
  public boolean isstatisticsProvider();
}
