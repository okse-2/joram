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

import java.util.Properties;

import org.objectweb.joram.shared.excepts.MessageValueException;
import org.objectweb.joram.shared.messages.ConversionHelper;
import org.objectweb.joram.shared.messages.Message;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Debug;

public class DistributionModule {
  public static Logger logger = Debug.getLogger(DistributionModule.class.getName());

  /** The property name for the distribution handler class name. */
  public static final String CLASS_NAME = "distribution.className";

  /** Holds the distribution logic. */
  private DistributionHandler distributionHandler;
  
  private boolean isAsyncDistribution = false;

  public DistributionModule(String className, Properties properties, boolean firstTime) {
    try {
      distributionHandler = (DistributionHandler) Class.forName(className).newInstance();
    } catch (Exception exc) {
      logger.log(BasicLevel.ERROR, "DistributionModule: can't create distribution handler.", exc);
    }
    setProperties(properties, firstTime);
  }

  /**
   * Resets the distribution properties.
   */
  public void setProperties(Properties properties, boolean firstTime) {
    if (distributionHandler != null) {
      distributionHandler.init(properties, firstTime);
    }
    
    if (properties.containsKey(DistributionQueue.ASYNC_DISTRIBUTION_OPTION)) {
    	try {
    		isAsyncDistribution = ConversionHelper.toBoolean(properties.get(DistributionQueue.ASYNC_DISTRIBUTION_OPTION));
    	} catch (MessageValueException exc) {	}
    }
  }
  
  public void close() {
    distributionHandler.close();
  }

  public void processMessage(Message fullMessage) throws Exception {
  	if (logger.isLoggable(BasicLevel.DEBUG)) {
  		logger.log(BasicLevel.DEBUG, "DistributionModule.processMessage(" + fullMessage.id + ") isAsyncDistribution = " + isAsyncDistribution);
  	}
  	if (!isAsyncDistribution) {
  		distributionHandler.distribute(fullMessage);
  	} else {
  		// the message are send by distributionDaemon.
  		throw new Exception("async distribution is on.");
  	}
  }

  public DistributionHandler getDistributionHandler() {
  	return distributionHandler;
  }
}
