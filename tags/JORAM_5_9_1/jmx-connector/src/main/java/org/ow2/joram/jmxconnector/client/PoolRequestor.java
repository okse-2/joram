/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2011 ScalAgent Distributed Technologies
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
package org.ow2.joram.jmxconnector.client;

import java.io.IOException;
import java.io.Serializable;

import javax.jms.Connection;
import javax.jms.JMSException;

import fr.dyade.aaa.common.Pool;

/**
 * This class allows synchronous call through a JMS connection. It use a pool
 * of requestors to allow concurrent calls.
 */
public class PoolRequestor {
  private Connection cnx;
  private Pool pool = null;
  private String qname = null;

  public PoolRequestor(Connection cnx, String qname, int capacity) {
    this.cnx = cnx;
    this.qname = qname;
    pool = new Pool("Pool Requestor", capacity);
  }

  public Object request(Serializable request) throws IOException {
    Object reply = null;
    Requestor requestor = null;
    try {
      requestor = allocRequestor();
      reply = requestor.request(request);
      if (reply instanceof Throwable)
        throw new IOException((Throwable) reply);
    } catch (JMSException exc) {
      exc.printStackTrace();
      throw new IOException(exc);
    } finally {
      freeRequestor(requestor);
    }
    return reply;
  }
    
  private Requestor allocRequestor() throws JMSException {
    Requestor requestor = null;
    try {
      requestor = (Requestor) pool.allocElement();
    } catch (Exception exc) {
      return new Requestor(cnx, qname);
    }
    return requestor;
  }

  private void freeRequestor(Requestor requestor) {
    pool.freeElement(requestor);
  }
}
