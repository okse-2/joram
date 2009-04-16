/*
 * Copyright (C) 2002 - 2008 ScalAgent Distributed Technologies
 * Copyright (C) 2004 - France Telecom R&D
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
 */
package fr.dyade.aaa.agent;

import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.admin.cmd.AdminCmd;
import fr.dyade.aaa.admin.cmd.DomainCmdException;
import fr.dyade.aaa.admin.cmd.ExceptionCmd;
import fr.dyade.aaa.admin.cmd.JvmArgsCmdException;
import fr.dyade.aaa.admin.cmd.NatCmdException;
import fr.dyade.aaa.admin.cmd.NetworkCmdException;
import fr.dyade.aaa.admin.cmd.NewDomainCmd;
import fr.dyade.aaa.admin.cmd.NewNetworkCmd;
import fr.dyade.aaa.admin.cmd.NewServerCmd;
import fr.dyade.aaa.admin.cmd.NewServiceCmd;
import fr.dyade.aaa.admin.cmd.PropertyCmdException;
import fr.dyade.aaa.admin.cmd.RemoveDomainCmd;
import fr.dyade.aaa.admin.cmd.RemoveNetworkCmd;
import fr.dyade.aaa.admin.cmd.RemoveServerCmd;
import fr.dyade.aaa.admin.cmd.RemoveServiceCmd;
import fr.dyade.aaa.admin.cmd.ServerCmdException;
import fr.dyade.aaa.admin.cmd.ServiceCmdException;
import fr.dyade.aaa.admin.cmd.SetJvmArgsCmd;
import fr.dyade.aaa.admin.cmd.SetNetworkPortCmd;
import fr.dyade.aaa.admin.cmd.SetPropertyCmd;
import fr.dyade.aaa.admin.cmd.SetServerNatCmd;
import fr.dyade.aaa.admin.cmd.SetServerPropertyCmd;
import fr.dyade.aaa.admin.cmd.StartAdminCmd;
import fr.dyade.aaa.admin.cmd.StartNetworkCmd;
import fr.dyade.aaa.admin.cmd.StartServerCmd;
import fr.dyade.aaa.admin.cmd.StartServiceCmd;
import fr.dyade.aaa.admin.cmd.StopAdminCmd;
import fr.dyade.aaa.admin.cmd.StopNetworkCmd;
import fr.dyade.aaa.admin.cmd.StopServiceCmd;
import fr.dyade.aaa.admin.cmd.UnsetJvmArgsCmd;
import fr.dyade.aaa.admin.cmd.UnsetPropertyCmd;
import fr.dyade.aaa.admin.cmd.UnsetServerNatCmd;
import fr.dyade.aaa.admin.cmd.UnsetServerPropertyCmd;
import fr.dyade.aaa.admin.script.Script;
import fr.dyade.aaa.admin.script.StartScript;
import fr.dyade.aaa.admin.script.StopScript;
import fr.dyade.aaa.agent.conf.A3CMLConfig;
import fr.dyade.aaa.agent.conf.A3CMLDomain;
import fr.dyade.aaa.agent.conf.A3CMLNat;
import fr.dyade.aaa.agent.conf.A3CMLNetwork;
import fr.dyade.aaa.agent.conf.A3CMLProperty;
import fr.dyade.aaa.agent.conf.A3CMLServer;
import fr.dyade.aaa.agent.conf.A3CMLService;


/**
 * The <code>AgentAdmin</code> allows the administration of the AgentServer
 * through scripts.
 */
final public class AgentAdmin extends Agent {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  static Logger logmon;

  // current operation
  public static final int NONE = 0;
  public static final int CONFIGURED = 1;
  public static final int STOPED = 2;
  public static final int STARTED = 3;
  static String[] statusName = {"NONE", "CONFIGURED", "STOPED", "STARTED"};

  /** silence use for idempotence */
  private boolean silence = false;
  /** configuration */
  private transient A3CMLConfig a3cmlConfig = null;
  /** start script */
  private StartScript startScript = null;
  /** stop script */
  private StopScript stopScript = null;
  /** script use to rollback a configuration */
  private Script rollback = null;
  /** server id counter */
  private short maxId = 0;

  /**
   * Get default AgentId of AgentAdmin for specified AgentServer.
   * 
   * @param serverId the id of specified AgentServer.
   * @return the default AgentId of AgentAdmin for specified AgentServer.
   */
  public static AgentId getDefault(short serverId) {
    return new AgentId(serverId, serverId, AgentId.AdminIdStamp);
  }

  /**
   * Get default AgentId of AgentAdmin for local AgentServer.
   * 
   * @return the default AgentId of AgentAdmin for local AgentServer.
   */
  public static AgentId getDefault() {
    return getDefault(AgentServer.getServerId());
  }

  /**
   * Initializes the package as a well known service.
   * <p>
   * Creates a <code>AgentAdmin</code> agent with the well known stamp
   * <code>AgentId.AdminIdStamp</code>.
   *
   * @param args        parameters from the configuration file
   * @param firstTime   <code>true</code> when agent server starts anew
   *
   * @exception Exception
   *    unspecialized exception
   */
  public static void init(String args, boolean firstTime) throws Exception {
    // Get the logging monitor from current server MonologMonitorFactory
    logmon = Debug.getLogger("fr.dyade.aaa.agent.Admin");
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,
                 "AgentAdmin.init(" + args + ", " + firstTime + ")");
    
    if (! firstTime) return;
    
