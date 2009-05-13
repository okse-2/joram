/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - 2009 ScalAgent Distributed Technologies
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
package org.objectweb.joram.client.jms.ha.local;

import java.util.*;

import javax.jms.*;

import org.objectweb.joram.client.jms.local.*;
import org.objectweb.joram.shared.client.*;
import org.objectweb.joram.shared.security.Identity;
import org.objectweb.joram.mom.proxies.*;
import org.objectweb.joram.mom.notifications.*;
import org.objectweb.joram.mom.dest.AdminTopic;

import org.objectweb.joram.client.jms.connection.RequestChannel;

import fr.dyade.aaa.agent.*;
import fr.dyade.aaa.util.Debug;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

public class HALocalRequestChannel implements RequestChannel {
  /** logger */
  public static Logger logger = Debug.getLogger(HALocalRequestChannel.class.getName());

  public final static int NONE = 0;
  public final static int INIT = 1;
  public final static int RUN = 2;

  private static Object lock = new Object();

  private static int status;

  public static void init(String args, boolean firstTime) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "HALocalConnection.init(" + args + ',' + firstTime + ')');

    synchronized (lock) {
      status = INIT;
      lock.notifyAll();
    }
  }

  public static void waitForStart() {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "HALocalConnection.waitForStart()");

    synchronized (lock) {
      while (status == NONE) {
        try {
          lock.wait();
        } catch (InterruptedException exc) {}
      }

      if (status == INIT) {
        // Clean the proxies
        GetProxyIdListNot gpin = new GetProxyIdListNot();
        AgentId[] proxyIds;
        try {
          gpin.invoke(AdminTopic.getDefault());
          proxyIds = gpin.getIds();
          ResetCollocatedConnectionsNot rccn = 
            new ResetCollocatedConnectionsNot();
          for (int i = 0; i < proxyIds.length; i++) {
            Channel.sendTo(proxyIds[i], rccn);
          }
          status = RUN;
        } catch (Exception exc) {
          logger.log(BasicLevel.ERROR, "", exc);
          throw new Error(exc.toString());
        }
      }
    }
  }

  private Identity identity;

  private LocalRequestChannel localRequestChannel;

  public HALocalRequestChannel(Identity identity) throws JMSException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "HALocalConnection.<init>(" + identity + ')');
    waitForStart();
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, " -> create the local connection");
    this.identity = identity;
  }

  public void setTimer(Timer timer) {
    // No timer is useful
  }

  public void connect() throws Exception {
    localRequestChannel = new LocalRequestChannel(identity);
    localRequestChannel.connect();
  }

  public void send(AbstractJmsRequest request) 
  throws Exception {
    localRequestChannel.send(request);
  }

  public AbstractJmsReply receive() 
  throws Exception {
    return localRequestChannel.receive();
  }

  public void close() {
    localRequestChannel.close();
  }
}
