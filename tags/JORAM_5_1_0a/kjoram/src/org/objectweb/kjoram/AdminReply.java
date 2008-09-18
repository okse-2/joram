/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 ScalAgent Distributed Technologies
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
package org.objectweb.kjoram;

import java.io.IOException;

/**
 * An <code>AdminReply</code> is a reply sent by a
 * <code>org.objectweb.joram.mom.dest.AdminTopic</code> topic and containing data or
 * information destinated to a client administrator.
 */
public class AdminReply extends AbstractAdminMessage {

  public final static int NAME_ALREADY_USED = 0;
  public final static int START_FAILURE = 1;
  public final static int SERVER_ID_ALREADY_USED = 2;
  public final static int UNKNOWN_SERVER = 3;
  
  /** <code>true</code> if this reply replies to a successful request. */
  private boolean success = false;

  /** Information. */
  private String info;

  /** Object. */
  private Object replyObj;

  private int errorCode;

  /**
   * Constructs an <code>AdminReply</code> instance.
   *
   * @param success  <code>true</code> if this reply replies to a successful
   *          request.
   * @param info  Information to carry.
   */
  public AdminReply(boolean success, 
                    String info) {
    this(success, -1, info, null);
  }

  /**
   * Constructs an <code>AdminReply</code> instance.
   *
   * @param success  <code>true</code> if this reply replies to a successful
   *          request.
   * @param info  Information to carry.
   * @param replyObj Object to carry.
   */
  public AdminReply(boolean success, 
                    String info,
                    Object replyObj) {
    this(success, -1, info, replyObj);
  }

  /**
   * Constructs an <code>AdminReply</code> instance.
   *
   * @param success  <code>true</code> if this reply replies to a successful
   *          request.
   * @param errorCode error code defining the type of the error
   * @param info  Information to carry.
   * @param replyObj Object to carry.
   */
  public AdminReply(boolean success, 
                    int errorCode,
                    String info,
                    Object replyObj) {
    this.success = success;
    this.errorCode = errorCode;
    this.info = info;
    this.replyObj = replyObj;
  }

  public AdminReply() { }
  
  /**
   * Returns <code>true</code> if this reply replies to a successful request.
   */
  public final boolean succeeded() {
    return success;
  }

  /** Returns the carried info. */
  public final String getInfo() {
    return info;
  }

  /** Returns the carried object. */
  public final Object getReplyObject() {
    return replyObj;
  }

  public final int getErrorCode() {
    return errorCode;
  }

  public void toString(StringBuffer strbuf) {
    strbuf.append('('); 
    strbuf.append(",success=").append(success);
    strbuf.append(",info=").append(info);
    strbuf.append(",errorCode=").append(errorCode); 
    strbuf.append(",replyObj=").append(replyObj).append(')');
  }

  protected int getClassId() {
    return ADMIN_REPLY;
  }

  /* ***** ***** ***** ***** *****
   * Streamable interface
   * ***** ***** ***** ***** ***** */
  
  public void writeTo(OutputXStream os) throws IOException {
    os.writeBoolean(success);
    os.writeString(info);
    os.writeObject(replyObj);
    os.writeInt(errorCode);
  }
  
  public void readFrom(InputXStream is) throws IOException {
    success = is.readBoolean();
    info = is.readString();
    replyObj = is.readObject();
    errorCode = is.readInt();
  }

}
