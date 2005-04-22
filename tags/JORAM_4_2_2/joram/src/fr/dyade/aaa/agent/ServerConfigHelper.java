/*
 * Copyright (C) 2001 - 2004 ScalAgent Distributed Technologies
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
import java.io.*;

import fr.dyade.aaa.agent.conf.*;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

public class ServerConfigHelper {

  private static Logger logger = Debug.getLogger(
    "fr.dyade.aaa.agent.ServerConfigHelper");

  public static boolean addDomain(String domainName,
                                  int routerId,
                                  int port) 
    throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "ServerConfigHelper.addDomain(" + 
                 domainName +
                 ',' + routerId + ',' + 
                 port + ')');

    // Check configuration consistency (may fail)
    A3CMLConfig a3cmlConfig = AgentServer.getConfig();
    if (a3cmlConfig.domains.get(domainName) != null) 
      throw new NameAlreadyUsedException(
        "Domain name already used: " + domainName);
    if (a3cmlConfig.servers.get(new Short((short)routerId)) == null)
      throw new Exception("Server not found: " + routerId);
    
    // Update the configuration (can't fail)
    A3CMLDomain domain = new A3CMLDomain(
      domainName, 
      fr.dyade.aaa.agent.SimpleNetwork.class.getName());
    a3cmlConfig.addDomain(domain);
    A3CMLPServer a3cmlServer = (A3CMLPServer)a3cmlConfig.getServer((short)routerId);
    domain.addServer(a3cmlServer);
    A3CMLNetwork a3cmlNetwork = new A3CMLNetwork(domainName, port);
    a3cmlServer.addNetwork(a3cmlNetwork);

    A3CMLServer root = a3cmlConfig.getServer(
      AgentServer.getServerId());
    a3cmlConfig.configure((A3CMLPServer) root);

    boolean res = false;
    if (routerId == AgentServer.getServerId()) {
      // Create and start the run-time entities (may fail)
      Network network = 
        (Network) fr.dyade.aaa.agent.SimpleNetwork.class.newInstance();
      AgentServer.addConsumer(domainName, network);
      
      try {
        short[] sids = new short[1];
        sids[0] = (short)routerId;
        network.init(domainName, 
                     port, 
                     sids);
        network.start();
      } catch (Exception exc) {
        if (logger.isLoggable(BasicLevel.ERROR))
          logger.log(BasicLevel.ERROR, "", exc);
        // Rollback the network addition
        AgentServer.removeConsumer(domainName);

        a3cmlServer.removeNetwork(domainName);
        a3cmlConfig.removeDomain(domainName);
        a3cmlConfig.configure((A3CMLPServer) root);

        throw new StartFailureException(exc.getMessage());
      }
      
      res = true;
    }
    
    // commit changes
    commitChanges();

    return res;
  }

  public static boolean removeDomain(String domainName) 
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

    A3CMLPServer router = null;    
    if (domain.servers.size() == 1) {
      router = (A3CMLPServer)domain.servers.elementAt(0);
    }
    
    // Update the configuration and the run-time entities
    // at the same time (can't fail)
    a3cmlConfig.removeDomain(domainName);
    
    if (router != null) {
      router.removeNetwork(domainName);
    }

    A3CMLServer root = a3cmlConfig.getServer(AgentServer.getServerId());
    a3cmlConfig.configure((A3CMLPServer) root);

    boolean res = false;
    if (router != null && 
        router.sid == AgentServer.getServerId()) {
      
      // stop and delete the run-time entity
      AgentServer.removeConsumer(domainName);
      
      res = true;
    }
    
    // commit changes
    commitChanges();

    return res;
  }

  public static void addServer(int sid, 
                               String hostName, 
                               String domainName,
                               int port,
                               String name) 
    throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "ServerConfigHelper.addServer(" + 
                 sid + ',' + 
                 hostName + ',' + 
                 domainName + ',' + 
                 port + ',' + 
                 name + ')');

    A3CMLConfig a3cmlConfig = AgentServer.getConfig();
    if (a3cmlConfig.servers.get(new Integer(sid)) != null)
      throw new Exception("Server id already used: " + sid);

    A3CMLDomain domain = 
        (A3CMLDomain) a3cmlConfig.getDomain(domainName);

    A3CMLPServer server = new A3CMLPServer(
      (short)sid,
      name,
      hostName);
    a3cmlConfig.addServer(server);

    A3CMLNetwork network = new A3CMLNetwork(domainName, port);
    server.addNetwork(network);
    domain.addServer(server);
    server.domain = domainName;

    A3CMLService service = new A3CMLService(
      "org.objectweb.joram.mom.proxies.ConnectionManager",
      "root root");
    server.addService(service);

    A3CMLServer root = a3cmlConfig.getServer(AgentServer.getServerId());
    a3cmlConfig.configure((A3CMLPServer) root);
    
    ServerDesc serverDesc = new ServerDesc(
      (short)sid,
      name,
      hostName,
      -1);
    AgentServer.addServerDesc(serverDesc);
    AgentServer.initServerDesc(serverDesc, server);
    if (serverDesc.gateway == serverDesc.sid) {
      if (serverDesc.domain instanceof Network) {
        ((Network) serverDesc.domain).addServer((short)sid);
      } else {
        throw new Error("Unknown gateway type: " + 
                        serverDesc.domain);
      }
    }

    // commit changes
    commitChanges();
  }

  public static void removeServer(int sid) 
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
      a3cmlConfig.configure((A3CMLPServer) root);
      
      ServerDesc servDesc = 
        AgentServer.removeServerDesc((short)sid);
      
      if (servDesc.domain instanceof Network) {
        Network nw = (Network) servDesc.domain;
        nw.delServer(servDesc.sid);
      }
      
      for (Enumeration e = AgentServer.elementsServerDesc(); 
           e.hasMoreElements(); ) {
        ServerDesc sd = (ServerDesc)e.nextElement();
        if (sd.gateway == sid) {
          sd.gateway = -1;
          sd.domain = null;
        }
      }
      
      // commit changes
      commitChanges();
    }
    //else do nothing (don't remove the local server)
  }

  public static void commitChanges() throws Exception {
    A3CMLConfig a3cmlConfig = AgentServer.getConfig();
    if (AgentServer.getTransaction() instanceof 
        fr.dyade.aaa.util.NullTransaction) {
      String cfgDir = System.getProperty(AgentServer.CFG_DIR_PROPERTY, 
                                         AgentServer.DEFAULT_CFG_DIR);
      String cfgFile = System.getProperty(AgentServer.CFG_FILE_PROPERTY,
                                          AgentServer.DEFAULT_CFG_FILE);
      FileOutputStream fos = new FileOutputStream(
        new File(cfgDir, cfgFile));
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

  public static class NameAlreadyUsedException extends Exception {
    public NameAlreadyUsedException(String info) {
      super(info);
    }
  }

  public static class StartFailureException extends Exception {
    public StartFailureException(String info) {
      super(info);
    }
  }
}

    
    
