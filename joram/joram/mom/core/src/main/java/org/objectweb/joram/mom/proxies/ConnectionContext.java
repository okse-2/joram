/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2006 - 2013 ScalAgent Distributed Technologies
 * Copyright (C) 1996 - 2000 Dyade
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
package org.objectweb.joram.mom.proxies;

import org.objectweb.joram.shared.client.AbstractJmsReply;
import org.objectweb.joram.shared.client.AbstractJmsRequest;
import org.objectweb.joram.shared.excepts.MomException;


/**
 * Interface to abstract the communication between the client and the MOM.
 */
public interface ConnectionContext {
  
  public enum Type {
    STANDARD, RELIABLE, AMQP, MQTT;
    
    public String getClassName() {
      switch(this) {
      case RELIABLE:
        return "org.objectweb.joram.mom.proxies.ReliableConnectionContext";
      case AMQP:
        //TODO
        return "com.scalagent.amqp.adapter.agent.AMQPConnectionContext";
      case MQTT:
        return "com.scalagent.jorammq.mqtt.adapter.JoramConnectionContext";
      case STANDARD:
      default:
        return "org.objectweb.joram.mom.proxies.StandardConnectionContext";
      }
    }
  }
  
  public void initialize(int key, OpenConnectionNot not);
  
  public AbstractJmsRequest getRequest(Object req);

  public int getKey();
  
  public void pushReply(AbstractJmsReply reply);
  
  public void pushError(MomException exc);
  
  public boolean isClosed();
  
}
