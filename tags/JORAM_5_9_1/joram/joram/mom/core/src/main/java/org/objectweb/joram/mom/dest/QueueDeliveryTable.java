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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.objectweb.joram.mom.proxies.UserAgentArrivalState;
import org.objectweb.joram.mom.util.JoramHelper;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.common.Debug;
import fr.dyade.aaa.common.encoding.Decoder;
import fr.dyade.aaa.common.encoding.Encodable;
import fr.dyade.aaa.common.encoding.EncodableFactory;
import fr.dyade.aaa.common.encoding.EncodableHelper;
import fr.dyade.aaa.common.encoding.Encoder;

public class QueueDeliveryTable implements Encodable, Serializable {
  
  public static Logger logger = Debug.getLogger(QueueDeliveryTable.class.getName());
  
  public static QueueDeliveryTable load(String txName) throws Exception {
    // TODO: as we know the type, the method 'loadByteArray' would be more efficient
    QueueDeliveryTable res = (QueueDeliveryTable) AgentServer.getTransaction().load(txName);
    res.txName = txName;
    return res;
  }
  
  private transient String txName;
  
  private HashMap<String, QueueDelivery> deliveries;
  
  private boolean modified;
  
  public QueueDeliveryTable() {}
  
  public QueueDeliveryTable(String txName) {
    this.txName = txName;
    deliveries = new HashMap<String, QueueDelivery>();
    modified = true;
  }
  
  public QueueDelivery get(String msgId) {
    return deliveries.get(msgId);
  }
  
  private void checkModified(QueueDelivery delivery) {
    if (!modified && delivery.getMessage().isPersistent()) {
      modified = true;
    }
  }
  
  public QueueDelivery remove(String msgId) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "QueueDeliveryTable.remove(" + msgId + ')');
    QueueDelivery delivery = deliveries.remove(msgId);
    if (delivery != null) {
      checkModified(delivery);
    }
    return delivery;
  }
  
  public void put(String msgId, QueueDelivery delivery) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "QueueDeliveryTable.put(" + msgId + ')');
    checkModified(delivery);
    deliveries.put(msgId, delivery);
  }
  
  public int size() {
    return deliveries.size();
  }
  
  public Iterator<Entry<String, QueueDelivery>> iterator() {
    return new DeliveryIterator(deliveries.entrySet().iterator());
  }
  
  public void save() throws IOException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "QueueDeliveryTable.save() " + modified);
    if (modified) {
      // Calls 'save' and not 'saveByteArray' in order to enable lazy encoding
      // (and potentially 'delete') when reactions are grouped.
      AgentServer.getTransaction().save(this, txName);
      modified = false;
    }
  }

  public void delete() {
    AgentServer.getTransaction().delete(txName);
  }

  public int getEncodableClassId() {
    return JoramHelper.QUEUE_DELIVERY_TABLE_CLASS_ID;
  }

  public int getEncodedSize() throws Exception {
    int encodedSize = INT_ENCODED_SIZE;
    Iterator<Entry<String, QueueDelivery>> iterator = deliveries.entrySet().iterator();
    while (iterator.hasNext()) {
      Entry<String, QueueDelivery> delivery = iterator.next();
      encodedSize += EncodableHelper.getStringEncodedSize(delivery.getKey());
      encodedSize += delivery.getValue().getEncodedSize();
    }
    return encodedSize;
  }

  /**
   * Do not encode the transient messages.
   */
  public void encode(Encoder encoder) throws Exception {    
    int persistentMessageCount = 0;
    Iterator<Entry<String, QueueDelivery>> iterator = deliveries.entrySet().iterator();
    while (iterator.hasNext()) {
      Entry<String, QueueDelivery> delivery = iterator.next();
      if (delivery.getValue().getMessage() == null) throw new RuntimeException("Null message: " + delivery.getKey());
      if (delivery.getValue().getMessage().isPersistent()) {
        persistentMessageCount++;
      }
    }
    
    encoder.encodeUnsignedInt(persistentMessageCount);
    iterator = deliveries.entrySet().iterator();
    while (iterator.hasNext()) {
      Entry<String, QueueDelivery> delivery = iterator.next();
      if (delivery.getValue().getMessage().isPersistent()) {
        encoder.encodeString(delivery.getKey());
        delivery.getValue().encode(encoder);
      }
    }
  }

  public void decode(Decoder decoder) throws Exception {
    int tableSize = decoder.decodeUnsignedInt();
    deliveries = new HashMap<String, QueueDelivery>(tableSize);
    for (int i = 0; i < tableSize; i++) {
      String key = decoder.decodeString();
      QueueDelivery delivery = new QueueDelivery();
      delivery.decode(decoder);
      deliveries.put(key, delivery);
    }
  }
  
  public static class Factory implements EncodableFactory {

    public Encodable createEncodable() {
      return new QueueDeliveryTable();
    }

  }
  
  class DeliveryIterator implements Iterator<Entry<String, QueueDelivery>> {
    
    private Iterator<Entry<String, QueueDelivery>> delegate;
    
    private Entry<String, QueueDelivery> current;

    public DeliveryIterator(Iterator<Entry<String, QueueDelivery>> delegate) {
      super();
      this.delegate = delegate;
    }

    public boolean hasNext() {
      return delegate.hasNext();
    }

    public Entry<String, QueueDelivery> next() {
      current = delegate.next();
      return current;
    }

    public void remove() {
      checkModified(current.getValue());
      delegate.remove();
    }
    
  }

}
