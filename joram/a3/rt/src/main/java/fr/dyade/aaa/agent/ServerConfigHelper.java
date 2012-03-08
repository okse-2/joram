/*
 * Copyright (C) 2001 - 2012 ScalAgent Distributed Technologies
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Enumeration;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.conf.A3CML;
import fr.dyade.aaa.agent.conf.A3CMLConfig;
import fr.dyade.aaa.agent.conf.A3CMLDomain;
import fr.dyade.aaa.agent.conf.A3CMLNetwork;
import fr.dyade.aaa.agent.conf.A3CMLServer;
import fr.dyade.aaa.agent.conf.A3CMLService;

public class ServerConfigHelper {
  
  private static Logger logger = Debug.getLogger(ServerConfigHelper.class.getName());

  private boolean autoCommit;

  public ServerConfigHelper(boolean autoCommit) {
    this.autoCommit = autoCommit;
  }

  public boolean addDomain(String domainName,
                           String network,
                           int routerId,
                           int port) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "ServerConfigHelper.addDomain(" + domainName + ',' + network + ',' + routerId + ',' + port + ')');

    // Check configuration consistency (may fail)
    A3CMLConfig a3cmlConfig = AgentServer.getConfig();
    
    if (a3cmlConfig.domains.get(domainName) != null) 
      throw new NameAlreadyUsedException("Domain name already used: " + domainName);
    
    if (a3cmlConfig.servers.get(new Short((short)routerId)) == null)
      throw new Exception("Server not found: " + routerId);
    
    // Update the configuration (can't fail)
    A3CMLDomain domain = new A3CMLDomain(domainName, network);
    a3cmlConfig.addDomain(domain);
    A3CMLServer a3cmlServer = a3cmlConfig.getServer((short) routerId);
    domain.addServer(a3cmlServer);
    A3CMLNetwork a3cmlNetwork = new A3CMLNetwork(domainName, port);
    a3cmlServer.addNetwork(a3cmlNetwork);

    A3CMLServer root = a3cmlConfig.getServer(AgentServer.getServerId());
    a3cmlConfig.configure(root);

    boolean res = false;
    if (routerId == AgentServer.getServerId()) {
      // Create and start the run-time entities (may fail)
      Network net = (Network) Class.forName(network).newInstance();
      
      // GS: Network name is set earlier than normal to have a well formed name
      // for the MBean in addConsumer method.
      net.name = AgentServer.getName() + '.' + domainName;
      AgentServer.addConsumer(domainName, net);
      
      try {
        short[] sids = new short[1];
        sids[0] = (short) routerId;
        net.init(domainName, port, sids);
        net.start();
      } catch (Exception exc) {
        if (logger.isLoggable(BasicLevel.ERROR))
          logger.log(BasicLevel.ERROR, "", exc);
        // Rollback the network addition
        AgentServer.removeConsumer(domainName);

        a3cmlServer.removeNetwork(domainName);
        a3cmlConfig.removeDomain(domainName);
        a3cmlConfig.configure(root);

        throw new StartFailureException(exc.getMessage());
      }
      
      res = true;
    }
    
    if (autoCommit) commit();

    return res;
  }

  public boolean removeDomain(String domainName) 
    throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "ServerConfigHelper.removeDomain(" + 
                 domainName + ')');

    A3CMLConfig a3cmlConfig = AgentServer.getConfig();
    A3CMLDomain domain = a3cmlConfig.getDomain(domainName);

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 " -> domain.servers = " + domain.servers);

    // Check that there is only one server left inside the domain:
    // the router. If there is more than one server, the domain
    // can't be removed. The servers belonging to the domain must
    // first be removed.
    if (domain.servers.size() > 1) 
      throw new Exception("Can't remove domain: it contains more than one server.");

    A3CMLServer router = null;    
    if (domain.servers.size() == 1) {
      router = (A3CMLServer) domain.servers.elementAt(0);
    }
    
    // Update the configuration and the run-time entities
    // at the same time (can't fail)
    a3cmlConfig.removeDomain(domainName);
    
    if (router != null) {
      router.removeNetwork(domainName);
    }

    A3CMLServer root = a3cmlConfig.getServer(AgentServer.getServerId());
    a3cmlConfig.configure(root);

    boolean res = false;
    if (router != null && 
        router.sid == AgentServer.getServerId()) {
      
      // stop and delete the run-time entity
      AgentServer.removeConsumer(domainName);
      
      res = true;
    }
    
    if (autoCommit) commit();

    return res;
  }

  public void addServer(int sid, 
                        String hostName, 
                        String domainName,
                        int port,
                        String name) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "ServerConfigHelper.addServer(" + sid + ',' + hostName + ',' + domainName + ',' + port + ',' + name + ')');
    
    // Adds the server in the ACML configuration graph
    
    A3CMLConfig a3cmlConfig = AgentServer.getConfig();
    if (a3cmlConfig.servers.get(new Integer(sid)) != null)
      throw new ServerIdAlreadyUsedException("Server id already used: " + sid);
    
    A3CMLDomain domain = a3cmlConfig.getDomain(domainName);
    
    A3CMLServer server = new A3CMLServer((short)sid, name, hostName);
    a3cmlConfig.addServer(server);

    A3CMLNetwork network = new A3CMLNetwork(domainName, port);
    server.addNetwork(network);
    domain.addServer(server);
    server.domain = domainName;

    A3CMLServer root = a3cmlConfig.getServer(AgentServer.getServerId());
    a3cmlConfig.configure(root);
    
    // Adds the server in the configuration structure
    
    ServerDesc desc = new ServerDesc((short)sid, name, hostName, -1);
    AgentServer.addServerDesc(desc);
    AgentServer.initServerDesc(desc, server);
    
    // TODO (AF): There is a problem with HttpNetwork.

//    if (desc.gateway == desc.sid) {
    if (server.hops == 1) {
      // The server is directly accessible, adds it to the corresponding Network component
      if (desc.getDomain() instanceof Network) {
        Network net = (Network) desc.getDomain();
        net.stop();
        net.addServer((short)sid);
        net.start();
      } else {
        throw new Error("Unknown gateway type: " + desc.getDomain());
      }
    } else {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "ServerConfigHelper.addServer -> desc = " + desc);
    }


    if (autoCommit) commit();
  }

  public void removeServer(int sid) 
    throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "ServerConfigHelper.removeServer(" + 
                 sid + ')');
    if (sid != AgentServer.getServerId()) {
      A3CMLConfig a3cmlConfig = AgentServer.getConfig();
      A3CMLServer a3cmlServer = a3cmlConfig.getServer((short)sid);
      
      if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 " -> server.networks = " + a3cmlServer.networks);

      // Check that the server doesn't belong to more than one
      // domain.
      if (a3cmlServer.networks.size() > 1)
        throw new Exception(
          "Can't remove server: it belongs to more than one domain.");

      a3cmlConfig.removeServer((short)sid);
      
      A3CMLServer root = a3cmlConfig.getServer(AgentServer.getServerId());
      a3cmlConfig.configure(root);
      
      ServerDesc servDesc = 
        AgentServer.removeServerDesc((short)sid);
      
      if (servDesc.getDomain() instanceof Network) {
        Network net = (Network) servDesc.getDomain();
        net.stop();
        net.delServer(servDesc.sid);
        net.start();
      }
      
      for (Enumeration<ServerDesc> e = AgentServer.elementsServerDesc(); e.hasMoreElements(); ) {
        ServerDesc sd = e.nextElement();
        if (sd.gateway == sid) {
          sd.gateway = -1;
          sd.setDomain(null);
        }
      }

      if (autoCommit) commit();
    }
    //else do nothing (don't remove the local server)
  }

  public void addService(int sid,
                         String className, 
                         String args) 
    throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "ServerConfigHelper.addService(" + sid + ',' + className + ',' + args + ')');
    A3CMLConfig a3cmlConfig = AgentServer.getConfig();
    A3CMLServer a3cmlServer = a3cmlConfig.getServer((short)sid);
    A3CMLService a3cmlService = new A3CMLService(className, args);
    a3cmlServer.addService(a3cmlService);
    
    if (sid == AgentServer.getServerId()) {
      try {
        ServiceManager.register(className, args);
        ServiceDesc desc = (ServiceDesc) ServiceManager.manager.registry.get(className);
        if (! desc.running) {
          ServiceManager.start(desc);
        }
      } catch (Exception exc) {
        a3cmlServer.removeService(className);
      }
    }

    if (autoCommit) commit();
  }

  public void removeService(int sid,
                            String className) 
    throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "ServerConfigHelper.addService(" + 
                 sid + ',' + className + ')');
    A3CMLConfig a3cmlConfig = AgentServer.getConfig();
    A3CMLServer a3cmlServer = a3cmlConfig.getServer((short)sid);
    
    if (sid == AgentServer.getServerId()) {
      ServiceManager.stop(className);
    }

    a3cmlServer.removeService(className);

    if (autoCommit) commit();
  }

  public void commit() throws Exception {
    A3CMLConfig a3cmlConfig = AgentServer.getConfig();
    if (AgentServer.getTransaction() instanceof fr.dyade.aaa.util.NullTransaction) {
      // TODO (AF): NullTransaction is not significant. 
      String cfgDir = System.getProperty(AgentServer.CFG_DIR_PROPERTY, AgentServer.DEFAULT_CFG_DIR);
      String cfgFile = System.getProperty(AgentServer.CFG_FILE_PROPERTY, AgentServer.DEFAULT_CFG_FILE);
      FileOutputStream fos = new FileOutputStream(new File(cfgDir, cfgFile));
      PrintWriter out = new PrintWriter(fos);
      A3CML.toXML(a3cmlConfig, out);
      out.flush();
      fos.flush();
      fos.getFD().sync();
      out.close();
      fos.close();
    } else {
      a3cmlConfig.save();
    }
  }

  public static class ServerIdAlreadyUsedException extends Exception {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public ServerIdAlreadyUsedException(String info) {
      super(info);
    }
  }
  
  public static class NameAlreadyUsedException extends Exception {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public NameAlreadyUsedException(String info) {
      super(info);
    }
  }

  public static class StartFailureException extends Exception {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public StartFailureException(String info) {
      super(info);
    }
  }
}

    
    
