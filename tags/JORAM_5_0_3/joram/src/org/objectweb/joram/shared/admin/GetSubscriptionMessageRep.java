/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - 2006 ScalAgent Distributed Technologies
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
package org.objectweb.joram.shared.admin;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.objectweb.joram.shared.messages.Message;

public class GetSubscriptionMessageRep extends AdminReply {
  private static final long serialVersionUID = -7547698179282063264L;

  private transient Message msg;

  public GetSubscriptionMessageRep(Message msg) {
    super(true, null);
    this.msg = msg;
  }

  public final Message getMessage() {
    return msg;
  }

  /** ***** ***** ***** ***** ***** ***** ***** *****
   * Serializable interface
   * ***** ***** ***** ***** ***** ***** ***** ***** */

  private void writeObject(ObjectOutputStream out) throws IOException {
    msg.writeTo(out);
  }

  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    msg = new Message();
    msg.readFrom(in);
  }
}
