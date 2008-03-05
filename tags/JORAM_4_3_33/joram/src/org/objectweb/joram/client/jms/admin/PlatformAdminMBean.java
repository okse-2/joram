/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2005 - ScalAgent Distributed Technologies
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
 * Initial developer(s): Nicolas Tachker (ScalAgent)
 * Contributor(s):
 */
package org.objectweb.joram.client.jms.admin;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.List;


/**
 *
 */
public interface PlatformAdminMBean {

  public void connect(javax.jms.TopicConnectionFactory cnxFact, 
                      String name,
                      String password)
    throws ConnectException, AdminException;
  
  public void connect(String hostName,
                      int port,
                      String name,
                      String password,
                      int cnxTimer,
                      String reliableClass)
    throws UnknownHostException, ConnectException, AdminException;

  public void connect(String name, String password, int cnxTimer)
    throws UnknownHostException, ConnectException, AdminException;

  public void collocatedConnect(String name, String password)
    throws ConnectException, AdminException;

  public void disconnect();

  public void exit();

  public void stopServer(int serverId)
    throws ConnectException, AdminException;

  public void stopServer() 
    throws ConnectException, AdminException;

  public void addServer(int sid,
                        String hostName,
                        String domainName,
                        int port,
                        String serverName)
    throws ConnectException, AdminException;

  public void removeServer(int sid)
    throws ConnectException, AdminException;

  public void addDomain(String domainName,
                        int sid,
                        int port)
    throws ConnectException, AdminException;

  public void removeDomain(String domainName)
    throws ConnectException, AdminException;

  public String getConfiguration()
    throws ConnectException, AdminException;

  public List getServersIds();

  public List getServersIds(String domainName) 
    throws ConnectException, AdminException;

  public String[] getDomainNames(int serverId) 
    throws ConnectException, AdminException;

  public void setDefaultThreshold(int serverId, int threshold)
    throws ConnectException, AdminException;

  public void setDefaultThreshold(int threshold)
    throws ConnectException, AdminException;

  public int getDefaultThreshold(int serverId)
    throws ConnectException, AdminException;

  public int getDefaultThreshold()
    throws ConnectException, AdminException;

  public int getLocalServerId() 
    throws ConnectException;

  public String getLocalHost() 
    throws ConnectException;

  public int getLocalPort() 
    throws ConnectException;
}
