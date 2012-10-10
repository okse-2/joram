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
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.util.monolog.api.BasicLevel;

import fr.dyade.aaa.agent.osgi.Activator;

public class SCAdminOSGi extends SCBaseAdmin {
  // use stop 0 to shutdown ! (available in felix and gogo)
  private static byte [] halt = "stop 0\n".getBytes();
  
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

    String javapath = new File(new File(System.getProperty("java.home"), "bin"), "java").getPath();

    // Find felix jar and put it on the classpath
    File felixbin = new File(System.getProperty("felix.dir") + "/felix.jar");
    if (!felixbin.exists()) {
      throw new Exception("Felix framework not found.");
    }
    List argv = new ArrayList();
    argv.add(javapath);

    argv.add("-classpath");
    argv.add("." + File.pathSeparatorChar + felixbin.getAbsolutePath());
    
    if (jvmargs != null) {
      for (int i = 0; i < jvmargs.length; i++)
        argv.add(jvmargs[i]);
    }

    // Add JMX monitoring options
    argv.add("-Dcom.sun.management.jmxremote");

    // Choose a random telnet port if unspecified
    int port = Integer.getInteger("osgi.shell.telnet.port", getFreePort());
    if (logmon.isLoggable(BasicLevel.DEBUG)) {
      logmon.log(BasicLevel.DEBUG, "SCAdmin: AgentServer#" + sid + " telnet port: " + port);
    }

    // Get felix configuration file.
    URI configFile = new URI(System.getProperty("felix.config.properties", "file:config.properties"));
    argv.add("-Dfelix.config.properties=" + configFile);

    // Assign AgentServer properties for server id, storage directory and cluster id.
    argv.add("-Dorg.osgi.framework.storage=" + 's' + sid + "/felix-cache");
    argv.add("-Dosgi.shell.telnet.port=" + port);
    argv.add("-D" + Activator.AGENT_SERVER_ID_PROPERTY + '=' + sid);
    argv.add("-D" + Activator.AGENT_SERVER_STORAGE_PROPERTY + "=s" + sid);
    argv.add("-XX:+UnlockDiagnosticVMOptions");
    argv.add("-XX:+UnsyncloadClass");
    argv.add("-Dgosh.args=--nointeractive");// need with gogo

    // Main class
    argv.add("org.apache.felix.main.Main");

    if (logmon.isLoggable(BasicLevel.DEBUG)) {
      logmon.log(BasicLevel.DEBUG, "SCAdmin" + ": launches AgentServer#" + sid + " with: " + argv);
    }

    Process p = Runtime.getRuntime().exec((String[]) argv.toArray(new String[argv.size()]));

    p.getInputStream().close();
    p.getOutputStream().close();
    p.getErrorStream().close();

    launchedServers.put(new Short(sid), new Server(port, p));
  }
}