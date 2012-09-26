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

import java.io.Serializable;
import java.util.Properties;

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

  /** Holds the distribution logic. */
  private DistributionHandler distributionHandler;

  public DistributionModule(String className, Properties properties) {
    try {
      Class clazz = Class.forName(className);
      distributionHandler = (DistributionHandler) clazz.newInstance();
    } catch (Exception exc) {
      logger.log(BasicLevel.ERROR, "DistributionModule: can't create distribution handler.", exc);
    }
    setProperties(properties);
  }

  /**
   * Resets the distribution properties.
   */
  public void setProperties(Properties properties) {
    if (distributionHandler != null) {
      distributionHandler.init(properties);
    }
  }
  
  public void close() {
    distributionHandler.close();
  }

  public void processMessage(Message fullMessage) throws Exception {
    distributionHandler.distribute(fullMessage);
  }

}
