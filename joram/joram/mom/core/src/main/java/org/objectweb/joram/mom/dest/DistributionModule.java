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
package org.objectweb.joram.mom.dest;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import org.objectweb.joram.mom.notifications.ClientMessages;
import org.objectweb.joram.mom.util.DMQManager;
import org.objectweb.joram.shared.MessageErrorConstants;
import org.objectweb.joram.shared.excepts.MessageValueException;
import org.objectweb.joram.shared.excepts.RequestException;
import org.objectweb.joram.shared.messages.ConversionHelper;
import org.objectweb.joram.shared.messages.Message;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Debug;

public class DistributionModule implements Serializable {

  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  public static Logger logger = Debug.getLogger(DistributionModule.class.getName());

  /** The property name for the distribution handler class name. */
  public static final String CLASS_NAME = "distribution.className";

  /** Keep message property name: tells if distributed message is kept in destination. */
  public static final String KEEP_MESSAGE_OPTION = "distribution.keep";

  private static Properties transform(fr.dyade.aaa.common.stream.Properties properties) {
    if (properties == null)
      return null;
    Properties prop = new Properties();
    Enumeration e = properties.keys();
    while (e.hasMoreElements()) {
      String key = (String) e.nextElement();
      prop.put(key, properties.get(key));
    }
    return prop;
  }

  /** Tells if distributed message is kept in destination */
  private boolean keep = false;

  /** Holds the distribution logic. */
  private DistributionHandler distributionHandler;

  /** The distribution queue or topic using this module. */
  private final Destination destination;

  public DistributionModule(Destination destination, Properties properties) throws RequestException {
    this.destination = destination;
    setProperties(properties);
  }

  /**
   * Resets the distribution properties.
   */
  private void setProperties(Properties props) throws RequestException {

    if (props == null) {
      return;
    }

    // reset default
    keep = false;

    if (props.containsKey(KEEP_MESSAGE_OPTION)) {
      try {
        keep = ConversionHelper.toBoolean(props.get(KEEP_MESSAGE_OPTION));
      } catch (MessageValueException exc) {
        logger.log(BasicLevel.ERROR, "DistributionModule: can't parse keep message option.", exc);
      }
      props.remove(KEEP_MESSAGE_OPTION);
    }

    if (props.containsKey(CLASS_NAME)) {
      try {
        String className = ConversionHelper.toString(props.get(CLASS_NAME));
        props.remove(CLASS_NAME);

        Class clazz = Class.forName(className);
        distributionHandler = (DistributionHandler) clazz.newInstance();
        distributionHandler.init(props);

      } catch (Exception exc) {
        logger.log(BasicLevel.ERROR, "DistributionModule: can't create distribution handler.", exc);
        throw new RequestException(exc.getMessage());
      }
    } else if (distributionHandler == null) {
      throw new RequestException("Distribution class not defined: use " + CLASS_NAME
          + " property to chose distribution class.");
    }

  }

  /**
   * Messages received on the distribution destination are distributed using the
   * specified {@link DistributionHandler}.
   */
  public ClientMessages processMessages(ClientMessages cm) {

    List msgs = cm.getMessages();
    DMQManager dmqManager = null;
    for (int i = 0; i < msgs.size(); i++) {
      Message msg = (Message) msgs.get(i);
      try {
        setProperties(transform(msg.properties));
        distributionHandler.distribute(msg);
      } catch (Exception exc) {
        logger.log(BasicLevel.ERROR, "DistributionModule: distribution error.", exc);
        if (dmqManager == null) {
          dmqManager = new DMQManager(cm.getDMQId(), destination.getDMQAgentId(), destination.getId());
        }
        dmqManager.addDeadMessage(msg, MessageErrorConstants.UNDELIVERABLE);
      }
    }
    if (dmqManager != null) {
      dmqManager.sendToDMQ();
    }

    if (keep) {
      return cm;
    }
    return null;
  }

  public void close() {
    distributionHandler.close();
  }

}
