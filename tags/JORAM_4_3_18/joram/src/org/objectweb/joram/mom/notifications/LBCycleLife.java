/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - France Telecom R&D
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
 * Initial developer(s): Nicolas Tachker (ScalAgent)
 * Contributor(s):
 */
package org.objectweb.joram.mom.notifications;

import org.objectweb.joram.mom.notifications.ClientMessages;
import java.util.Hashtable;
import java.util.Vector;

public class LBCycleLife extends QueueClusterNot {

  private ClientMessages clientMessages;
  private Hashtable visitTable;

  public LBCycleLife(float rateOfFlow) {
    super(rateOfFlow);
    visitTable = new Hashtable();
  }
  
  public void setClientMessages(ClientMessages clientMessages) {
    this.clientMessages = clientMessages;
  }

  public ClientMessages getClientMessages() {
    return clientMessages;
  }

  public void putInVisitTable(String msgId,
                              Vector visit) {
    visitTable.put(msgId,visit);
  }

  public Hashtable getVisitTable() {
    return visitTable;
  }

  public String toString() {
    StringBuffer str = new StringBuffer();
    str.append("LBCycleLife (");
    str.append(super.toString());
    str.append(", clientMessages=");
    str.append(clientMessages);
    str.append(", visitTable");
    str.append(visitTable);
    str.append(")");
    return str.toString();
  }
}
