/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2010 ScalAgent Distributed Technologies
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
package org.objectweb.joram.mom.dest.jms;

import javax.jms.IllegalStateException;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.objectweb.joram.client.jms.XidImpl;
import org.objectweb.joram.mom.dest.DistributionHandler;
import org.objectweb.joram.shared.messages.Message;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Debug;

public class JMSDistribution extends JMSModule implements DistributionHandler {

  private static final Logger logger = Debug.getLogger(JMSDistribution.class.getName());

  /** Producer object. */
  protected MessageProducer producer;

  public void distribute(Message message) throws Exception {
    if (!usable) {
      throw new IllegalStateException(notUsableMessage);
    }

    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "send(" + message + ')');
    }

    synchronized (lock) {
      Xid xid = null;
      if (isXA) {
        xid = new XidImpl(new byte[0], 1, message.id.getBytes());
        if (logger.isLoggable(BasicLevel.DEBUG)) {
          logger.log(BasicLevel.DEBUG, "send: xid=" + xid);
        }

        try {
          xaRes.start(xid, XAResource.TMNOFLAGS);
        } catch (XAException e) {
          if (logger.isLoggable(BasicLevel.WARN)) {
            logger.log(BasicLevel.WARN, "Exception:: XA can't start resource : " + xaRes, e);
          }
        }
      }

      producer.send(org.objectweb.joram.client.jms.Message.wrapMomMessage(null, message));

      if (logger.isLoggable(BasicLevel.DEBUG)) {
        logger.log(BasicLevel.DEBUG, "send: " + producer + " send.");
      }

      if (isXA) {
        try {
          xaRes.end(xid, XAResource.TMSUCCESS);
          if (logger.isLoggable(BasicLevel.DEBUG)) {
            logger.log(BasicLevel.DEBUG, "send: XA end " + xaRes);
          }
        } catch (XAException e) {
          throw new JMSException("resource end(...) failed: " + xaRes + " :: " + e.getMessage());
        }
        try {
          int ret = xaRes.prepare(xid);
          if (ret == XAResource.XA_OK) {
            xaRes.commit(xid, false);
          }
          if (logger.isLoggable(BasicLevel.DEBUG)) {
            logger.log(BasicLevel.DEBUG, "send: XA commit " + xaRes);
          }
        } catch (XAException e) {
          try {
            xaRes.rollback(xid);
            if (logger.isLoggable(BasicLevel.DEBUG)) {
              logger.log(BasicLevel.DEBUG, "send: XA rollback " + xaRes);
            }
          } catch (XAException e1) {
          }
          throw new JMSException("XA resource rollback(" + xid + ") failed: " + xaRes + " :: "
              + e.getMessage());
        }
      }
    }
  }

  /**
   * Opens a connection with the foreign JMS server and creates the JMS
   * resources for interacting with the foreign JMS destination.
   * 
   * @exception JMSException
   *              If the needed JMS resources could not be created.
   */
  protected void doConnect() throws JMSException {
    super.doConnect();
    producer = session.createProducer(dest);
  }
  
  /**
   * Opens a XA connection with the foreign JMS server and creates the XA JMS
   * resources for interacting with the foreign JMS destination.
   * 
   * @exception JMSException
   *              If the needed JMS resources could not be created.
   */
  protected void doXAConnect() throws JMSException {
    super.doXAConnect();
    producer = session.createProducer(dest);
  }

}
