/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2012 ScalAgent Distributed Technologies
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
package org.ow2.joram.spring;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;
import org.springframework.beans.factory.DisposableBean;

import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.common.Debug;

/**
 * Start/Stop a Joram server.
 */
public class JoramServer implements DisposableBean {
  
  public static final Logger logmon = Debug.getLogger(JoramServer.class.getName());
      
  private boolean stopServer = false;
  
  public JoramServer() {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "JoramServer.<init>()");
  }
  
  /**
   * Create and start a Joram server (only if collocated).
   * 
   * @param sid the server id
   * @param pathToConf the path to the configuration (a3servers.xml, ...)
   * @param storage the path to the persistent directory
   * @param collocated true if the Joram server is collocated
   * @param persistent true if the Joram server if persistent
   */
  public void create(short sid, String pathToConf, String storage, boolean collocated, boolean persistent) {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "JoramServer.create(" + sid + ", " + pathToConf + ", " + storage + ", " + collocated + ", " + persistent + ')');
    
    System.setProperty(AgentServer.CFG_DIR_PROPERTY, pathToConf);
    System.setProperty(Debug.DEBUG_DIR_PROPERTY, pathToConf);
    if (!persistent)
      System.setProperty("Transaction", "fr.dyade.aaa.util.NullTransaction");
    //<property name="Transaction" value="fr.dyade.aaa.util.NTransaction" />
    
    if (collocated) {
      if (AgentServer.getStatus() == AgentServer.Status.STARTED ||
          AgentServer.getStatus() == AgentServer.Status.STARTING)
        return;

      try {
        AgentServer.init(sid, storage, null);
      } catch (Exception exc) {
        logmon.log(BasicLevel.ERROR, AgentServer.getName() + " initialization failed", exc);
      }

      try {
        String errStr = AgentServer.start();
        if (errStr == null) {
          if (logmon.isLoggable(BasicLevel.INFO))
            logmon.log(BasicLevel.INFO, AgentServer.getName() + " started: " + AgentServer.OKSTRING);
        } else {
          if (logmon.isLoggable(BasicLevel.INFO)) {
            logmon.log(BasicLevel.INFO, AgentServer.getName() + " started: " + AgentServer.ERRORSTRING +
                "\n" + errStr + "\n" + AgentServer.ENDSTRING);
          }
        }
      } catch (Exception exc) {
        logmon.log(BasicLevel.ERROR, AgentServer.getName() + " failed", exc);
      }

    } else{
      //TODO
    }
  }

  /**
   * Stop the Joram server
   * @see org.springframework.beans.factory.DisposableBean#destroy()
   */
  public void destroy() throws Exception {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, "JoramServer.destroy()");
    if (stopServer)
      AgentServer.stop(true, 0, true);
  }

  /**
   * @return the stopServer
   */
  public boolean isStopServer() {
    return stopServer;
  }

  /**
   * @param stopServer the stopServer to set
   */
  public void setStopServer(boolean stopServer) {
    this.stopServer = stopServer;
  }
}
