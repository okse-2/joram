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

import java.util.Properties;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageFormatException;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.Topic;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.objectweb.joram.client.jms.XidImpl;
import org.objectweb.joram.mom.dest.AcquisitionDaemon;
import org.objectweb.joram.mom.dest.ReliableTransmitter;
import org.objectweb.joram.shared.messages.Message;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Debug;

public class JMSAcquisition extends JMSModule implements AcquisitionDaemon, MessageListener {

  private static final Logger logger = Debug.getLogger(JMSDistribution.class.getName());

  /** Consumer object. */
  protected MessageConsumer consumer;

  /** Selector for filtering messages. */
  protected String selector;

  private ReliableTransmitter transmitter;

  public void start(Properties properties, ReliableTransmitter transmitter) {
    super.init(properties);
    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "<init>(" + properties + ')');
    }
    this.transmitter = transmitter;
    selector = properties.getProperty("selector");
  }

  public void stop() {
    super.close();
    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "close()");
    }
    unsetMessageListener();
  }

  /**
   * Sets a message listener on the foreign JMS destination.
   * 
   * @exception javax.jms.IllegalStateException
   *              If the module state does not allow to set a listener.
   */
  protected void connectionDone() {

    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "setMessageListener()");
    }

    try {
      try {
        if (dest instanceof Queue) {
          consumer = session.createConsumer(dest, selector);
        } else {
          consumer = session.createDurableSubscriber((Topic) dest, "JMSAcquisition", selector, false);
        }
        if (logger.isLoggable(BasicLevel.DEBUG)) {
          logger.log(BasicLevel.DEBUG, "setConsumer: consumer=" + consumer);
        }

      } catch (JMSException exc) {
        throw exc;
      } catch (Exception exc) {
        throw new JMSException("JMS resources do not allow to create consumer: " + exc);
      }
      consumer.setMessageListener(this);
      cnx.start();
    } catch (JMSException exc) {
    }
  }

  /**
   * Unsets the set message listener on the foreign JMS destination.
   */
  private void unsetMessageListener() {
    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "unsetMessageListener()");
    }

    try {
      cnx.stop();
      consumer.setMessageListener(null);
      if (logger.isLoggable(BasicLevel.DEBUG)) {
        logger.log(BasicLevel.DEBUG, "unsetConsumer()");
      }
      try {
        if (dest instanceof Topic) {
          session.unsubscribe("JMSAcquisition");
        }

        consumer.close();
      } catch (Exception exc) {
      }

      consumer = null;
    } catch (JMSException exc) {
    }
  }

  /**
   * Implements the {@link MessageListener} interface for processing the
   * asynchronous deliveries coming from the foreign JMS server.
   */
  public void onMessage(javax.jms.Message jmsMessage) {
    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "onMessage(" + jmsMessage + ')');
    }
    try {
      Xid xid = null;
      synchronized (lock) {
        try {
          if (isXA) {
            xid = new XidImpl(new byte[0], 1, Long.toString(System.currentTimeMillis()).getBytes());
            if (logger.isLoggable(BasicLevel.DEBUG)) {
              logger.log(BasicLevel.DEBUG, "onMessage: xid=" + xid);
            }

            try {
              xaRes.start(xid, XAResource.TMNOFLAGS);
            } catch (XAException e) {
              if (logger.isLoggable(BasicLevel.WARN)) {
                logger.log(BasicLevel.WARN, "Exception onMessage:: XA can't start resource : " + xaRes,
                    e);
              }
            }
          }
          org.objectweb.joram.client.jms.Message clientMessage = org.objectweb.joram.client.jms.Message
              .convertJMSMessage(jmsMessage);
          Message momMessage = clientMessage.getMomMsg();

          transmitter.transmit(momMessage, jmsMessage.getJMSMessageID());

          if (isXA) {
            try {
              xaRes.end(xid, XAResource.TMSUCCESS);
              if (logger.isLoggable(BasicLevel.DEBUG)) {
                logger.log(BasicLevel.DEBUG, "onMessage: XA end " + xaRes);
              }
            } catch (XAException e) {
              if (logger.isLoggable(BasicLevel.DEBUG)) {
                logger.log(BasicLevel.DEBUG, "Exception onMessage:: XA resource end(...) failed: "
                    + xaRes, e);
              }
              throw new JMSException("onMessage: XA resource end(...) failed: " + xaRes + " :: "
                  + e.getMessage());
            }
            try {
              int ret = xaRes.prepare(xid);
              if (ret == XAResource.XA_OK) {
                xaRes.commit(xid, false);
              }
              if (logger.isLoggable(BasicLevel.DEBUG)) {
                logger.log(BasicLevel.DEBUG, "onMessage: XA commit " + xaRes);
              }
            } catch (XAException e) {
              if (logger.isLoggable(BasicLevel.DEBUG)) {
                logger.log(BasicLevel.DEBUG, "Exception onMessage:: XA resource rollback(" + xid + ")", e);
              }
              try {
                xaRes.rollback(xid);
                if (logger.isLoggable(BasicLevel.DEBUG)) {
                  logger.log(BasicLevel.DEBUG, "onMessage: XA rollback " + xaRes);
                }
              } catch (XAException e1) {
              }
              throw new JMSException("onMessage: XA resource rollback(" + xid + ") failed: " + xaRes
                  + " :: " + e.getMessage());
            }

          } else {
            if (logger.isLoggable(BasicLevel.DEBUG)) {
              logger.log(BasicLevel.DEBUG, "onMessage: commit.");
            }
            session.commit();
          }

        } catch (MessageFormatException conversionExc) {
          // Conversion error: denying the message.
          if (isXA) {
            try {
              xaRes.rollback(xid);
              if (logger.isLoggable(BasicLevel.DEBUG)) {
                logger.log(BasicLevel.DEBUG, "run: XA rollback " + xaRes);
              }
            } catch (XAException e1) {
            }
          } else {
            session.rollback();
            if (logger.isLoggable(BasicLevel.DEBUG)) {
              logger.log(BasicLevel.DEBUG, "Exception:: onMessage: rollback.");
            }
          }
        }
      }
    } catch (JMSException exc) {
      // Commit or rollback failed: nothing to do.
    }
  }

}
