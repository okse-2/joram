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
package org.objectweb.joram.mom.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

import org.objectweb.joram.mom.proxies.UserAgentArrivalState;

import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.common.encoding.Decoder;
import fr.dyade.aaa.common.encoding.Encodable;
import fr.dyade.aaa.common.encoding.EncodableFactory;
import fr.dyade.aaa.common.encoding.EncodableHelper;
import fr.dyade.aaa.common.encoding.Encoder;

public class MessageIdListImpl implements MessageIdList, Encodable,
    Serializable {

  private String listId;

  private ArrayList<String> list;
  
  public MessageIdListImpl() {}

  public MessageIdListImpl(String listId) {
    this.listId = listId;
    list = new ArrayList<String>();
  }

  void setListId(String listId) {
    this.listId = listId;
  }

  public int size() {
    return list.size();
  }

  public String[] toArray(String[] array) {
    return list.toArray(array);
  }

  public boolean contains(String msgId) {
    return list.contains(msgId);
  }

  /**
   * The indicator 'persistent' is ignored because the
   * list is stored as a whole.
   */
  public void add(String msgId, boolean persistent) {
    list.add(msgId);
  }

  public boolean isEmpty() {
    return list.isEmpty();
  }

  public String remove(int index) {
    return list.remove(index);
  }

  public String get(int index) {
    return list.get(index);
  }

  public void remove(String msgId) {
    list.remove(msgId);
  }

  /**
   * The indicator 'persistent' is ignored because the
   * list is stored as a whole.
   */
  public void add(int index, String msgId, boolean persistent) {
    list.add(index, msgId);
  }

  public Iterator<String> iterator() {
    return list.iterator();
  }

  public void clear() {
    list.clear();
  }

  public void save() throws Exception {
    // Calls 'save' and not 'saveByteArray' in order to enable lazy encoding
    // (and potentially 'delete') when reactions are grouped.
    AgentServer.getTransaction().save(this, listId);
  }

  public void delete() {
    AgentServer.getTransaction().delete(listId);
  }

  public int getEncodableClassId() {
    return JoramHelper.MESSAGE_ID_LIST_IMPL_CLASS_ID;
  }

  public int getEncodedSize() throws Exception {
    int res = 4;
    for (String str : list) {
      res += EncodableHelper.getStringEncodedSize(str);
    }
    return res;
  }

  public void encode(Encoder encoder) throws Exception {
    encoder.encode32(list.size());
    for (String str : list) {
      encoder.encodeString(str);
    }
  }

  public void decode(Decoder decoder) throws Exception {
    int listSize = decoder.decode32();
    list = new ArrayList<String>(listSize);
    for (int i = 0; i < listSize; i++) {
      list.add(decoder.decodeString());
    }
  }
  
  public static class MessageIdListImplEncodableFactory implements EncodableFactory {

    public Encodable createEncodable() {
      return new MessageIdListImpl();
    }

  }

}
