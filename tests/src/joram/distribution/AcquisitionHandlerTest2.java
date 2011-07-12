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
 * Contributor(s): 
 */
package joram.distribution;

import java.util.Properties;

import org.objectweb.joram.mom.dest.AcquisitionHandler;
import org.objectweb.joram.mom.dest.ReliableTransmitter;

public class AcquisitionHandlerTest2 implements AcquisitionHandler {

  public void close() {

  }

  public void retrieve(ReliableTransmitter trsmiter) throws Exception {
    trsmiter.transmit(DistributionHandlerTest2.getAllMessages(), null);
  }

  public void setProperties(Properties arg0) {

  }

}
