/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2005 - ScalAgent Distributed Technologies
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
package org.objectweb.joram.shared.messages;

import java.io.*;
import java.util.*;

/** 
 *
 */
public class MessagePersistent implements Cloneable, Serializable {

  transient Message message;
  transient String msgClassName;
  transient private boolean pin;

  public MessagePersistent(Message message) {
    this.message = message;
  }

  public void setPin(boolean pin) {
    this.pin = pin;
  }

  public boolean getPin() {
    return pin;
  }

  public Message getMessage() {
    return message;
  }

  public MessageBody getMessageBody() {
    return message.getMessageBody();
  }

  public String getIdentifier() {
   return  message.getIdentifier();
  }

  public String getSaveName() {
    return message.getSaveName();
  }

  private static void writeString(ObjectOutput os,
                                  String s) throws IOException {
    if (s == null) {
      os.writeInt(-1);
    } else if (s.length() == 0) {
      os.writeInt(0);
    } else {
      byte[] bout = s.getBytes();
      os.writeInt(bout.length);
      os.write(bout);
    }
  }

  private static String readString(ObjectInput is) throws IOException {
    int length = is.readInt();
    if (length == -1) {
      return null;
    } else if (length == 0) {
      return "";
    } else {
      byte[] bin = new byte[length];
      is.readFully(bin);
      return new String(bin);
    }
  }

  final static byte BOOLEAN = 0;
  final static byte BYTE = 1;
  final static byte DOUBLE = 2;
  final static byte FLOAT = 3;
  final static byte INTEGER = 4;
  final static byte LONG = 5;
  final static byte SHORT = 6;
  final static byte STRING = 7;

  private static void writeProperties(ObjectOutput os,
                                      Hashtable h) 
    throws IOException {
    if (h == null) {
      os.writeInt(0);
      return;
    }

    os.writeInt(h.size());
    for (Enumeration e = h.keys() ; e.hasMoreElements() ;) {
      String key = (String) e.nextElement();
      writeString(os, key);
      Object value = h.get(key);
      if (value instanceof Boolean) {
        os.writeByte(BOOLEAN);
        os.writeBoolean(((Boolean) value).booleanValue());
      } else if (value instanceof Byte) {
        os.writeByte(BYTE);
        os.writeByte(((Byte) value).byteValue());
      } else if (value instanceof Double) {
        os.writeByte(DOUBLE);
        os.writeDouble(((Double) value).doubleValue());
      } else if (value instanceof Float) {
        os.writeByte(FLOAT);
        os.writeFloat(((Float) value).floatValue());
      } else if (value instanceof Integer) {
        os.writeByte(INTEGER);
        os.writeInt(((Integer) value).intValue());
      } else if (value instanceof Long) {
        os.writeByte(LONG);
        os.writeLong(((Long) value).longValue());
       } else if (value instanceof Short) {
        os.writeByte(SHORT);
        os.writeShort(((Short) value).shortValue());
      } else if (value instanceof String) {
        os.writeByte(STRING);
        writeString(os, (String) value);
      }
     }
  }

  private static Hashtable readProperties(ObjectInput is) 
    throws IOException, ClassNotFoundException {
    int size = is.readInt();
    if (size == 0) return null;

    Hashtable h = new Hashtable(size);
    
    for (int i = 0; i < size; i++) {
      String key = readString(is);
      byte type = is.readByte();
      if (type == BOOLEAN) {
        h.put(key, new Boolean(is.readBoolean()));
      } else if (type == BYTE) {
        h.put(key, new Byte(is.readByte()));
      } else if (type == DOUBLE) {
        h.put(key, new Double(is.readDouble()));
      } else if (type == FLOAT) {
        h.put(key, new Float(is.readFloat()));
      } else if (type == INTEGER) {
        h.put(key, new Integer(is.readInt()));
      } else if (type == LONG) {
        h.put(key, new Long(is.readLong()));
      } else if (type == SHORT) {
        h.put(key, new Short(is.readShort()));
      } else if (type == STRING) {
        h.put(key, readString(is));
      }
    }
    return h;
  }

  private void writeObject(ObjectOutputStream os) 
    throws IOException {
    writeString(os, message.getClass().getName());
    os.writeBoolean(pin);
    os.writeInt(message.type);
    os.writeBoolean(message.persistent);
    os.writeBoolean(message.noBody);
    writeString(os, message.id);
    os.writeInt(message.priority);
    os.writeLong(message.expiration);
    os.writeLong(message.timestamp);
    writeString(os, message.toId);
    writeString(os, message.toType);
    writeString(os, message.correlationId);
    writeString(os, message.replyToId);
    writeString(os, message.replyToType);

    os.writeObject(message.optionalHeader);

    os.writeBoolean(message.bodyRO);

    writeProperties(os, message.properties);
    
    os.writeLong(message.order);
    os.writeInt(message.deliveryCount);

    os.writeBoolean(message.denied);
    os.writeBoolean(message.deletedDest);
    os.writeBoolean(message.expired);
    os.writeBoolean(message.notWriteable);
    os.writeBoolean(message.undeliverable);
    writeString(os, message.getSaveName());
    message.bodyRO = true;
  }

  private void readObject(ObjectInputStream is)
    throws Exception {
    // read header part
    msgClassName = readString(is);
    message = (Message) Class.forName(msgClassName).newInstance();
    pin = is.readBoolean();
    message.type = is.readInt();
    message.persistent = is.readBoolean();
    message.noBody = is.readBoolean();
    message.id = readString(is);
    message.priority = is.readInt();
    message.expiration = is.readLong();
    message.timestamp = is.readLong();
    message.toId = readString(is);
    message.toType = readString(is);
    message.correlationId = readString(is);
    message.replyToId = readString(is);
    message.replyToType = readString(is);

    message.optionalHeader = (Hashtable) is.readObject();

    message.bodyRO = is.readBoolean();

    message.properties = readProperties(is);
    
    message.order = is.readLong();
    message.deliveryCount = is.readInt();

    message.denied = is.readBoolean();
    message.deletedDest= is.readBoolean();
    message.expired = is.readBoolean();
    message.notWriteable = is.readBoolean();
    message.undeliverable = is.readBoolean();
    message.setSaveName(readString(is));
    message.acksCounter = 0;
    message.durableAcksCounter = 0;
    message.propertiesRO = true;
  }
  
  /** Clones the MessageBody. */
  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException cE) {
      return null;
    }
  }

  public String toString() {
    StringBuffer buff = new StringBuffer();
    buff.append(super.toString());
    buff.append(",msgClassName=");
    buff.append(msgClassName);
    buff.append(",message.saveName=");
    buff.append(message.getSaveName());
    buff.append(",pin=");
    buff.append(pin);
    buff.append(",message.type=");
    buff.append(message.type);
    buff.append(",message.id=");
    buff.append(message.id);
    buff.append(",message.toId=");
    buff.append(message.toId);
    buff.append(",message.toType=");
    buff.append(message.toType);
    buff.append(",message.replyToId=");
    buff.append(message.replyToId);
    buff.append(",message.replyToType=");
    buff.append(message.replyToType);
    buff.append(",message.body=");
    buff.append(message.getMessageBody());
    buff.append(')');
    return buff.toString();
  }
}