    short maxId;
    if (args == null) {
      maxId = -1;
    } else {
      try {
        maxId = Short.parseShort(args);
      } catch (Exception exc) {
        if (logmon.isLoggable(BasicLevel.WARN))
          logmon.log(BasicLevel.WARN, "", exc);
        maxId = -1;
      }
    }
    AgentAdmin agentAdmin = new AgentAdmin(maxId);
    agentAdmin.deploy();
  }

  public static void stopService() {
    // Does nothing
  }
  
  protected void agentInitialize(boolean firstTime) throws Exception {
    super.agentInitialize(firstTime);
  }

  /**
   * Creates a local administration agent.
   */
  public AgentAdmin(short maxId) {
    super("AgentAdmin#" + AgentServer.getServerId(),
          true, AgentId.adminId);
    this.maxId = maxId;
  }

  /**
   * Reacts to <code>AgentAdmin</code> specific notifications.
   * Analyzes the notification request code, then do the appropriate
   * work. By default calls <code>react</code> from base class.
   * Handled notification types are :
   *    <code>AdminRequest</code>,
   *    <code>AdminStartStopNot</code>
   *
   * @param from        agent sending notification
   * @param not         notification to react to
   *
   * @exception Exception
   *    unspecialized exception
   */
  public void react(AgentId from, Notification not) throws Exception {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,
                 "\n\n\nAgentAdmin.react(" + from + "," + not + ")");    
    if (not instanceof AdminRequestNot) {
      doReact(from, (AdminRequestNot) not);
    } else if (not instanceof AdminStartStopNot) {
      doReact(from, (AdminStartStopNot) not);
    } else { 
      super.react(from, not);
    }
  }

 /**
   * doReact to <code>AdminRequestNot</code> notifications.
   *
   * @param from        agent sending AdminRequestNot
   * @param not         AdminRequestNot notification
   *
   * @exception Exception
   */
  private void doReact(AgentId from, AdminRequestNot not) throws Exception {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "AgentAdmin.doReact(AdminRequestNot)");
    
    startScript = new StartScript();
    stopScript = new StopScript();
    rollback = new Script();
    silence = not.silence;

    try {
      // configuration phase
      doReact(not.script);      
    } catch (Exception exc) {
      logmon.log(BasicLevel.WARN, "AgentAdmin.react", exc);
      if (exc instanceof ExceptionCmd) {
        // send exception to agent sending AdminRequestNot.
        AdminReplyNot reply = new AdminReplyNot((ExceptionCmd) exc);
        reply.setContext(not.getContext());
        reply.status = NONE;
        sendTo(from, reply);
        // remove all i do (rollback)
        doReact(rollback);
        startScript = null;
        stopScript = null;
        rollback = null;
      } else
        throw exc;
    }

    if (not.autoStart) {
      // start/stop configuration in same reaction
      AdminStartStopNot startstop = new AdminStartStopNot();
      startstop.setContext(not.getContext());
      startstop.startScript = startScript;
      startstop.stopScript = stopScript;
      doReact(from, startstop);
    } else {
      // reply to agent sending AdminRequestNot.
      // set in AdminReplyNot startScript and stopScript.
      // use to start this configuration.
      AdminReplyNot reply = new AdminReplyNot();
      reply.setContext(not.getContext());
      reply.startScript = startScript;
      reply.stopScript = stopScript;
      reply.status = CONFIGURED;
      sendTo(from, reply);
    }
  }

 /**
   * doReact to <code>AdminStartStopNot</code> notifications.
   *
   * @param from        agent sending AdminStartStopNot
   * @param not         AdminStartStopNot notification
   *
   * @exception Exception
   */
  private void doReact(AgentId from, AdminStartStopNot not) throws Exception {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,
                 "AgentAdmin.doReact(AdminStartStopNot)\n startScript = " + startScript);

    AdminAckStartStopNot ack = new AdminAckStartStopNot();
    ack.setContext(not.getContext());
    ack.status = CONFIGURED;
    try {
      startScript = not.startScript;
      stopScript = not.stopScript;
      // first execute stop script
      if (stopScript != null) {
        stop();
        ack.status = STOPED;
      }
      // second execute start script
      if (startScript != null) {
        start();
        ack.status = STARTED;
      }
      // startScript should be null
      ack.startScript = startScript;
      // stopScript contains the complementary 
      // of initial startScript.
      ack.stopScript = stopScript;
      sendTo(from, ack);
    } catch (Exception exc) {
      logmon.log(BasicLevel.WARN,
                 "AgentAdmin.doReact(AdminStartStopNot) Exception : " + exc);
      if (exc instanceof ExceptionCmd) {
        ack.exc = (ExceptionCmd) exc;
        // startScript should not be empty
        // depend where exception is catch
        ack.startScript = startScript;
        ack.stopScript = stopScript;
        sendTo(from, ack);
      } else
        throw exc;
    }
  }

  /** execute start script */
  private void start() throws Exception {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,
                 "AgentAdmin.start()\nstartScript = " + startScript);
    if (startScript == null) return;

    Vector toRemove = new Vector();
    try {
      // add serverDesc
      for (Enumeration e = startScript.serverDesc.elements(); e.hasMoreElements();)
        AgentServer.addServerDesc((ServerDesc) e.nextElement());

      // start network, server and service.
      for (Enumeration e = startScript.elements(); e.hasMoreElements();) {
        StartAdminCmd cmd = (StartAdminCmd) e.nextElement();
//         if (cmd instanceof UpdateNetworkPortCmd) {
//           doReact((UpdateNetworkPortCmd) cmd);
//         } else 
        if (cmd instanceof StartNetworkCmd) {
          doReact((StartNetworkCmd) cmd);
        } else if (cmd instanceof StartServiceCmd) {
          doReact((StartServiceCmd) cmd);
        } else if (cmd instanceof StartServerCmd) {
          doReact((StartServerCmd) cmd);
        }
        // add command done.
        toRemove.addElement(cmd);
      }
      
      // remove from startScript all done command.
      for (Enumeration e = toRemove.elements(); e.hasMoreElements();) {
        StartAdminCmd cmd = (StartAdminCmd) e.nextElement();
        startScript.remove(cmd);
      }
      if (startScript.size() == 0) 
        startScript = null;
    } catch (Exception exc) {
      // remove from startScript all done command.
      for (Enumeration e = toRemove.elements(); e.hasMoreElements();) {
        StartAdminCmd cmd = (StartAdminCmd) e.nextElement();
        startScript.remove(cmd);
      }
      if (startScript.size() == 0) 
        startScript = null;
      throw exc;
    }
  }

   /** execute stop script */
  private void stop() throws Exception {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,
                 "AgentAdmin.stop()\nstopScript = " + stopScript);
    if (stopScript == null) return;

    Vector toRemove = new Vector();
    try {
      for (Enumeration e = stopScript.elements(); e.hasMoreElements();) {
        StopAdminCmd cmd = (StopAdminCmd) e.nextElement();
        if (cmd instanceof StopNetworkCmd) {
          doReact((StopNetworkCmd) cmd);
        } else if (cmd instanceof StopServiceCmd) {
          doReact((StopServiceCmd) cmd);
        }
        // add command done.
        toRemove.addElement(cmd);
      }

      // remove from stopScript all done command.
      for (Enumeration e = toRemove.elements(); e.hasMoreElements();) {
        StopAdminCmd cmd = (StopAdminCmd) e.nextElement();
        stopScript.remove(cmd);
      }
      if (stopScript.size() == 0) 
        stopScript = null;
    } catch (Exception exc) {
      // remove from stopScript all done command.
      for (Enumeration e = toRemove.elements(); e.hasMoreElements();) {
        StartAdminCmd cmd = (StartAdminCmd) e.nextElement();
        startScript.remove(cmd);
      }
      if (startScript.size() == 0) 
        startScript = null;
      throw exc;
    }
  }

  /** 
   * execute configuration script 
   *
   * @param script Script
   *
   * @exception Exception
   */
  private void doReact(Script script) throws Exception {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,
                 "AgentAdmin.doReact(" + script + ")");

    if (script.newConfig) {
      a3cmlConfig = new A3CMLConfig();
    } else {
      // keep AgentServer configuration
      a3cmlConfig = AgentServer.getConfig();
    }

    if (a3cmlConfig == null) {
      throw new ExceptionCmd("a3cmlConfig is null");
    }

    for (Enumeration e = script.elements(); e.hasMoreElements();) {
      AdminCmd cmd = (AdminCmd) e.nextElement();
      if (cmd instanceof NewDomainCmd) {
        doReact((NewDomainCmd) cmd);
      } else if (cmd instanceof NewServerCmd) {
        doReact((NewServerCmd) cmd);
      } else if (cmd instanceof NewServiceCmd) {
        doReact((NewServiceCmd) cmd);
      } else if (cmd instanceof NewNetworkCmd) {
        doReact((NewNetworkCmd) cmd);
      } else if (cmd instanceof SetJvmArgsCmd) {
        doReact((SetJvmArgsCmd) cmd);
      } else if (cmd instanceof SetServerPropertyCmd) {
        doReact((SetServerPropertyCmd) cmd);
      } else if (cmd instanceof SetPropertyCmd) {
        doReact((SetPropertyCmd) cmd);
      } else if (cmd instanceof SetServerNatCmd) {
        doReact((SetServerNatCmd) cmd);
      }  else if (cmd instanceof RemoveDomainCmd) {
        doReact((RemoveDomainCmd) cmd);
      }  else if (cmd instanceof RemoveNetworkCmd) {
        doReact((RemoveNetworkCmd) cmd);
      }  else if (cmd instanceof RemoveServerCmd) {
        doReact((RemoveServerCmd) cmd);
      }  else if (cmd instanceof RemoveServiceCmd) {
        doReact((RemoveServiceCmd) cmd);
      }  else if (cmd instanceof UnsetServerPropertyCmd) {
        doReact((UnsetServerPropertyCmd) cmd);
      }  else if (cmd instanceof UnsetPropertyCmd) {
        doReact((UnsetPropertyCmd) cmd);
      }  else if (cmd instanceof UnsetJvmArgsCmd) {
        doReact((UnsetJvmArgsCmd) cmd);
      } else if (cmd instanceof UnsetServerNatCmd) {
        doReact((UnsetServerNatCmd) cmd);
      } else if (cmd instanceof SetNetworkPortCmd) {
        doReact((SetNetworkPortCmd) cmd);
      } 
    }

    A3CMLServer root = a3cmlConfig.getServer(AgentServer.getServerId());
    a3cmlConfig.configure(root);

    if (script.newConfig)
      AgentServer.setConfig(a3cmlConfig);

    // save configuration (transaction)
    a3cmlConfig.save();
  }

  /** 
   * create new domain 
   *
   * @param cmd NewDomainCmd
   *
   * @exception DomainCmdException
   */
  private void doReact(NewDomainCmd cmd) throws DomainCmdException {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,
                 "AgentAdmin.doReact(" + cmd + ")");
    
    try {
      if (!a3cmlConfig.containsDomain(cmd.name)) {
        a3cmlConfig.addDomain(
          new A3CMLDomain(cmd.name,
                          cmd.networkClass));
        rollback.add(new RemoveDomainCmd(cmd.name,cmd.networkClass));
      } else {
        if (silence) return;
        throw new DomainCmdException("Domain " + cmd.name + " already exist.");
      }
    } catch (Exception exc) {
      if (logmon.isLoggable(BasicLevel.ERROR))
        logmon.log(BasicLevel.ERROR, "", exc);
      throw new DomainCmdException(exc);
    }
  }

  /** 
   * create new server 
   *
   * @param cmd NewServerCmd
   *
   * @exception ServerCmdException
   */
  private void doReact(NewServerCmd cmd) throws ServerCmdException {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,
                 "AgentAdmin.doReact(" + cmd + ")");
    
    try {
      if (!a3cmlConfig.containsServer(cmd.name)) {
        // set id for new server
        Short id = null;
        if (cmd.sid == null) {
          id = getSid();
          if (id == null) 
            throw new ServerCmdException(
              "AgentAdmin : NewServerCmd(" + cmd + 
              ")not authorized on " + 
              AgentServer.getServerId());
        } else
          id = cmd.sid;
        
        a3cmlConfig.addServer(
          new A3CMLServer(id.shortValue(),
                           cmd.name,
                           cmd.hostname));
        rollback.add(new RemoveServerCmd(cmd.name,cmd.hostname));

        // prepare serverDesc and add to startScript
        ServerDesc sd = new ServerDesc(id.shortValue(),
                                       cmd.name,
                                       cmd.hostname,
                                       -1);
//         sd.isTransient = false;
        sd.gateway = id.shortValue();
        startScript.serverDesc.put(id, sd);
        startScript.add(new StartServerCmd(id.shortValue(),cmd.name));
        if (logmon.isLoggable(BasicLevel.DEBUG))
          logmon.log(BasicLevel.DEBUG, 
                     "AgentAdmin startScript.serverDesc.put(" + id + "," + sd + ")");
      } else {
        if (silence) {
          short sid;
          if (cmd.sid == null) {
            sid = a3cmlConfig.getServerIdByName(cmd.name);
          } else {
            sid = cmd.sid.shortValue();
          }
          if (sid != AgentServer.getServerId()) {
            startScript.add(new StartServerCmd(
              sid,
              cmd.name));
          }
        } else {
          throw new ServerCmdException(
            "Server " + cmd.name + " already exists");
        }
      }
    } catch (Exception exc) {
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG,
                   "AgentAdmin.doReact(NewServerCmd)",exc);
      throw new ServerCmdException(exc);
    }
  }
  
  /** 
   * create new service
   *
   * @param cmd NewServiceCmd
   *
   * @exception ServiceCmdException
   */
  private void doReact(NewServiceCmd cmd) throws ServiceCmdException {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,
                 "AgentAdmin.doReact(" + cmd + ")");
    
    try {
      A3CMLServer server = a3cmlConfig.getServer(cmd.serverName);
      A3CMLService newService = new A3CMLService(cmd.className,cmd.args);
      if (!server.services.contains(newService)) {
        server.services.addElement(newService);
        rollback.add(new RemoveServiceCmd(cmd.serverName,
                                          cmd.className,
                                          cmd.args));
        
        A3CMLServer adm = a3cmlConfig.getServer(AgentServer.getServerId());
        if (cmd.serverName.equals(adm.name)) {
          if (logmon.isLoggable(BasicLevel.DEBUG))
            logmon.log(BasicLevel.DEBUG,
                       "NewServiceCmd startScript.add(" + cmd.className + ")");
          startScript.add(new StartServiceCmd(cmd.className,cmd.args));
        }
      } else {
        if (silence) {
          startScript.add(new StartServiceCmd(cmd.className,cmd.args));
          return;
        }
        throw new ServiceCmdException("Service " + newService + " already exist in server " + server);
      }
    } catch (Exception exc) {
      if (logmon.isLoggable(BasicLevel.ERROR))
        logmon.log(BasicLevel.ERROR, "", exc);
      throw new ServiceCmdException(exc);
    }
  }
  
  /** 
   * create new network
   *
   * @param cmd NewNetworkCmd
   *
   * @exception NetworkCmdException
   */
  private void doReact(NewNetworkCmd cmd) throws NetworkCmdException {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,
                 "AgentAdmin.doReact(" + cmd + ")");

    try {
      A3CMLServer server = a3cmlConfig.getServer(cmd.serverName);
      A3CMLNetwork toadd = new A3CMLNetwork(cmd.domain,cmd.port);
      if (server.networks.contains(toadd)) {
        if (silence) {
          if (server.sid == AgentServer.getServerId()) {
            startScript.add(new StartNetworkCmd(server.sid,
                                                cmd.domain));
          }
          return;
        }
        throw new NetworkCmdException(cmd.serverName + " already contains network " + toadd);
      }
      server.networks.addElement(toadd);
      rollback.add(new RemoveNetworkCmd(cmd.serverName,
                                        cmd.domain));
      
      if (!cmd.domain.equals("transient")) {
        // add server in domains
        A3CMLDomain domain = a3cmlConfig.getDomain(cmd.domain);
        domain.addServer(server);
        
        if (server.sid == AgentServer.getServerId()) {
          if (logmon.isLoggable(BasicLevel.DEBUG))
            logmon.log(BasicLevel.DEBUG,
                       "startScript.add(" + server.sid + ")");
          startScript.add(new StartNetworkCmd(server.sid,
                                              cmd.domain));
        }

        // prepare serverDesc and add to startScript
        ServerDesc serverDesc = 
          (ServerDesc) startScript.serverDesc.get(new Short(server.sid));
        if (serverDesc == null)
          serverDesc = AgentServer.getServerDesc(server.sid);
//         if (serverDesc != null) {
//           serverDesc.port = cmd.port;
//         }
      }
    } catch (Exception exc) {
      if (logmon.isLoggable(BasicLevel.ERROR))
        logmon.log(BasicLevel.ERROR, "", exc);
      throw new NetworkCmdException(exc);
    }
  }

  /** 
   * Set the port of a network
   *
   * @param cmd SetNetworkPortCmd
   *
   * @exception NetworkCmdException
   */
  private void doReact(SetNetworkPortCmd cmd) throws NetworkCmdException {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,
                 "AgentAdmin.doReact(" + cmd + ")");
    try {
      A3CMLServer server = a3cmlConfig.getServer(cmd.serverName);
      A3CMLNetwork network = null;
      for (int i = 0; i < server.networks.size(); i++) {
        A3CMLNetwork nw = 
          (A3CMLNetwork)server.networks.elementAt(i);
        if (nw.domain.equals(cmd.domain)) {
          network = nw;
        }
      }
      if (network != null) {
        // DF: the rollback script doesn't
        //     work properly:
        // 1- a 'remove' command is not rollbacked
        //    because it is considered as a rollback action.
        // 2- a 'set' command is its own reverse command.
        //    So a rollback loops indefinitely.
        // rollback.add(
//           new SetNetworkPortCmd(
//             cmd.serverName, cmd.domain, network.port));
        network.port = cmd.port;
        startScript.add(
          new StartNetworkCmd(
            server.sid,
            cmd.domain));
      } else {
        throw new Exception("Unknown network");
      }
    } catch (Exception exc) {
      if (logmon.isLoggable(BasicLevel.ERROR))
        logmon.log(BasicLevel.ERROR, "", exc);
      throw new NetworkCmdException(exc);
    }    
  }

  /** 
   * set jvm arguments
   *
   * @param cmd SetJvmArgsCmd
   *
   * @exception JvmArgsCmdException
   */
  private void doReact(SetJvmArgsCmd cmd) throws JvmArgsCmdException {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,
                 "AgentAdmin.doReact(" + cmd + ")");

    try {
      A3CMLServer server = a3cmlConfig.getServer(cmd.serverName);
      server.jvmArgs = cmd.args;
      rollback.add(new UnsetJvmArgsCmd(cmd.serverName,cmd.args));
    } catch (Exception exc) {
      if (logmon.isLoggable(BasicLevel.ERROR))
        logmon.log(BasicLevel.ERROR, "", exc);
      throw new JvmArgsCmdException(exc);
    }
  }
  
  /** 
   * set property
   *
   * @param cmd SetPropertyCmd
   *
   * @exception PropertyCmdException
   */
  private void doReact(SetPropertyCmd cmd) throws PropertyCmdException {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,
                 "AgentAdmin.doReact(" + cmd + ")");

    try {
      if (!a3cmlConfig.containsProperty(cmd.name)) {
        a3cmlConfig.addProperty(
          new A3CMLProperty(cmd.name,
                            cmd.value));
        rollback.add(new UnsetPropertyCmd(cmd.name,cmd.value));
      } else {
        if (silence) return;
        throw new PropertyCmdException("Property " + cmd.name + " already exist.");
      }
    } catch (Exception exc) {
      if (logmon.isLoggable(BasicLevel.ERROR))
        logmon.log(BasicLevel.ERROR, "", exc);
      throw new PropertyCmdException(exc);
    }
  }
  
  /** 
   * set server property
   *
   * @param cmd SetServerPropertyCmd
   *
   * @exception PropertyCmdException
   */
  private void doReact(SetServerPropertyCmd cmd) throws PropertyCmdException {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,
                 "AgentAdmin.doReact(" + cmd + ")");

    try {
      A3CMLServer server = a3cmlConfig.getServer(cmd.serverName);
      if (!server.containsProperty(cmd.name)) {
        server.addProperty(
          new A3CMLProperty(cmd.name,
                            cmd.value));
        rollback.add(new UnsetServerPropertyCmd(cmd.serverName,cmd.name,cmd.value));
      } else {
        if (silence) return;
        throw new PropertyCmdException("Property " + cmd.name + 
                                       " already exist in server " + cmd.serverName);
      }
    } catch (Exception exc) {
      if (logmon.isLoggable(BasicLevel.ERROR))
        logmon.log(BasicLevel.ERROR, "", exc);
      throw new PropertyCmdException(exc);
    }
  }
  
  /** 
   * set network address translation
   *
   * @param cmd SetServerNatCmd
   *
   * @exception NatCmdException
   */
  private void doReact(SetServerNatCmd cmd) throws NatCmdException {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,
                 "AgentAdmin.doReact(" + cmd + ")");

    try {
      A3CMLServer server = a3cmlConfig.getServer(cmd.serverName);
      short sid = a3cmlConfig.getServerIdByName(cmd.translationServerName);
      if (!server.containsNat(sid)) {
        server.addNat(
          new A3CMLNat(sid,
                       cmd.translationHostName,
                       cmd.translationHostPort));
        rollback.add(new UnsetServerNatCmd(cmd.serverName,
                                           cmd.translationServerName,
                                           cmd.translationHostName,
                                           cmd.translationHostPort));
      } else {
        if (silence) return;
        throw new NatCmdException("Nat " + cmd.translationServerName + 
                                  " already exist in server " + cmd.serverName);
      }
    } catch (Exception exc) {
      if (logmon.isLoggable(BasicLevel.ERROR))
        logmon.log(BasicLevel.ERROR, "", exc);
      throw new NatCmdException(exc);
    }
  }

  /** 
   * start service
   *
   * @param cmd StartServiceCmd
   *
   * @exception ServiceCmdException
   */
  private void doReact(StartServiceCmd cmd) throws ServiceCmdException {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,
                 "AgentAdmin.doReact(" + cmd + ")");
    
    try {
      A3CMLServer a3cmlServer = 
        a3cmlConfig.getServer(AgentServer.getServerId());
      A3CMLService a3cmlService = 
        a3cmlServer.getService(cmd.className);
      ServiceManager.register(
        a3cmlService.classname, a3cmlService.args);
      ServiceDesc desc = 
        (ServiceDesc) ServiceManager.manager.registry.get(
          a3cmlService.classname);
      if (desc.running) return;
      ServiceManager.start(desc);
      if (stopScript == null) stopScript = new StopScript();
      stopScript.add(new StopServiceCmd(a3cmlService.classname,
                                        a3cmlService.args));
    } catch (Exception exc) {
      if (logmon.isLoggable(BasicLevel.ERROR))
        logmon.log(BasicLevel.ERROR, "", exc);
      throw new ServiceCmdException(exc);
    }
  }

  /** 
   * Starts a network (idempotent).
   *
   * @param cmd StartNetworkCmd
   *
   * @exception NetworkCmdException
   */
  private void doReact(StartNetworkCmd cmd) throws NetworkCmdException {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,
                 "AgentAdmin.doReact(" + cmd + ")");
    try {
      if (a3cmlConfig == null)
        a3cmlConfig = AgentServer.getConfig();
      A3CMLServer a3cmlServer = a3cmlConfig.getServer(cmd.sid);
      A3CMLNetwork a3cmlNetwork = null;
      for (int i = 0; i < a3cmlServer.networks.size(); i++) {
        A3CMLNetwork nw = 
          (A3CMLNetwork)a3cmlServer.networks.elementAt(i);
        if (nw.domain.equals(cmd.domainName)) {
          a3cmlNetwork = nw;
        }
      }
      
      if (a3cmlNetwork == null)
        throw new NetworkCmdException(
          "Unknown network: " + 
          cmd.sid + '.' + cmd.domainName);

      if (cmd.sid == AgentServer.getServerId()) {
        Network network;
        try {
          // The network may have already been 
          // added (idempotency).
          network =
            (Network) AgentServer.getConsumer(cmd.domainName);
        } catch (Exception exc) {
          network = null;
        }

        if (network != null) {         
          if (network.getPort() != a3cmlNetwork.port) {
            if (logmon.isLoggable(BasicLevel.DEBUG))
              logmon.log(BasicLevel.DEBUG,
                         " -> port change (before = " + 
                         network.getPort() + 
                         ", after = " + 
                         a3cmlNetwork.port + ')');
            // The network is local so we have to 
            // stop it in order to update the listen port.
            network.stop();
            network.setPort(a3cmlNetwork.port);
          }
          network.start();
        } else {
          A3CMLDomain a3cmlDomain = a3cmlConfig.getDomain(cmd.domainName);
          network = (Network) Class.forName(a3cmlDomain.network).newInstance();        
          short[] domainSids = new short[a3cmlDomain.servers.size()];
          for (int i = 0; i < domainSids.length; i++) {
            domainSids[i] = 
              ((A3CMLServer) a3cmlDomain.servers.elementAt(i)).sid;
          }
          AgentServer.addConsumer(cmd.domainName, network);
          network.init(a3cmlDomain.name, 
                       a3cmlNetwork.port, 
                       domainSids);
          network.start();
        }
      } else {
        // The network is remote so we just 
        // may have to update the port value
        // of the server desc.
        try {
          ServerDesc serverDesc = 
            AgentServer.getServerDesc(cmd.sid);
          if (cmd.domainName.equals(serverDesc.getDomainName())) {
            //serverDesc.setPort(a3cmlNetwork.port);
            serverDesc.updateSockAddr(
              serverDesc.getHostname(),
              a3cmlNetwork.port);
          }
        } catch (UnknownServerException exc) {
          // Nothing to do
        }
      }
      if (stopScript == null) stopScript = new StopScript();
      stopScript.add(new StopNetworkCmd(cmd.sid,
                                        cmd.domainName));
    } catch (Exception exc) {
      if (logmon.isLoggable(BasicLevel.ERROR))
        logmon.log(BasicLevel.ERROR, "", exc);
      throw new NetworkCmdException(exc);
    }
  }

  // DF: removed this command. It is replaced by StartNetworkCmd
  // 
  // private void doReact(UpdateNetworkPortCmd cmd) throws NetworkCmdException {    
