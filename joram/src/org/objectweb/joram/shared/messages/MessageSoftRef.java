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

import java.io.*;
import java.util.*;
import java.lang.ref.SoftReference;
import org.objectweb.joram.mom.util.MessagePersistenceModule;
import org.objectweb.joram.shared.messages.MessageTracing;
import org.objectweb.util.monolog.api.BasicLevel;

/** 
 *
 */
public class MessageSoftRef extends Message
  implements Cloneable, Serializable {

  transient SoftReference softRef = null;

  /**
   * Constructs a <code>MessageSoftRef</code> instance.
   */
  public MessageSoftRef() {
    this.type = MessageType.SIMPLE;

    if (MessageTracing.dbgMessage.isLoggable(BasicLevel.DEBUG))
      MessageTracing.dbgMessage.log(BasicLevel.DEBUG,
                                    "MessageSoftRef <init>");
  }

  public void setPin(boolean pin) {
    if (MessageTracing.dbgMessage.isLoggable(BasicLevel.DEBUG))
      MessageTracing.dbgMessage.log(BasicLevel.DEBUG,
                                    "MessageSoftRef.setPin(" + pin + ')');

    super.setPin(pin);
    if (isPin() && ! noBody) {
      if (softRef != null)
        body = (MessageBody) softRef.get();
      if (body == null)
        body = loadMessageBody();
    }
  }

  public MessageBody getMessageBody() {
    if (MessageTracing.dbgMessage.isLoggable(BasicLevel.DEBUG))
      MessageTracing.dbgMessage.log(BasicLevel.DEBUG,
                                    "MessageSoftRef.getMessageBody() isPin=" + isPin());

    if (isPin()) return body;

    MessageBody mb = null;

    if (softRef != null)
      mb = (MessageBody) softRef.get();
    if (MessageTracing.dbgMessage.isLoggable(BasicLevel.DEBUG))
      MessageTracing.dbgMessage.log(BasicLevel.DEBUG,
                                    "MessageSoftRef.getMessageBody : mb=" + mb);
    if (mb == null) {
      mb = loadMessageBody();
      setMessageBody(mb);
    }
    return mb;
  }

  public void setMessageBody(MessageBody msgBody) {
    if (MessageTracing.dbgMessage.isLoggable(BasicLevel.DEBUG))
      MessageTracing.dbgMessage.log(BasicLevel.DEBUG,
                                    "MessageSoftRef.setMessageBody(" + 
                                    msgBody + ')');
    
    softRef = new SoftReference(msgBody);
    if (isPin()) 
      body = (MessageBody) softRef.get();
    else 
      body = null;
  }

  private MessageBody loadMessageBody() {
    if (MessageTracing.dbgMessage.isLoggable(BasicLevel.DEBUG))
      MessageTracing.dbgMessage.log(BasicLevel.DEBUG,
                                    "MessageSoftRef.loadMessageBody() : saveName=" + getSaveName());
    if (noBody || getSaveName() == null) 
      return null;

    try {
      return MessagePersistenceModule.loadBody(getSaveName());
    } catch (ClassNotFoundException exc) {
      MessageTracing.dbgMessage.log(BasicLevel.ERROR,
                                    "ERROR :: MessageSoftRef.loadMessageBody() : " + getSaveName());
      return null;
    }
  }

  public void save(String id) {
    if (MessageTracing.dbgMessage.isLoggable(BasicLevel.DEBUG))
      MessageTracing.dbgMessage.log(BasicLevel.DEBUG,
                                    "MessageSoftRef.save(" + id + ')');

    if (! getPersistent()) 
      return;

    if (messagePersistent == null)
      messagePersistent = new MessagePersistent(this);

    setSaveName(MessagePersistenceModule.getSaveName(id,messagePersistent));

    if (MessageTracing.dbgMessage.isLoggable(BasicLevel.DEBUG))
      MessageTracing.dbgMessage.log(BasicLevel.DEBUG,
                                    "MessageSoftRef.save : isPin()=" + isPin() +
                                    ", bodyRO=" + bodyRO + ", saveName=" + getSaveName());
    if (!isPin() && bodyRO) {
      MessagePersistenceModule.saveHeader(id,messagePersistent);
    } else {
      // save Message
      messagePersistent.setPin(false);
      String saveName = MessagePersistenceModule.save(id,messagePersistent);
      if (saveName != null) {
        // body unpin and set body to null
        setPin(false);
        body = null;
      }
    }
  }

  public String toString() {
    StringBuffer buff = new StringBuffer();
    buff.append('(');
    buff.append(super.toString());
    buff.append(",softRef=");
    buff.append(softRef);
    buff.append(')');
    return buff.toString();
  }
}
