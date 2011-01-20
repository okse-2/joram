/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2010 - 2011 ScalAgent Distributed Technologies
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

import java.util.Enumeration;
import java.util.Properties;

import org.objectweb.joram.mom.notifications.ClientMessages;
import org.objectweb.joram.shared.excepts.RequestException;
import org.objectweb.joram.shared.messages.ConversionHelper;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.common.Debug;

/**
 * The {@link DistributionQueue} class implements the MOM distribution topic
 * behavior, delivering messages via the {@link DistributionModule}.
 */
public class DistributionTopic extends Topic {

  public static Logger logger = Debug.getLogger(DistributionTopic.class.getName());

  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  private transient DistributionModule distributionModule;

  private Properties properties;

  /**
   * Configures a {@link DistributionTopic} instance.
   * 
   * @param properties
   *          The initial set of properties.
   */
  public void setProperties(Properties properties) throws RequestException {
    super.setProperties(properties);

    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "DistributionTopic.<init> prop = " + properties);
    }

    this.properties = (Properties) properties.clone();

    // Check the existence of the distribution class and the presence of a no-arg constructor.
    try {
      String className = ConversionHelper.toString(properties.get(DistributionModule.CLASS_NAME));
      Class.forName(className).getConstructor();
    } catch (Exception exc) {
      logger.log(BasicLevel.ERROR, "DistributionTopic: error with distribution class.", exc);
      throw new RequestException(exc.getMessage());
    }
  }

  public void initialize(boolean firstTime) {
    super.initialize(firstTime);
    if (distributionModule == null) {
      try {
        distributionModule = new DistributionModule(this, (Properties) properties.clone());
      } catch (RequestException exc) {
        // Should not happen as distribution module creation previously succeeded in constructor
        logger.log(BasicLevel.ERROR, "DistributionTopic.initialize prop = " + properties, exc);
      }
    }
  }

  public void agentFinalize(boolean lastTime) {
    super.agentFinalize(lastTime);
    close();
  }

  /**
   * @see DistributionModule#processMessages(ClientMessages)
   * @see Destination#preProcess(AgentId, ClientMessages)
   */
  public ClientMessages preProcess(AgentId from, ClientMessages cm) {
    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "DistributionTopic. preProcess(" + from + ", " + cm + ')');
    }
    return distributionModule.processMessages(cm);
  }

  public String toString() {
    return "DistributionTopic:" + getId().toString();
  }

  private void close() {
    if (distributionModule != null) {
      distributionModule.close();
    }
  }

  /**
   * Update properties configuration, they are processed by the distribution module 
   * @param prop properties to update.
   * @throws Exception
   */
  public void updateProperties(Properties prop) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "DistributionTopic.updateProperties(" + prop + ')');
    }
    super.setProperties(prop);
    // update this.properties
    Enumeration e = prop.keys();
    while (e.hasMoreElements()) {
    	String key = (String) e.nextElement();
    	properties.put(key, prop.get(key));
    }
    // update the module
    distributionModule.updateProperties(properties);
  }
}