//     try {
//       ServerDesc serverDesc = AgentServer.getServerDesc(cmd.sid);
//       if (cmd.domainName.equals(serverDesc.getDomainName())) {
//         // DF: Don't add the reverse action into the
//         // stopScript because the meaning of the
//         // update command does not fit into the start/stop
//         // script principle.
//         serverDesc.port = cmd.port;
//       }
//     } catch (Exception exc) {
//       if (logmon.isLoggable(BasicLevel.ERROR))
//         logmon.log(BasicLevel.ERROR, "", exc);
//       throw new NetworkCmdException(exc);
//     }
//   }

  /** 
   * start server
   *
   * @param cmd StartServerCmd
   *
   * @exception ServerCmdException
   */
  private void doReact(StartServerCmd cmd) throws ServerCmdException {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "AgentAdmin.doReact(" + cmd + ")");

    try {
      // Attention, on prend des infos de la config courante (!=  de celle
      // de l'AgentServer et on reporte les modifs dans les ServerDesc de
      // l'AgentServer.
      if (a3cmlConfig == null)
        a3cmlConfig = AgentServer.getConfig();
//    A3CMLServer current = a3cmlConfig.getServer(AgentServer.getServerId());
      A3CMLServer server = a3cmlConfig.getServer(cmd.sid);
      ServerDesc desc = AgentServer.getServerDesc(server.sid);

      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG,
                   "AgentAdmin.StartServerCmd : desc = " + desc);

