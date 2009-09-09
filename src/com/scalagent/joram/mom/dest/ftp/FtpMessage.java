/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2007 - 2008 ScalAgent Distributed Technologies
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
 * An FtpMessage encapsulates a proprietary message which is also used
 * for effective MOM transport facility.
 */
public class FtpMessage {
  private Message sharedMsg;

  /**
   * Instantiates an <code>FtpMessage</code> wrapping a consumed
   * shared simple message.
   *
   * @param sharedMsg  The shared message to wrap.
   */
  public FtpMessage(Message sharedMsg) {
    this.sharedMsg = sharedMsg;
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
    Message cloneShared = (Message) sharedMsg.clone();
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

  public String toString() {
    if (sharedMsg != null)
      return sharedMsg.toString();
    else 
      return null;
  }
}
