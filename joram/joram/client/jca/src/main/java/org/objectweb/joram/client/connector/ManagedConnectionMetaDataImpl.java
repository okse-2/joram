/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - Bull SA
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
 * Initial developer(s): Frederic Maistre (Bull SA)
 * Contributor(s): Nicolas Tachker (Bull SA)
 */
package org.objectweb.joram.client.connector;

import javax.resource.ResourceException;

import org.objectweb.joram.client.jms.ConnectionMetaData;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Debug;

/**
 * A <code>ManagedConnectionMetaDataImpl</code> instance provides information
 * related to a managed connection.
 */
public class ManagedConnectionMetaDataImpl
             implements javax.resource.spi.ManagedConnectionMetaData {
  
  public static Logger logger = Debug.getLogger(ManagedConnectionMetaDataImpl.class.getName());
  
  /** Name of the user associated with the managed connection. */
  private String userName;

  /**
   * Constructs a <code>ManagedConnectionMetaDataImpl</code> instance.
   */
  public ManagedConnectionMetaDataImpl(String userName) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "ManagedConnectionMetaDataImpl(" + userName + ")");

    this.userName = userName;
  }

  /** Returns JORAM's name. */
  public String getEISProductName() throws ResourceException {
    return ConnectionMetaData.providerName;
  }

  /** Returns the current JORAM release number. */
  public String getEISProductVersion() throws ResourceException {
    return ConnectionMetaData.providerVersion;
  }

  /** Returns 0 as JORAM as no upper limit of active connections. */
  public int getMaxConnections() throws ResourceException {
    return 0;
  }

  /** Returns the name of the user associated with the managed connection. */
  public String getUserName() throws ResourceException {
    return userName;
  }
}
