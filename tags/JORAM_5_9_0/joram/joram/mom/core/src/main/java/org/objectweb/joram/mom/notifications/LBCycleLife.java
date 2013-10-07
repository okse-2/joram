/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2005 - 2013 ScalAgent Distributed Technologies
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
 * Initial developer(s): ScalAgent Distributed Technologies
 * Contributor(s):
 */
package org.objectweb.joram.mom.notifications;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class LBCycleLife extends ClusterLBNot {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  private ClientMessages clientMessages;

  private Map visitTable;

  /**
   * 
   * @param rateOfFlow
   */
  public LBCycleLife(float rateOfFlow) {
    super(rateOfFlow);
    visitTable = new Hashtable();
  }
  
  /**
   * 
   * @param clientMessages
   */
  public void setClientMessages(ClientMessages clientMessages) {
    this.clientMessages = clientMessages;
  }

  /**
   * 
   * @return ClientMessages
   */
  public ClientMessages getClientMessages() {
    return clientMessages;
  }

  /**
   * 
   * @param msgId
   * @param visit
   */
  public void putInVisitTable(String msgId, List visit) {
    visitTable.put(msgId,visit);
  }

  /**
   * 
   * @return visitTable
   */
  public Map getVisitTable() {
    return visitTable;
  }

  /**
   * Appends a string image for this object to the StringBuffer parameter.
   *
   * @param output
   *	buffer to fill in
   * @return <code>output</code> buffer is returned
   */
  public StringBuffer toString(StringBuffer output) {
    output.append('(');
    super.toString(output);
    output.append(", clientMessages=").append((clientMessages==null)?0:clientMessages.getMessageCount());
//    output.append(", visitTable=").append(visitTable);
    output.append(')');

    return output;
  }
}
