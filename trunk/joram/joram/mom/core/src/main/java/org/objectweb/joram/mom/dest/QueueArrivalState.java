/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2013 ScalAgent Distributed Technologies
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

import java.io.IOException;
import java.io.Serializable;

import org.objectweb.joram.mom.util.JoramHelper;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.common.Debug;
import fr.dyade.aaa.common.encoding.Decoder;
import fr.dyade.aaa.common.encoding.Encodable;
import fr.dyade.aaa.common.encoding.EncodableFactory;
import fr.dyade.aaa.common.encoding.Encoder;

public class QueueArrivalState implements Encodable, Serializable {
  
  public static Logger logger = Debug.getLogger(QueueArrivalState.class.getName());
  
  public static QueueArrivalState load(String id) throws Exception {
    // TODO: as we know the type, the method 'loadByteArray' would be more efficient
    QueueArrivalState res = (QueueArrivalState) AgentServer.getTransaction().load(id);
    res.id = id;
    res.modified = false;
    return res;
  }
  
  private String id;
  
  private long arrivalCount;
  
  private boolean modified;
  
  public QueueArrivalState() {}
  
  public QueueArrivalState(String id) {
    this.id = id;
    arrivalCount = 0;
    modified = true;
  }
  
  public long getAndIncrementArrivalCount(boolean persistent) {
    if (persistent) {
      modified = true;
    }
    return arrivalCount++;
  }
  
  /**
   * Saves this state if modified.
   * @throws Exception
   */
  public void save() throws IOException {
    if (modified) {
      // Calls 'save' and not 'saveByteArray' in order to enable lazy encoding
      // (and potentially 'delete') when reactions are grouped.
      AgentServer.getTransaction().save(this, id);
      modified = false;
    }
  }

  public void delete() {
    AgentServer.getTransaction().delete(id);
  }

  public int getEncodableClassId() {
    return JoramHelper.QUEUE_ARRIVAL_STATE_CLASS_ID;
  }

  public int getEncodedSize() throws Exception {
    return 8;
  }

  public void encode(Encoder encoder) throws Exception {
    encoder.encode64(arrivalCount);
  }

  public void decode(Decoder decoder) throws Exception {
    arrivalCount = decoder.decode64();
  }
  
  public static class Factory implements EncodableFactory {

    public Encodable createEncodable() {
      return new QueueArrivalState();
    }

  }

}