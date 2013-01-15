/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2009 - 2012 ScalAgent Distributed Technologies
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
package framework;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.util.monolog.api.BasicLevel;

import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.agent.conf.A3CML;
import fr.dyade.aaa.agent.conf.A3CMLConfig;
import fr.dyade.aaa.agent.conf.UnknownServiceException;

public class SCAdminClassic extends SCBaseAdmin {
  private static byte [] halt = "halt\n".getBytes();
  
  protected byte[] getHaltCommand() {
    return halt;
  }

  public void startAgentServer(short sid, String[] jvmargs) throws Exception {
    logmon.log(BasicLevel.DEBUG, "SCAdmin: run AgentServer#" + sid);

    Server server = (Server) launchedServers.get(new Short(sid));

    if (server != null) {
      try {
        int exitValue = server.process.exitValue();
        if (logmon.isLoggable(BasicLevel.DEBUG)) {
          logmon.log(BasicLevel.DEBUG, "SCAdmin: AgentServer#" + sid + " -> " + exitValue);
        }
      } catch (IllegalThreadStateException exc) {
        if (logmon.isLoggable(BasicLevel.WARN)) {
          logmon.log(BasicLevel.WARN, "SCAdmin: AgentServer#" + sid + " already running.");
        }
        throw new IllegalStateException("AgentServer#" + sid + " already running.");
      }
    }
    
    // Get the configuration
    A3CMLConfig a3config;
    try {
      String configPath = null;
      if (jvmargs != null) {
        for (int i = 0; i < jvmargs.length; i++) {
          String jvmArg = jvmargs[i];
          if (jvmArg.startsWith("-D" + AgentServer.CFG_FILE_PROPERTY)) {
            configPath = jvmArg.substring(2 + AgentServer.CFG_FILE_PROPERTY.length() + 1);
            break;
          }
        }
      }
      if (configPath == null) {
        a3config = A3CML.getXMLConfig();
      } else {
        a3config = A3CML.getXMLConfig(configPath);
      }
    } catch (Exception exc) {
      logmon.log(BasicLevel.ERROR, "SCAdmin: problem during configuration parsing", exc);
      throw new Exception("Problem during configuration parsing");
    }

//    String javapath = new File(new File(System.getProperty("java.home"), "bin"), "java").getPath();
//    String classpath = System.getProperty("java.class.path");

//    List argv = new ArrayList();
//    argv.add(javapath);
//
//    argv.add("-classpath");
//    argv.add(classpath);
//    if (jvmargs != null) {
//      for (int i = 0; i < jvmargs.length; i++)
//        argv.add(jvmargs[i]);
//    }
//
//    // Add JMX monitoring options
//    argv.add("-Dcom.sun.management.jmxremote");

    // Retrieve port from a3 configuration file (a3servers.xml)
    int port = -1;
    try {
      port = Integer.parseInt(a3config.getServiceArgs(sid, "fr.dyade.aaa.agent.AdminProxy"));
    } catch (UnknownServiceException exc) {
      if (logmon.isLoggable(BasicLevel.WARN)) {
        logmon.log(BasicLevel.WARN, "SCAdmin: AdminProxy service not found, server will not be stoppable "
            + "using SCAdmin. Only the killAgentServer() method can be used. ");
      }
    }
    if (logmon.isLoggable(BasicLevel.DEBUG)) {
      logmon.log(BasicLevel.DEBUG, "SCAdmin: AgentServer#" + sid + " telnet port: " + port);
    }

//    // Main class
//    argv.add();
//    argv.add(Short.toString(sid));
//    argv.add("s" + sid);

    if (logmon.isLoggable(BasicLevel.DEBUG)) {
      logmon.log(BasicLevel.DEBUG, "SCAdmin" + ": launches AgentServer#" + sid);
    }

//    Process p = Runtime.getRuntime().exec((String[]) argv.toArray(new String[argv.size()]));
//
//    p.getInputStream().close();
//    p.getOutputStream().close();
//    p.getErrorStream().close();

    Process p = BaseTestCase.startProcess("fr.dyade.aaa.agent.AgentServer",
                                          jvmargs,
                                          new String[] {Short.toString(sid), "s" + sid});
    
    launchedServers.put(new Short(sid), new Server(port, p));
  }
}