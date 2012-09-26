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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import org.objectweb.joram.shared.messages.ConversionHelper;
import org.objectweb.joram.shared.messages.Message;

public class MonitoringAcquisition implements AcquisitionHandler {

  /** The various elements to monitor. */
  private Vector elements = new Vector();

  public void retrieve(ReliableTransmitter transmitter) throws Exception {
    Message message = new Message();
    MonitoringHelper.getJMXValues(message, elements);
    List list = new ArrayList(1);
    list.add(message);
    transmitter.transmit(list, null);
  }

  public void setProperties(Properties properties) {
    if (properties != null) {
      elements.clear();
      Enumeration e = properties.keys();
      while (e.hasMoreElements()) {
        String name = (String) e.nextElement();
        String attributes = ConversionHelper.toString(properties.get(name));
        elements.add(new MonitoringElement(name, attributes));
      }
    }

  }

  public void close() {
    // Nothing to do
  }
  
}
