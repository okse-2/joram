/*
 * Copyright (C) 2001 - 2004 ScalAgent Distributed Technologies
 * Copyright (C) 2004 - France Telecom R&D
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
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
package fr.dyade.aaa.agent;

import java.util.*;

import fr.dyade.aaa.util.Transaction;

import fr.dyade.aaa.agent.conf.*;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

public class ConfigController {

  public final static String ADMIN_SERVER = 
      "fr.dyade.aaa.agent.ADMIN_SERVER";

  public final static String SERVER_COUNTER = "serverCounter";

  private static Logger logger = Debug.getLogger(
    "fr.dyade.aaa.agent.ConfigController");

  public static class Status {
    public static final int FREE = 0;
    public static final int CONFIG = 1;

    public static String[] names = {
      "FREE", "CONFIG"};
    
    public static String toString(int status) {
      return names[status];
    }
  }

  private short serverCounter;

  private A3CMLConfig a3cmlConfig;

  private int status;

  private Vector newServers;

  private Vector stopScript;

  private Vector startScript;

  ConfigController() throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "ConfigController.<init>()");
    // DF: must be improved. The admin server id can be
    // higher than zero.
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 " -> AgentServer.getServerId() = " + 
                 AgentServer.getServerId());
    if (AgentServer.getServerId() == 0) {
      Transaction transaction = AgentServer.getTransaction();
      Short counter = (Short)transaction.load(SERVER_COUNTER);
      if (counter != null) {
        serverCounter = counter.shortValue();
      } else {
        serverCounter = 1;
      }
    } else {
      serverCounter = -1;
    }
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 " -> serverCounter = " + serverCounter);

    setStatus(Status.FREE);
  }

  private void setStatus(int status) {
    this.status = status;
  }
  
  public synchronized void beginConfig() throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "ConfigController.beginConfig()");
    while (status != Status.FREE) {
      try {
	wait();
      } catch (InterruptedException exc) {
      }
    }
    a3cmlConfig = AgentServer.getConfig();
    newServers = new Vector();
    startScript = new Vector();
    stopScript = new Vector();
    setStatus(Status.CONFIG);    
  }

  public synchronized void commitConfig() throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "ConfigController.commitConfig()");
    checkStatus(Status.CONFIG);

    try {
      A3CMLServer root = a3cmlConfig.getServer(AgentServer.getServerId());
      a3cmlConfig.configure((A3CMLPServer) root);
      
      stop();
      
      AgentServer.setConfig(a3cmlConfig, true);

      Transaction transaction = AgentServer.getTransaction();
      transaction.begin();

      a3cmlConfig.save();
      transaction.save(
        new Short(serverCounter), 
        SERVER_COUNTER);

      transaction.commit();
      transaction.release();
      
      addNewServers();

      start();
      
    } finally {
      setStatus(Status.FREE);
      notify();
    }
  }

  private void stop() throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "ConfigController.stop()");
    for (int i = 0; i < stopScript.size(); i++) {
      Object cmd = stopScript.elementAt(i);
      if (cmd instanceof StopNetworkCmd) {
        exec((StopNetworkCmd)cmd);
      }
    }
  }
  
  private void exec(StopNetworkCmd cmd) throws Exception {
    stopNetwork(cmd.domainName);
  }

  private void addNewServers() throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "ConfigController.addNewServers()");
    for (int i = 0; i < newServers.size(); i++) {
      ServerDesc sd = (ServerDesc)newServers.elementAt(i);
      AgentServer.addServerDesc(sd);
    }
  }

  private void start() throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "ConfigController.start()");
    for (int i = 0; i < startScript.size(); i++) {
      Object cmd = startScript.elementAt(i);
      if (cmd instanceof StartNetworkCmd) {
        exec((StartNetworkCmd)cmd);
      } else if (cmd instanceof StartServiceCmd) {
        exec((StartServiceCmd)cmd);
      } else if (cmd instanceof ReconfigureClientNetworkCmd) {
        exec((ReconfigureClientNetworkCmd)cmd);
      } else if (cmd instanceof ReconfigureServerNetworkCmd) {
        exec((ReconfigureServerNetworkCmd)cmd);
      }
    }
    
    // 'Start server' implies that the consumers
    // have been added (done by 'start network').
    for (int i = 0; i < startScript.size(); i++) {
      Object cmd = startScript.elementAt(i);
      if (cmd instanceof StartServerCmd) {
        exec((StartServerCmd)cmd);
      }
    }
  }

  private void exec(StartServerCmd cmd) throws Exception {
    startServer(cmd.sid);
  }

  private void exec(StartNetworkCmd cmd) throws Exception {
    startNetwork(cmd.domainName);
  }

  private void exec(StartServiceCmd cmd) throws Exception {
    startService(cmd.serviceClassName,
                 cmd.args);
  }

  private void exec(ReconfigureClientNetworkCmd cmd) throws Exception {
    reconfigureClientNetwork(cmd.sid,
                             cmd.domainName,
                             cmd.port);
  }
  
  private void exec(ReconfigureServerNetworkCmd cmd) throws Exception {
    reconfigureServerNetwork(cmd.domainName,
                             cmd.port);
  }

  private void checkStatus(int expectedStatus) 
    throws Exception {
    if (status != expectedStatus) {
      throw new Exception("Illegal status: " + 
                          Status.toString(expectedStatus));
    }
  }

  public void addDomain(String name, String className) 
    throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "ConfigController.addDomain(" + name + ',' + className + ')');
    checkStatus(Status.CONFIG);
    try {
      a3cmlConfig.addDomain(new A3CMLDomain(name, className));
    } catch (Exception exc) {
      // idempotent
    }
  }

  public void addServer(String name, String hostName, short id) 
    throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "ConfigController.addServer(" +
                 name + ',' + hostName + ',' + id + ')');
    
    checkStatus(Status.CONFIG);
    if (! a3cmlConfig.containsServer(name)) {
      if (id < 0) {
        if (serverCounter > -1) {
          id = serverCounter++;
        } else {
          throw new Exception("Missing server id");
        }
      }
      a3cmlConfig.addServer(
        new A3CMLPServer(id,
                         name,
                         hostName));       
      ServerDesc serverDesc = new ServerDesc(
        id,
        name,
        hostName,
        -1);
      serverDesc.gateway = id;
      newServers.addElement(serverDesc);
    }
    // else idempotent
    
    if (id != AgentServer.getServerId()) {
      startScript.addElement(
        new StartServerCmd(id));
    }
  }

  public void addService(String serverName,
                         String serviceClassName,
                         String args) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "ConfigController.addService(" + 
                 serverName + ',' + 
                 serviceClassName + ',' + 
                 args + ')');
    checkStatus(Status.CONFIG);
    A3CMLServer server = a3cmlConfig.getServer(serverName);
    A3CMLService newService = new A3CMLService(
      serviceClassName,
      args);
    try {
      server.addService(newService);
    } catch (Exception exc) {
      // Idempotent
    }

    short sid = a3cmlConfig.getServerIdByName(serverName);
    if (sid == AgentServer.getServerId()) {
      startScript.addElement(
        new StartServiceCmd(serviceClassName, args));
    }
  }
  
  public void addNetwork(String serverName,
                         String domainName,
                         int port) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "ConfigController.addNetwork(" + 
                 serverName + ',' + 
                 domainName + ',' + 
                 port + ')');
    checkStatus(Status.CONFIG);
    A3CMLPServer server = (A3CMLPServer)a3cmlConfig.getServer(serverName);
    A3CMLNetwork newNetwork = new A3CMLNetwork(domainName, port);    
    try {
      server.addNetwork(newNetwork);
      A3CMLDomain domain = 
        (A3CMLDomain) a3cmlConfig.getDomain(domainName);
      domain.addServer(server);
    } catch (Exception exc) {
      // Idempotent
    }
    
    short sid = a3cmlConfig.getServerIdByName(serverName);
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 " -> sid = " + sid);
    if (sid == AgentServer.getServerId()) {
      startScript.add(
        new StartNetworkCmd(domainName));
    }
  }

  public void setNetworkPort(String serverName,
                             String domainName,
                             int port) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "ConfigController.setNetworkPort(" + 
                 serverName + ',' + 
                 domainName + ',' + 
                 port + ')');
    checkStatus(Status.CONFIG);
    A3CMLServer server = a3cmlConfig.getServer(serverName);
    A3CMLNetwork network = server.getNetwork(domainName);
    if (network != null) {
      network.port = port;
    } else {
      throw new Exception("Unknown network");
    }
    
    if (server.sid == AgentServer.getServerId()) {
      startScript.add(
        new ReconfigureServerNetworkCmd(domainName, port));
    } else {
      startScript.add(
        new ReconfigureClientNetworkCmd(
          server.sid, domainName, port));
    }
  }

  public void setJVMArgs(String serverName,
                         String jvmArgs) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "ConfigController.setJVMArgs(" + 
                 serverName + ',' + 
                 jvmArgs + ')');
    checkStatus(Status.CONFIG);
    A3CMLServer server = a3cmlConfig.getServer(serverName);
    server.jvmArgs = jvmArgs;
  }

  public void setProperty(String propName,
                          String value) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "ConfigController.setProperty(" + 
                 propName + ',' + 
                 value + ')');
    checkStatus(Status.CONFIG);
    a3cmlConfig.addProperty(
      new A3CMLProperty(propName, value));
  }

  public void setServerProperty(String serverName,
                                String propName,
                                String value) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "ConfigController.setServerProperty(" + 
                 serverName + ',' + 
                 propName + +',' + 
                 value + ')');
    checkStatus(Status.CONFIG);
    A3CMLServer server = a3cmlConfig.getServer(serverName);
    server.addProperty(new A3CMLProperty(propName,
                                         value));
  }

  public void setServerNat(String serverName,
                           String translatedServerName,
                           String translationHostName,
                           int translationPort) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "ConfigController.setServerNat(" + 
                 serverName + ',' +
                 translatedServerName + ',' +
                 translationHostName + ',' +
                 translationPort + ')');
    checkStatus(Status.CONFIG);
    A3CMLServer server = a3cmlConfig.getServer(serverName);
    short sid = a3cmlConfig.getServerIdByName(translatedServerName);
    server.addNat(new A3CMLNat(sid, translationHostName,
                               translationPort));
  }

  public void removeDomain(String domainName) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "ConfigController.removeDomain(" + domainName + ')');
    checkStatus(Status.CONFIG);
    Vector serversToRemove = new Vector();
    A3CMLDomain domain = a3cmlConfig.getDomain(domainName);
    for (int i = 0; i < domain.servers.size(); i++) {
      A3CMLServer server = (A3CMLServer)domain.servers.elementAt(i);
      if (server.networks.size() == 1) {
        serversToRemove.addElement(server);
      } else {
        removeNetwork(server.name, domainName);
      }
    }

    for (int i = 0; i < serversToRemove.size(); i++) {
      A3CMLServer server = 
        (A3CMLServer)serversToRemove.elementAt(i);
      if (server.sid != AgentServer.getServerId()) {
        removeServer(server.name);
      } else {
        // Remove the network
        removeNetwork(server.name, domainName);
      }
    }

    a3cmlConfig.removeDomain(domainName);
  }

  public void removeNetwork(String serverName, 
                            String domainName) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "ConfigController.removeNetwork(" + 
                 serverName + ',' + 
                 domainName + ')');
    checkStatus(Status.CONFIG);
    A3CMLPServer server = (A3CMLPServer) a3cmlConfig.getServer(serverName);
    server.removeNetwork(domainName);

    if (server.sid == AgentServer.getServerId()) {
      stopScript.addElement(new StopNetworkCmd(domainName));
    }
  }

  public void removeServer(String serverName) throws Exception {
    checkStatus(Status.CONFIG);
    try {
      A3CMLPServer server = (A3CMLPServer) a3cmlConfig.getServer(serverName);
      ServerDesc servDesc = AgentServer.getServerDesc(server.sid);
      if (servDesc.domain instanceof Network)
        ((Network) servDesc.domain).delServer(server.sid);
      AgentServer.removeServerDesc(server.sid);
      for (Enumeration e = AgentServer.elementsServerDesc(); 
           e.hasMoreElements(); ) {
        ServerDesc sd = (ServerDesc)e.nextElement();
        if (sd.gateway == server.sid) {
          sd.gateway = -1;
          sd.domain = null;
        }
      }
      a3cmlConfig.removeServer(server.sid);
    } catch (fr.dyade.aaa.agent.conf.UnknownServerException exc) {
      // The server has already been removed.
      // This may happen e.g. if the domain used 
      // to communicate with it has been deleted.
      // Idempotent: do nothing
    }
  }
  
  public void removeService(String serverName,
                            String serviceClassName) throws Exception {
    checkStatus(Status.CONFIG);
    A3CMLServer server = (A3CMLPServer) a3cmlConfig.getServer(serverName);
    server.removeService(serviceClassName);
  }
  
  private void stopNetwork(String domainName) throws Exception {
    AgentServer.removeConsumer(domainName);
  }

  private void stopService(String serviceClassName) throws Exception {
    ServiceManager.stop(serviceClassName);
  }

  private void startNetwork(String domainName) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "ConfigController.startNetwork(" + 
                 domainName + ')');
    A3CMLPServer a3cmlServer = 
      (A3CMLPServer) a3cmlConfig.getServer(AgentServer.getServerId());
    A3CMLNetwork a3cmlNetwork = a3cmlServer.getNetwork(domainName);
    if (a3cmlNetwork == null) throw new Exception(
      "Unknown network " + domainName);
    try {
      Network network =      
        (Network) AgentServer.getConsumer(domainName);
      network.start();
    } catch (Exception exc) {
      A3CMLDomain a3cmlDomain = 
        (A3CMLDomain) a3cmlConfig.getDomain(domainName);        
      Network network = 
        (Network) Class.forName(a3cmlDomain.network).newInstance();
      short[] domainSids = new short[a3cmlDomain.servers.size()];
      for (int i = 0; i < domainSids.length; i++) {
        domainSids[i] = 
          ((A3CMLServer) a3cmlDomain.servers.elementAt(i)).sid;
      }
      AgentServer.addConsumer(domainName, network);
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, 
                   " -> init network " + a3cmlDomain.name
                   + ',' + a3cmlNetwork.port);
      network.init(a3cmlDomain.name, 
                   a3cmlNetwork.port, 
                   domainSids);
      network.start();
    }
  }

  private void reconfigureServerNetwork(String domainName,
                                        int port) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "ConfigController.reconfigureServerNetwork(" + 
                 domainName + ',' + 
                 port + ')');
    Network network =
      (Network) AgentServer.getConsumer(domainName);
    network.stop();
    network.setPort(port);
    network.start();
  }

  private void reconfigureClientNetwork(short sid, 
                                        String domainName,
                                        int port) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "ConfigController.reconfigureClientNetwork(" + 
                 sid + ',' + 
                 domainName + ',' + 
                 port + ')');
    A3CMLPServer a3cmlServer = 
      (A3CMLPServer) a3cmlConfig.getServer(sid);
    ServerDesc serverDesc = 
      AgentServer.getServerDesc(sid);
    if (domainName.equals(serverDesc.getDomainName())) {
      serverDesc.updateSockAddr(
        serverDesc.getHostname(),
        port);
    }
  }

  private void startService(String serviceClassName,
                            String args) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "ConfigController.startService(" + 
                 serviceClassName + ',' + 
                 args + ')');
    A3CMLServer a3cmlServer = 
      a3cmlConfig.getServer(AgentServer.getServerId());
    ServiceManager.register(
     serviceClassName, args);
    ServiceDesc desc = 
      (ServiceDesc) ServiceManager.manager.registry.get(
        serviceClassName);
    if (desc.running) return;
    ServiceManager.start(desc);
  }

  private void startServer(short sid) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "ConfigController.startServer(" + sid + ')');
    A3CMLServer server = a3cmlConfig.getServer(sid);
    ServerDesc desc = AgentServer.getServerDesc(sid);
    AgentServer.initServerDesc(desc, (A3CMLPServer) server);
    if (desc.gateway == desc.sid) {
      if (desc.domain instanceof Network) {
        ((Network) desc.domain).addServer(server.sid);
      } else {
        throw new Error("Unknown gateway type: " + desc.domain);
      }
    }
  }

  static class StartServerCmd {
    public short sid;

    public StartServerCmd(short sid) {
      this.sid = sid;
    }
  }

  static class StartServiceCmd {
    public String serviceClassName;
    public String args;

    public StartServiceCmd(String serviceClassName, 
                           String args) {
      this.serviceClassName = serviceClassName;
      this.args = args;
    }
  }

  static class StartNetworkCmd {
    public String domainName;

    public StartNetworkCmd(String domainName) {
      this.domainName = domainName;
    }
  }

  static class StopNetworkCmd {
    public String domainName;
    
    public StopNetworkCmd(String domainName) {
      this.domainName = domainName;
    }
  }

  static class ReconfigureClientNetworkCmd {
    public short sid;
    public String domainName;
    public int port;
    
    public ReconfigureClientNetworkCmd(
      short sid,
      String domainName,
      int port) {
      this.sid = sid;
      this.domainName = domainName;
      this.port = port;
    }
  }

  static class ReconfigureServerNetworkCmd {
    public String domainName;
    public int port;
    
    public ReconfigureServerNetworkCmd(
      String domainName,
      int port) {
      this.domainName = domainName;
      this.port = port;
    }
  }
}
