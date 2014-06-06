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

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.objectweb.joram.mom.dest.DistributionHandler;
import org.objectweb.joram.shared.messages.Message;

public class DistributionHandlerTest implements DistributionHandler {

  private static List messages = new ArrayList();

  public void close() {
  }

  // distribute the message correctly the fifth time.
  public void distribute(Message msg) throws Exception {
    if (msg.deliveryCount < 5) {
      throw new Exception("Message could not be delivered.");
    }
    msg.setText("test distribution - MODIFIED");
    addMessage(msg);
  }

  public void init(Properties props, boolean FirstTime) {

  }

  private static synchronized void addMessage(Message msg) {
    messages.add(msg.clone());
  }

  public static synchronized List getAllMessages() {
    List result = messages;
    messages = new ArrayList();
    return result;
  }

}