//       if (current.containsNat(server.sid)) {
//         A3CMLNat nat = current.getNat(server.sid);
//         desc.setPort(nat.portT);
//         desc.setHostname(nat.hostT);
//         if (logmon.isLoggable(BasicLevel.DEBUG))
//           logmon.log(BasicLevel.DEBUG,
//                      "AgentAdmin.StartServerCmd : NAT desc = " + desc);
//       }

      AgentServer.initServerDesc(desc, server);
        if (desc.gateway == desc.sid)
          ((Network) desc.domain).addServer(server.sid);

      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG,
                   "AgentAdmin.StartServerCmd : " +
                   "desc = " + AgentServer.getServerDesc(server.sid));
    } catch (Exception exc) {
      if (logmon.isLoggable(BasicLevel.ERROR))
        logmon.log(BasicLevel.ERROR, "", exc);
      throw new ServerCmdException(exc);
    }
  }

  /** 
   * stop network
   *
   * @param cmd StopNetworkCmd
   *
   * @exception NetworkCmdException
   */
  private void doReact(StopNetworkCmd cmd) throws NetworkCmdException {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,
                 "AgentAdmin.doReact(" + cmd + ")");

    try {
      for (Enumeration c=AgentServer.getConsumers(); c.hasMoreElements(); ) {
	MessageConsumer consumer = (MessageConsumer) c.nextElement();
        if (logmon.isLoggable(BasicLevel.DEBUG))
          logmon.log(BasicLevel.DEBUG,
                     "AgentAdmin consumer = " + consumer);
        
        if (consumer.getDomainName().equals(cmd.domainName)
            && consumer.isRunning()) {
          if (logmon.isLoggable(BasicLevel.DEBUG))
            logmon.log(BasicLevel.DEBUG,
                       "AgentAdmin Stop domain = " + cmd.domainName);
          consumer.stop();

          A3CMLNetwork nw = getNetwork(
            AgentServer.getServerId(), cmd.domainName);
          if (nw == null) {
            // Means that the network has been removed.
            AgentServer.removeConsumer(cmd.domainName);
          }
        }
      }        
    } catch (Exception exc) {
      if (logmon.isLoggable(BasicLevel.ERROR))
        logmon.log(BasicLevel.ERROR, "", exc);
      throw new NetworkCmdException(exc);
    }
  }
  
  private A3CMLNetwork getNetwork(short sid, String domainName) 
    throws Exception {
    if (a3cmlConfig == null)
      a3cmlConfig = AgentServer.getConfig();
    A3CMLServer server = a3cmlConfig.getServer(sid);
    if (server == null) return null;
    for (int i = 0; i < server.networks.size(); i++) {
      A3CMLNetwork network = (A3CMLNetwork) server.networks.elementAt(i);
      if (network.domain.equals(domainName)) {
        return network;
      }
    }
    return null;
  }

  /** 
   * stop service
   *
   * @param cmd StopServiceCmd
   *
   * @exception ServiceCmdException
   */
  private void doReact(StopServiceCmd cmd) throws ServiceCmdException {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,
                 "AgentAdmin.doReact(" + cmd + ")");

   try {
      ServiceManager.stop(cmd.className);
    } catch (Exception exc) {
      if (logmon.isLoggable(BasicLevel.ERROR))
        logmon.log(BasicLevel.ERROR, "", exc);
      throw new ServiceCmdException(exc);
    }
  }

  /** 
   * remove domain
   *
   * @param cmd RemoveDomainCmd
   *
   * @exception DomainCmdException
   */
  private void doReact(RemoveDomainCmd cmd) throws DomainCmdException {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,
                 "AgentAdmin.doReact(" + cmd + ")");
    
    try {
      if (a3cmlConfig.containsDomain(cmd.name)) {
        A3CMLDomain domain = a3cmlConfig.getDomain(cmd.name);

        if (domain.servers == null) {
          removeDomain(cmd.name);
          return;
        }

        Vector toRemove = new Vector();
        for (Enumeration s = domain.servers.elements(); s.hasMoreElements(); ) {
          A3CMLServer server = (A3CMLServer) s.nextElement();

          if (server.networks.size() > 2) {
            doReact(new RemoveNetworkCmd(server.name,
                                         cmd.name));
          } else if (server.networks.size() == 2) {
            if ((((A3CMLNetwork) server.networks.elementAt(0)).domain.equals("transient") && 
                 ((A3CMLNetwork) server.networks.elementAt(1)).domain.equals(cmd.name)) ||
                (((A3CMLNetwork) server.networks.elementAt(1)).domain.equals("transient") && 
                 ((A3CMLNetwork) server.networks.elementAt(0)).domain.equals(cmd.name)))
              toRemove.addElement(new RemoveServerCmd(server.name,
                                                      server.hostname));
            else
              doReact(new RemoveNetworkCmd(server.name,
                                           cmd.name));
          } else
            toRemove.addElement(new RemoveServerCmd(server.name,
                                             server.hostname));
        }
        
        for (int i = 0; i < toRemove.size(); i++)
          doReact((RemoveServerCmd)toRemove.elementAt(i));
        
        removeDomain(cmd.name);
      }
    } catch (Exception exc) {
      if (logmon.isLoggable(BasicLevel.ERROR))
        logmon.log(BasicLevel.ERROR, "", exc);
      throw new DomainCmdException(exc);
    }
  }

  /** 
   * remove network
   *
   * @param cmd RemoveNetworkCmd
   *
   * @exception NetworkCmdException
   */
  private void doReact(RemoveNetworkCmd cmd) throws NetworkCmdException {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,
                 "AgentAdmin.doReact(" + cmd + ")");
    
    A3CMLServer server = null;
    try {
      server = a3cmlConfig.getServer(cmd.serverName);
    } catch(fr.dyade.aaa.agent.conf.UnknownServerException e) { return;}
    if (server == null) return;
    
    try {
      if (server.sid == AgentServer.getServerId()) {
        if (logmon.isLoggable(BasicLevel.DEBUG))
          logmon.log(BasicLevel.DEBUG,
                     "stopScript.add( StopNetworkCmd(" + 
                     cmd.serverName + "))");
        stopScript.add(new StopNetworkCmd(server.sid,
                                          cmd.domain));
      }
      removeNetwork(server.sid,cmd.domain);
    } catch (Exception exc) {
      if (logmon.isLoggable(BasicLevel.ERROR))
        logmon.log(BasicLevel.ERROR, "", exc);
      throw new NetworkCmdException(exc);
    }
  }

  /** 
   * remove server
   *
   * @param cmd RemoveServerCmd
   *
   * @exception ServerCmdException
   */
  private void doReact(RemoveServerCmd cmd) throws ServerCmdException {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,
                 "AgentAdmin.doReact(" + cmd + ")");
    
    A3CMLServer svr = null;
    try {
      svr = a3cmlConfig.getServer(cmd.name);
    } catch (fr.dyade.aaa.agent.conf.UnknownServerException exc) {
      return;
    }
    if (svr == null) return;

    try {
      removeServer(svr.sid);
    } catch (Exception exc) {
      if (logmon.isLoggable(BasicLevel.ERROR))
        logmon.log(BasicLevel.ERROR, "", exc);
      throw new ServerCmdException(exc);
    }
  }

  /** 
   * remove service
   *
   * @param cmd RemoveServiceCmd
   *
   * @exception ServiceCmdException
   */
  private void doReact(RemoveServiceCmd cmd) throws ServiceCmdException {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,
                 "AgentAdmin.doReact(" + cmd + ")");
    
    A3CMLServer server = null;
    try {
      server = a3cmlConfig.getServer(cmd.serverName);
    } catch(fr.dyade.aaa.agent.conf.UnknownServerException exc) { return;}

    if (server == null) return;
    try {
      if (server.sid == AgentServer.getServerId()) {
        if (logmon.isLoggable(BasicLevel.DEBUG))
          logmon.log(BasicLevel.DEBUG,
                     "stopScript.add( StopServiceCmd(" + 
                     cmd.className + "))");
        stopScript.add(new StopServiceCmd(cmd.className,
                                          cmd.args));
      }
      removeService(server.sid,cmd.className);
    } catch (Exception exc) {
      if (logmon.isLoggable(BasicLevel.ERROR))
        logmon.log(BasicLevel.ERROR, "", exc);
      throw new ServiceCmdException(exc);
    }
  }

  /** 
   * unset server property
   *
   * @param cmd UnsetServerPropertyCmd
   *
   * @exception PropertyCmdException
   */
  private void doReact(UnsetServerPropertyCmd cmd) throws PropertyCmdException {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,
                 "AgentAdmin.doReact(" + cmd + ")");
    
    A3CMLServer server = null;
    try {
      server = a3cmlConfig.getServer(cmd.serverName);
    } catch(fr.dyade.aaa.agent.conf.UnknownServerException exc) { return;}
    
    if (server == null) return;
    try {
      unsetServerProperty(server.sid,cmd.name);
    } catch (Exception exc) {
      if (logmon.isLoggable(BasicLevel.ERROR))
        logmon.log(BasicLevel.ERROR, "", exc);
      throw new PropertyCmdException(exc);
    }
  }

  /** 
   * unset property
   *
   * @param cmd UnsetPropertyCmd
   *
   * @exception PropertyCmdException
   */
  private void doReact(UnsetPropertyCmd cmd) throws PropertyCmdException {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,
                 "AgentAdmin.doReact(" + cmd + ")");
    
    try {
      unsetProperty(cmd.name);
    } catch (Exception exc) {
      if (logmon.isLoggable(BasicLevel.ERROR))
        logmon.log(BasicLevel.ERROR, "", exc);
      throw new PropertyCmdException(exc);
    }
  }

  /** 
   * unset network address translation
   *
   * @param cmd UnsetServerNatCmd
   *
   * @exception NatCmdException
   */
  private void doReact(UnsetServerNatCmd cmd) throws NatCmdException {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,
                 "AgentAdmin.doReact(" + cmd + ")");

    try {
      A3CMLServer server = a3cmlConfig.getServer(cmd.serverName);
      short sid = a3cmlConfig.getServerIdByName(cmd.translationServerName);
      server.removeNat(sid);
    } catch (Exception exc) {
      if (logmon.isLoggable(BasicLevel.ERROR))
        logmon.log(BasicLevel.ERROR, "", exc);
      throw new NatCmdException(exc);
    }
  }
  /** 
   * unset jvm args
   *
   * @param cmd UnsetJvmArgsCmd
   *
   * @exception JvmArgsCmdException
   */
  private void doReact(UnsetJvmArgsCmd cmd) throws JvmArgsCmdException {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,
                 "AgentAdmin.doReact(" + cmd + ")");
    
    A3CMLServer server = null;
    try {
      server = a3cmlConfig.getServer(cmd.serverName);
    } catch(fr.dyade.aaa.agent.conf.UnknownServerException exc) { return;}
    
    if (server == null) return;
    try {
      unsetJvmArgs(server.sid,cmd.args);
    } catch (Exception exc) {
      if (logmon.isLoggable(BasicLevel.ERROR))
        logmon.log(BasicLevel.ERROR, "", exc);
      throw new JvmArgsCmdException(exc);
    }
  }

  private void removeDomain(String domainName) throws Exception {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,
                 "AgentAdmin.removeDomain(" + domainName + ")");
    
    a3cmlConfig.removeDomain(domainName);
  }

  private void removeNetwork(short sid, String domainName) throws Exception {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,
                 "AgentAdmin.removeNetwork(" + sid + 
                 "," + domainName + ")");
    try {
      A3CMLServer server = a3cmlConfig.getServer(sid);
      if (server == null) return;
      for (int i = 0; i < server.networks.size(); i++) {
        A3CMLNetwork network = 
          (A3CMLNetwork)server.networks.elementAt(i);
        if (network.domain.equals(domainName)) {
          if (logmon.isLoggable(BasicLevel.DEBUG))
            logmon.log(BasicLevel.DEBUG,
                       "AgentAdmin.removeNetwork remove : " + network);
          server.networks.removeElementAt(i);
        }
      }
    } catch (fr.dyade.aaa.agent.conf.UnknownServerException exc) {
      if (logmon.isLoggable(BasicLevel.ERROR))
        logmon.log(BasicLevel.ERROR, "", exc);
      return;
    }
  }

  private void removeServer(short sid) throws Exception {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,
                 "AgentAdmin.removeServer(" + sid + ")");

    try {
      ServerDesc servDesc = AgentServer.getServerDesc(sid);
      if (servDesc.domain instanceof Network)
        ((Network) servDesc.domain).delServer(sid);
    } catch (Exception exc) {
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG,
                   "AgentAdmin.removeServer remove server #" + sid, exc);
      return;
    }

    try {
      // remove server in serverDesc
      AgentServer.removeServerDesc(sid);
      for (Enumeration e = AgentServer.elementsServerDesc(); e.hasMoreElements(); ) {
        ServerDesc sd = (ServerDesc)e.nextElement();
        if (sd.gateway == sid) {
          sd.gateway = -1;
          sd.domain = null;
        }
      }
    } catch (Exception exc) {
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG,
                   "AgentAdmin.removeServer remove server #" + sid, exc);
    }

    // remove server in a3cmlConfig
    a3cmlConfig.removeServer(sid);
    for (Enumeration d = a3cmlConfig.domains.elements(); d.hasMoreElements(); ) {
      A3CMLDomain domain = (A3CMLDomain)d.nextElement();
      for (int i = 0; i < domain.servers.size(); i++) {
        A3CMLServer server = (A3CMLServer) domain.servers.elementAt(i);
        if (server.sid == sid) {
          domain.servers.removeElementAt(i);
          if (logmon.isLoggable(BasicLevel.DEBUG))
            logmon.log(BasicLevel.DEBUG,
                       "AgentAdmin.removeServer remove server #" + sid +
                       " in domain " + domain);
        }
      }
    }
  }

  private void removeService(short sid, String className) throws Exception {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,
                 "AgentAdmin.removeService(" + sid +
                 "," + className + ")");

    A3CMLServer server = a3cmlConfig.getServer(sid);
    if (server == null) return;
    for (int i = 0; i < server.services.size(); i++) {
      A3CMLService service = 
        (A3CMLService) server.services.elementAt(i);
      if (service.classname.equals(className)) {
        server.services.removeElementAt(i);
        if (logmon.isLoggable(BasicLevel.DEBUG))
          logmon.log(BasicLevel.DEBUG,
                     "AgentAdmin.removeService service = " + service);
      }
    }
  }

  private void unsetServerProperty(short sid, String name) throws Exception {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,
                 "AgentAdmin.unsetServerProperty(" + sid +
                 "," + name + ")");
    
    A3CMLServer server = a3cmlConfig.getServer(sid);
    if (server == null) return;
    server.removeProperty(name);
  }

  private void unsetProperty(String name) throws Exception {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,
                 "AgentAdmin.unsetProperty(" + name + ")");

    a3cmlConfig.removeProperty(name);
  }

  private void unsetJvmArgs(short sid, String args) throws Exception {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,
                 "AgentAdmin.unsetArgs(" + sid +
                 "," + name + ")");

    A3CMLServer server = a3cmlConfig.getServer(sid);
    if (server == null) return;
    //server.jvmArgs = server.jvmArgs.replaceAll(args,"");

     StringTokenizer st = new StringTokenizer(args);
     while (st.hasMoreTokens()) {
       String toRemove = st.nextToken();
       int i = server.jvmArgs.indexOf(toRemove);
       if (i > -1) {
         String begin = server.jvmArgs.substring(0,i);
         String end = server.jvmArgs.substring(i + toRemove.length());
         server.jvmArgs = begin.concat(end);
       }
     }

    if (server.jvmArgs == "")
      server.jvmArgs = null;
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG,
                 "AgentAdmin.unsetArgs jvmArgs = " + server.jvmArgs);
  }

  public Short getSid() {
    if (maxId > -1) {
      maxId += 1;
      return new Short(maxId);
    } else {
      return null;
    }
  }

//   private boolean setGatewayAndDomain(A3CMLPServer server,
//                                       A3CMLPServer current) throws Exception {
//     if (logmon.isLoggable(BasicLevel.DEBUG))
//       logmon.log(BasicLevel.DEBUG,
//                  "AgentAdmin.setGatewayAndDomain(" + server + "," + current + ")");
        
//     ServerDesc desc = AgentServer.getServerDesc(server.sid);
//     logmon.log(BasicLevel.DEBUG,
//                  "AgentAdmin.setGatewayAndDomain:" + desc);

//     // Get routing infos from config
//     if (server.domain != null) {
//       if (server.gateway != -1)
//         desc.gateway = server.gateway;
//       else
//         desc.gateway = server.sid;
// //       desc.domain = a3cmlConfig.getDomain(server.domain).consumer;
//       desc.domain = AgentServer.getConsumer(server.domain);
//       logmon.log(BasicLevel.DEBUG, "AgentAdmin.setGatewayAndDomain:" + desc);
    
//       return true;
//     }

//     return false;
//   }
}
