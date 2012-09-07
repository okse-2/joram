/*
 * Copyright (C) 2010 - 2012 ScalAgent Distributed Technologies
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
package org.objectweb.joram.mom.dest;

import java.util.Properties;

/**
 * An acquisition handler that does nothing and can be used, for instance to
 * configure an alias acquisition destination.
 */
public class VoidAcquisitionHandler implements AcquisitionHandler {

  /** @see AcquisitionHandler#retrieve(ReliableTransmitter) */
  public void retrieve(ReliableTransmitter transmitter) throws Exception {
    // Nothing to do
  }

  /** @see AcquisitionHandler#setProperties(Properties) */
  public void setProperties(Properties properties) {
    // Nothing to do
  }

  /** @see AcquisitionHandler#close() */
  public void close() {
    // Nothing to do
  }
}
