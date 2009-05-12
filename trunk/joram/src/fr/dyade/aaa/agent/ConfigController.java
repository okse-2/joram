/*
 * Copyright (C) 2001 - 2009 ScalAgent Distributed Technologies
 * Copyright (C) 2004 France Telecom R&D
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.conf.A3CMLConfig;
import fr.dyade.aaa.agent.conf.A3CMLDomain;
import fr.dyade.aaa.agent.conf.A3CMLNat;
import fr.dyade.aaa.agent.conf.A3CMLNetwork;
import fr.dyade.aaa.agent.conf.A3CMLProperty;
import fr.dyade.aaa.agent.conf.A3CMLServer;
import fr.dyade.aaa.agent.conf.A3CMLService;
import fr.dyade.aaa.util.Transaction;

public class ConfigController {

  public final static String ADMIN_SERVER = "fr.dyade.aaa.agent.ADMIN_SERVER";

  public final static String SERVER_COUNTER = "serverCounter";

  private static Logger logger = Debug.getLogger(ConfigController.class.getName());

  public static class Status {
    public static final int FREE = 0;
    public static final int CONFIG = 1;

    public static String[] names = {"FREE", "CONFIG"};
    
    public static String toString(int status) {
      return names[status];
    }
  }

  private short serverCounter;

  private A3CMLConfig currentA3cmlConfig;

  private A3CMLConfig a3cmlConfig;

  private int status;

  private Vector newServers;

  private Vector stopScript;

  private Vector startScript;

  private Hashtable envProperties;

  ConfigController() throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "ConfigController.<init>()");
    // DF: must be improved. The admin server id can be higher than zero.
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 " -> AgentServer.getServerId() = " + AgentServer.getServerId());
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
    
    currentA3cmlConfig = AgentServer.getConfig();
    
    // Copy the configuration in order to enable rollback.
    // Use serialization. The rollback could also 
    // be done with an "undo script".
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(baos);
    oos.writeObject(currentA3cmlConfig);
    oos.flush();
    oos.close();
    byte[] bytes = baos.toByteArray();
    ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
    ObjectInputStream ois = new ObjectInputStream(bais);
    a3cmlConfig = (A3CMLConfig)ois.readObject();
    
    newServers = new Vector();
    startScript = new Vector();
    stopScript = new Vector();
    envProperties = new Hashtable();
    setStatus(Status.CONFIG);    
  }

  public synchronized void commitConfig() throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "ConfigController.commitConfig()");
    checkStatus(Status.CONFIG);

    AgentServer.setConfig(a3cmlConfig, true);

    try {
      A3CMLServer root = a3cmlConfig.getServer(AgentServer.getServerId());
      a3cmlConfig.configure(root);
      
      stop();
      
      addNewServers();

      addEnvProperties();

      start();

      commit();
    } catch (Exception exc) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR, "", exc); 
      rollback();
      throw exc;
    } finally {
      setStatus(Status.FREE);
      notify();
    }
  }

  private void addEnvProperties() {
    Enumeration keys = envProperties.keys();
    Enumeration values = envProperties.elements();
    while (keys.hasMoreElements()) {
      String name = (String)keys.nextElement();
      String value = (String)values.nextElement();
      System.setProperty(name, value);
    }
  }

  private void commit() {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "ConfigController.commit()");

    try {
      Transaction transaction = AgentServer.getTransaction();
      transaction.begin();
      a3cmlConfig.save();
      transaction.save(new Short(serverCounter), SERVER_COUNTER);      
      transaction.commit(true);
    } catch (Exception exc) {
      throw new Error(exc.toString());
    }
  }

  private void rollback() throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "ConfigController.rollback()");
    
    for (int i = 0; i < newServers.size(); i++) {
      ServerDesc sd = (ServerDesc)newServers.elementAt(i);
      AgentServer.removeServerDesc(sd.sid);
    }

    AgentServer.setConfig(currentA3cmlConfig, true);
  }

  public synchronized void release() {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "ConfigController.release()");
    
    if (status == Status.CONFIG) {
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
    int i = 0;
    try {
      for (i = 0; i < startScript.size(); i++) {
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
    } catch (Exception exc) {
      int size = i + 1;
      for (int j = 0; j < size; j++) {
        Object cmd = startScript.elementAt(j);
        if (cmd instanceof StartNetworkCmd) {
          rollback((StartNetworkCmd)cmd);
        } else if (cmd instanceof StartServiceCmd) {
          rollback((StartServiceCmd)cmd);
        } else if (cmd instanceof ReconfigureClientNetworkCmd) {
          rollback((ReconfigureClientNetworkCmd)cmd);
        } else if (cmd instanceof ReconfigureServerNetworkCmd) {
          rollback((ReconfigureServerNetworkCmd)cmd);
        }
      }
      throw exc;
    }
    
    // 'Start server' implies that the consumers    
    // have been added (done by 'start network').
    try {
      for (i = 0; i < startScript.size(); i++) {
        Object cmd = startScript.elementAt(i);
        if (cmd instanceof StartServerCmd) {
          exec((StartServerCmd)cmd);
        }
      }
    } catch (Exception exc) {
      int size = i + 1;
      for (int j = 0; j < size; j++) {
        Object cmd = startScript.elementAt(j);
        if (cmd instanceof StartServerCmd) {
          rollback((StartServerCmd)cmd);
        }
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

  private void rollback(StartServerCmd cmd) throws Exception {
    ServerDesc servDesc = AgentServer.getServerDesc(cmd.sid);
    deleteServer(servDesc);
  }

  private void rollback(StartNetworkCmd cmd) throws Exception {
    stopNetwork(cmd.domainName);
  }

  private void rollback(StartServiceCmd cmd) throws Exception {
    stopService(cmd.serviceClassName);
  }

  private void rollback(ReconfigureClientNetworkCmd cmd) throws Exception {
    // Do nothing
  }
  
  private void rollback(ReconfigureServerNetworkCmd cmd) throws Exception {
    // Do nothing
  }

  private synchronized void checkStatus(int expectedStatus) 
    throws Exception {
    if (status != expectedStatus) {
      throw new Exception("Illegal status: " + Status.toString(status)
          + " expected: " + Status.toString(expectedStatus));
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

  public int addServer(String name, String hostName, short id) 
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
      a3cmlConfig.addServer(new A3CMLServer(id, name, hostName));       
      ServerDesc serverDesc = new ServerDesc(id, name, hostName, -1);
      serverDesc.gateway = id;
      newServers.addElement(serverDesc);
    } else {
      // idempotent
    }
    
    if (id != AgentServer.getServerId()) {
      startScript.addElement(
        new StartServerCmd(id));
    }

    return id;
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
    short sid = a3cmlConfig.getServerIdByName(serverName);
    addNetwork(sid, domainName, port);
  }

  public void addNetwork(short serverId,
                         String domainName,
                         int port) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "ConfigController.addNetwork(" + 
                 serverId + ',' + 
                 domainName + ',' + 
                 port + ')');
    checkStatus(Status.CONFIG);
    A3CMLServer server = a3cmlConfig.getServer(serverId);
    A3CMLNetwork newNetwork = new A3CMLNetwork(domainName, port);    
    try {
      server.addNetwork(newNetwork);
      A3CMLDomain domain = a3cmlConfig.getDomain(domainName);
      domain.addServer(server);
    } catch (Exception exc) {
      // Idempotent
    }
    
    if (serverId == AgentServer.getServerId()) {
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
        new ReconfigureServerNetworkCmd(domainName, new Integer(port)));
    } else {
      startScript.add(
        new ReconfigureClientNetworkCmd(
          server.sid, domainName, new Integer(port)));
    }
  }
  
  public void setNetworkProperties(String serverName, String domainName, Integer port)
      throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "ConfigController.setNetworkPort("
          + serverName + ',' + domainName + ',' + port + ')');
    checkStatus(Status.CONFIG);
    
    A3CMLServer server = a3cmlConfig.getServer(serverName);
    A3CMLNetwork network = server.getNetwork(domainName);
    if (port != null) {
      if (network != null) {
        network.port = port.intValue();
      } else {
        throw new Exception("Unknown network");
      }
    }
    // else the port is not reconfigured
    
    if (server.sid == AgentServer.getServerId()) {
      startScript.add(new ReconfigureServerNetworkCmd(domainName, port));
    } else {
      startScript.add(new ReconfigureClientNetworkCmd(server.sid, domainName, port));
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
    envProperties.put(propName, value);
  }

  public void setServerProperty(String serverName,
                                String propName,
                                String value) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "ConfigController.setServerProperty(" + 
                 serverName + ',' + 
                 propName + ',' + 
                 value + ')');
    checkStatus(Status.CONFIG);
    A3CMLServer server = a3cmlConfig.getServer(serverName);
    server.addProperty(new A3CMLProperty(propName,
                                         value));
    envProperties.put(propName, value);
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
    try {
      Vector serversToRemove = new Vector();
      A3CMLDomain domain = a3cmlConfig.getDomain(domainName);
      for (int i = 0; i < domain.servers.size(); i++) {
        A3CMLServer server = (A3CMLServer) domain.servers.elementAt(i);
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
    } catch (fr.dyade.aaa.agent.conf.UnknownDomainException exc) {
      // Do nothing (idempotency)
    }
  }

  public void removeNetwork(String serverName, 
                            String domainName) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "ConfigController.removeNetwork(" + 
                 serverName + ',' + 
                 domainName + ')');
    checkStatus(Status.CONFIG);
    A3CMLServer server = a3cmlConfig.getServer(serverName);
    server.removeNetwork(domainName);

    if (server.sid == AgentServer.getServerId()) {
      stopScript.addElement(new StopNetworkCmd(domainName));
    }
  }

  public void removeServer(String serverName) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "ConfigController.removeServer(" + 
                 serverName + ')');
    checkStatus(Status.CONFIG);
    try {
      A3CMLServer server = a3cmlConfig.getServer(serverName);
      ServerDesc servDesc = AgentServer.getServerDesc(server.sid);
      removeServer(servDesc);
    } catch (fr.dyade.aaa.agent.conf.UnknownServerException exc) {
      // The server has already been removed.
      // This may happen e.g. if the domain used 
      // to communicate with it has been deleted.
      // Idempotent: do nothing
    }
  }

  public void removeServer(short serverId) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "ConfigController.removeServer(" + 
                 serverId + ')'); 
    try {
      checkStatus(Status.CONFIG);
      ServerDesc servDesc = AgentServer.getServerDesc(serverId);
      removeServer(servDesc);
    } catch (fr.dyade.aaa.agent.conf.UnknownServerException exc) {
      // The server has already been removed.
      // This may happen e.g. if the domain used 
      // to communicate with it has been deleted.
      // Idempotent: do nothing
    }
  }

  private void removeServer(ServerDesc servDesc) throws Exception {
    if (servDesc.sid != AgentServer.getServerId()) {
      deleteServer(servDesc);
      a3cmlConfig.removeServer(servDesc.sid);
    } else {
      // else don't remove the local server
      throw new Exception("Can't remove local server");
    }
  }

  private void deleteServer(ServerDesc servDesc) throws Exception {
    if (servDesc.domain instanceof Network)
      ((Network) servDesc.domain).delServer(servDesc.sid);
    AgentServer.removeServerDesc(servDesc.sid);
    for (Enumeration e = AgentServer.elementsServerDesc(); 
         e.hasMoreElements(); ) {
      ServerDesc sd = (ServerDesc)e.nextElement();
      if (sd.gateway == servDesc.sid) {
        sd.gateway = -1;
        sd.domain = null;
      }
    }
  }
  
  public void removeService(String serverName,
                            String serviceClassName) throws Exception {
    checkStatus(Status.CONFIG);
    A3CMLServer server = a3cmlConfig.getServer(serverName);
    server.removeService(serviceClassName);
  }
  
  private void stopNetwork(String domainName) throws Exception {
    // In order to ensure idempotency we must check
    // that the consumer is still there.
    if (AgentServer.getConsumer(domainName) != null) {
      AgentServer.removeConsumer(domainName);
    }
  }

  private void stopService(String serviceClassName) throws Exception {
    ServiceManager.stop(serviceClassName);
  }

  private void startNetwork(String domainName) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "ConfigController.startNetwork(" + 
                 domainName + ')');
    A3CMLServer a3cmlServer = a3cmlConfig.getServer(AgentServer.getServerId());
    A3CMLNetwork a3cmlNetwork = a3cmlServer.getNetwork(domainName);
    if (a3cmlNetwork == null) throw new Exception(
      "Unknown network " + domainName);
    Network network;
    try {
      network = (Network) AgentServer.getConsumer(domainName);
    } catch (Exception exc) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "", exc);
      A3CMLDomain a3cmlDomain = a3cmlConfig.getDomain(domainName);        
      network = (Network) Class.forName(a3cmlDomain.network).newInstance();
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
    }
    network.start();
  }

  private void reconfigureServerNetwork(String domainName,
      Integer port) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "ConfigController.reconfigureServerNetwork(" + 
                 domainName + ',' + 
                 port + ')');
    Network network =
      (Network) AgentServer.getConsumer(domainName);
    network.stop();
    if (port != null) {
      network.setPort(port.intValue());
    }
    network.setProperties();
    network.start();
  }

  private void reconfigureClientNetwork(short sid, 
                                        String domainName,
                                        Integer port) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "ConfigController.reconfigureClientNetwork(" + 
                 sid + ',' + 
                 domainName + ',' + 
                 port + ')');
    ServerDesc serverDesc = AgentServer.getServerDesc(sid);
    if (port != null && domainName.equals(serverDesc.getDomainName())) {
      serverDesc.updateSockAddr(
        serverDesc.getHostname(),
        port.intValue());
    }
    
    // Reset the properties values
    Network network;
    try {
      network = (Network) AgentServer.getConsumer(domainName);
    } catch (Exception exc) {
      // Network is not found. Means that the server
      // is not connected to this domain.
      network = null;
    }
    if (network != null) {
      network.setProperties();
    }
  }

  private void startService(String serviceClassName,
                            String args) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "ConfigController.startService(" + 
                 serviceClassName + ',' + 
                 args + ')');
    ServiceManager.register(serviceClassName, args);
    ServiceDesc desc = (ServiceDesc) ServiceManager.manager.registry.get(serviceClassName);
    if (desc.running) return;
    ServiceManager.start(desc);
  }

  private void startServer(short sid) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "ConfigController.startServer(" + sid + ')');
    A3CMLServer server = a3cmlConfig.getServer(sid);
    ServerDesc desc = AgentServer.getServerDesc(sid);
    AgentServer.initServerDesc(desc, server);
    
    // TODO (AF): There is a problem with HttpNetwork.
    
//    if (desc.gateway == desc.sid) {
    if (server.hops == 1) {
      if (desc.domain instanceof Network) {
        ((Network) desc.domain).addServer(server.sid);
      } else {
        throw new Error("Unknown gateway type: " + desc.domain);
      }
    } else {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "ConfigController.startServer -> desc = " + desc);
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
    public Integer port;
    
    public ReconfigureClientNetworkCmd(
      short sid,
      String domainName,
      Integer port) {
      this.sid = sid;
      this.domainName = domainName;
      this.port = port;
    }
  }

  static class ReconfigureServerNetworkCmd {
    public String domainName;
    public Integer port;
    
    public ReconfigureServerNetworkCmd(
      String domainName,
      Integer port) {
      this.domainName = domainName;
      this.port = port;
    }
  }
}
