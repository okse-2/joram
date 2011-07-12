/**
 * Copyright (C) 2010 ScalAgent Distributed Technologies
 * All rights reserved.
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
