
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

import org.objectweb.joram.shared.excepts.*;
import org.objectweb.joram.shared.messages.MessageTracing;
import org.objectweb.util.monolog.api.BasicLevel;

import java.io.*;
import java.util.*;

/** 
 * The <code>MessageBody</code> class actually provides the mom message ref.
 * <p>
 */
public class MessageBody implements Cloneable, Serializable {

  /** The message type (SIMPLE, TEXT, OBJECT, MAP, STREAM, BYTES). */
  transient private int type;

  /** The bytes body. */
  transient private byte[] body_bytes = null;
  /** The map body. */
  transient private HashMap body_map = null;
  /** The text body. */
  transient private String body_text = null;
  /** body is saved */
  transient public boolean saved = false;

  /**
   * Constructs a <code>MessageBody</code> instance.
   */
  public MessageBody() {}

  void setType(int type) {
    this.type = type;
  }

  /** get bytes body. */
  public byte[] getBodyBytes() {
    return body_bytes;
  }
  
  /** get map body. */
  public HashMap getBodyMap() {
    return body_map;
  }

  /** get text body. */
  public String getBodyText() {
    return body_text;
  }
  
  /** set bytes body. */
  public void setBodyBytes(byte[] bytes) {
    body_bytes = bytes;
  }
  
  /** set map body. */
  public void setBodyMap(HashMap map) {
    body_map = map;
  }

  /** set text body. */
  public void setBodyText(String text) {
    body_text = text;
  }
  
  /** Clones the MessageBody. */
  public Object clone() {
      if (MessageTracing.dbgMessage.isLoggable(BasicLevel.DEBUG))
        MessageTracing.dbgMessage.log(BasicLevel.DEBUG,
                                      "MessageBody.clone()");
    try {
      MessageBody clone = (MessageBody) super.clone();
      if (getBodyMap() != null) {
        clone.setBodyMap(new HashMap());
        if (getBodyMap().keySet() != null) {
          for (Iterator it = getBodyMap().keySet().iterator(); it.hasNext(); ) {
            Object key = it.next();
            clone.getBodyMap().put(key,getBodyMap().get(key));
          }
        }
      }
      return clone;
    } catch (CloneNotSupportedException cE) {
      return null;
    }
  }

  private static void writeString(ObjectOutputStream os,
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

  private static String readString(ObjectInputStream is) throws IOException {
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

  private void writeObject(ObjectOutputStream os) 
    throws IOException {
    os.writeInt(type);
    if (type == MessageType.TEXT) {
      writeString(os, body_text);
    } else if ((type == MessageType.OBJECT) ||
               (type == MessageType.STREAM) ||
               (type == MessageType.BYTES)) {
      if (body_bytes == null) {
        os.writeInt(-1);
      } else {
        os.writeInt(body_bytes.length);
        os.write(body_bytes);
      }
    } else if (type == MessageType.MAP) {
      os.writeObject(body_map);
    }
  }

  private void readObject(ObjectInputStream is)
    throws IOException, ClassNotFoundException{
    type = is.readInt();
    if (type == MessageType.TEXT) {
      body_text = readString(is);
    } else if ((type == MessageType.OBJECT) ||
               (type == MessageType.STREAM) ||
               (type == MessageType.BYTES)) {
      int length = is.readInt();
      if (length ==  -1) {
        body_bytes = null;
      } else {
        body_bytes = new byte[length];
        is.readFully(body_bytes);
      }
    } else if (type == MessageType.MAP) {
      body_map = (HashMap) is.readObject();
    }
  }

  public String toString() {
    StringBuffer buff = new StringBuffer();
    buff.append('(');
    buff.append(super.toString());
    buff.append(",type=");
    buff.append(type);
    buff.append(",body_map=");
    buff.append(body_map);
    buff.append(",body_text=");
    buff.append(body_text);
    buff.append(",body_bytes=");
    buff.append(body_bytes);
    buff.append(",saved=");
    buff.append(saved);
    buff.append(')');
    return buff.toString();
  }
}
