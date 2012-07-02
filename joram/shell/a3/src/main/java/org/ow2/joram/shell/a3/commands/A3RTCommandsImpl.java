/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2012 - ScalAgent Distributed Technologies
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
package org.ow2.joram.shell.a3.commands;

import org.objectweb.util.monolog.api.LoggerFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.util.tracker.ServiceTracker;

import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.agent.EngineMBean;
import fr.dyade.aaa.ext.NGTransactionMBean;

public class A3RTCommandsImpl implements A3RTCommands {

  public static final String NAMESPACE = "joram:a3";
  private static final int TIMEOUT = 1000;
  
  // A3 Server default properties
  public static final String AGENT_SERVER_ID_PROPERTY = "fr.dyade.aaa.agent.AgentServer.id";
  public static final String AGENT_SERVER_CLUSTERID_PROPERTY = "fr.dyade.aaa.agent.AgentServer.clusterid";
  public static final String AGENT_SERVER_STORAGE_PROPERTY = "fr.dyade.aaa.agent.AgentServer.storage";


//  private static void help(String command) {
//    StringBuffer buf = new StringBuffer();
//    String fullCommand = "["+NAMESPACE+":]"+command;
//    buf.append("Usage: ").append(fullCommand).append(" ");
//    if(command.equals("")) {
//      buf.append("");
//    } else if(command.equals("")) {
//    } else if(command.equals("")) {
//    } else {
//      System.err.println("Unknown command: "+command);
//      return;
//    }
//    System.out.println(buf.toString());
//  }
  
  private BundleContext bundleContext;
  private ServiceTracker engineTracker;
  private ServiceTracker ngtTracker;

  public A3RTCommandsImpl(BundleContext context) {
    this.bundleContext = context;
    this.engineTracker = new ServiceTracker
      (bundleContext, EngineMBean.class, null);
    this.ngtTracker = new ServiceTracker
      (bundleContext, NGTransactionMBean.class, null);
    engineTracker.open();
    ngtTracker.open();
  }

  public void engineLoad(String[] args) {
    EngineMBean engine;
    try {
      engine = (EngineMBean) engineTracker.waitForService(TIMEOUT);
    } catch (InterruptedException e) {
      System.err.println("Error: Interrupted.");
      engine = null;
    }
    if(engine == null) {
      System.err.println("Error: No engine found.");
      return;
    }
    float load = engine.getAverageLoad1();
    System.out.println("Engine load (for the last min.): "+load);
  }
 
  /* Charge du moteur de persistance transactionnel
   *    - 2 paramètres charge moyenne et max
   *    - par défaut pas de paramètres => charge moyenne = 5, charge max = 30
   *    - AgentServer:cons=Transaction,server=AgentServer#0:GarbageRatio
   */
  public void garbageRatio(String[] args) {
    NGTransactionMBean ngt;
    try {
      ngt = (NGTransactionMBean) ngtTracker.waitForService(TIMEOUT);
    } catch (InterruptedException e) {
      System.err.println("Error: Interrupted.");
      ngt = null;
    }
    if(ngt == null) {
      System.err.println("Error: No NG transaction found.");
      return;
    }
    float load = ngt.getGarbageRatio();
    System.out.println("Garbage operations ratio: "+load);
  }



  public void stopServer() {
    System.out.print("Server stopping... "); System.out.flush();
    AgentServer.stop();
    AgentServer.reset();
    System.out.println("Done.");
  }



  public void startServer() {
    short sid = getShortProperty(AGENT_SERVER_ID_PROPERTY, (short) 0);
    String path = getProperty(AGENT_SERVER_STORAGE_PROPERTY, "s"+sid);
    short clusterId = getShortProperty(AGENT_SERVER_CLUSTERID_PROPERTY, AgentServer.NULL_ID);
    System.out.print("Server starting... "); System.out.flush();
    try {
      LoggerFactory f = null;
      AgentServer.init(sid,path,f,clusterId);
      AgentServer.start();
    } catch (Exception e) {
      System.err.println("Error: "+e.getMessage());
    }
    System.out.println("Started.");
  }



  public void restartServer() {
    stopServer();
    startServer();
  }
  
  public void close() {
    stopServer();
    Bundle bundle0 = bundleContext.getBundle(0);
    try {
      bundle0.stop();
    } catch (BundleException e) {
      System.err.println("Error: Failed to stop the System Bundle.");
      e.printStackTrace();
    }
  }
  
  private String getProperty(String prop, String defaultVal) {
    String val = bundleContext.getProperty(prop);
    if(val!=null)
      return val;
    else
      return defaultVal;
  }
  
  private Short getShortProperty(String prop, Short defaultVal) {
    String val = bundleContext.getProperty(prop);
    if(val!=null)
      return Short.parseShort(val);
    else
      return defaultVal;
  }
  
  
  
}
