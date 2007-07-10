/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2007 ScalAgent Distributed Technologies
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
package com.scalagent.joram.mom.dest.ftp;

import java.util.Enumeration;

import org.objectweb.joram.shared.messages.Message;


/**
 * A mail message encapsulates a proprietary message which is also used
 * for effective MOM transport facility.
 */
public class FtpMessage {
  private Message sharedMsg;

  /**
   * Constructs a bright new <code>MailMessage</code>.
   */
  public FtpMessage() {
    sharedMsg = new Message();
  }

  /**
   * Instanciates a <code>MailMessage</code> wrapping a consumed
   * MOM simple message.
   *
   * @param momMsg  The MOM message to wrap.
   */
  public FtpMessage(org.objectweb.joram.shared.messages.Message momMsg) {
    this.sharedMsg = momMsg;
  } 
  
  /**
   * 
   * @return shared message structure
   */
  public Message getSharedMessage() {
    return sharedMsg;
  }
  
  /**
   * The message identifier.
   * @return identifier
   */
  public String getIdentifier() {
    return sharedMsg.id;
  }

  /**
   * <code>true</code> if the message could not be written on the dest.
   * @param notWriteable
   */
  public void setNotWriteable(boolean notWriteable) {
    sharedMsg.notWriteable = notWriteable;
  }

  public String getStringProperty(String key) {
    return (String) sharedMsg.properties.get(key);
  }

  public long getLongProperty(String key) {
    return ((Long) sharedMsg.properties.get(key)).longValue();
  }

  public boolean getBooleanProperty(String key) {
    return ((Boolean) sharedMsg.properties.get(key)).booleanValue();
  }

  public Object getObjectProperty(String key) {
    return sharedMsg.properties.get(key);
  }
  
  public Object clone() {
    Message cloneShared = null;
    return new FtpMessage(cloneShared);
  }

  public void clearProperties() {
    sharedMsg.properties.clear();
  }

  public Enumeration getPropertyNames() {
    return sharedMsg.properties.keys();
  }

  public void setObjectProperty(String key, Object value) {
    sharedMsg.setProperty(key, value);
  }

  public void setStringProperty(String key, String value) {
    sharedMsg.setProperty(key, value);
  }

  public boolean propertyExists(String key) {
    return sharedMsg.properties.containsKey(key);
  }

}
