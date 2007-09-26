/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2003 ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
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
 * Initial developer(s): David Feliot
 */
package fr.dyade.aaa.jndi2.server;

import java.util.*;
import java.io.*;

import fr.dyade.aaa.agent.*;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

public class Container 
    extends Agent 
    implements BagSerializer {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private Vector entryPoints;

  private LifeCycleListener lifeCycleListener;

  private BagSerializer bagSerializer;

  /**
   * This agent cannot be swapped and has 
   * a reserved identifier on each agent server.
   */
  public Container() {
    super("", true, AgentId.LocalJndiServiceStamp);
    entryPoints = new Vector(2);
  }

  public void addEntryPoint(EntryPoint entryPoint) {
    entryPoints.addElement(entryPoint);
  }

  public void setLifeCycleListener(LifeCycleListener lifeCycleListener) {
    this.lifeCycleListener = lifeCycleListener;
  }

  public void setBagSerializer(BagSerializer bagSerializer) {
    this.bagSerializer = bagSerializer;
  }

  public void react(AgentId from, Notification not) throws Exception {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, "\n\nJndiServer[" + getId() +
                       "].react(" + from + ',' + not + ')');
    setNoSave();
    for (int i = 0; i < entryPoints.size(); i++) {
      EntryPoint entryPoint = 
        (EntryPoint)entryPoints.elementAt(i);
      if (entryPoint.accept(from, not)) {
        return;
      }
    }
    super.react(from, not);    
  }

  public void agentInitialize(boolean firstTime) throws Exception {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, "\n\nJndiServer[" + getId() + 
                       "].agentInitialize(" + firstTime + ')');
    lifeCycleListener.agentInitialize(firstTime);
  }
  
  public void agentFinalize(boolean lastTime) {
    lifeCycleListener.agentFinalize(lastTime);
  }

  void sendNotification(AgentId to, Notification not) {
    sendTo(to, not);
  }
  
  public void writeBag(ObjectOutputStream out)
    throws IOException {
    if (bagSerializer != null)
      bagSerializer.writeBag(out);
  }

  public void readBag(ObjectInputStream in) 
    throws IOException, ClassNotFoundException {
    if (bagSerializer != null)
      bagSerializer.readBag(in);
  }
}
