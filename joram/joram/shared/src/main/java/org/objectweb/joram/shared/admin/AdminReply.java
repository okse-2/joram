/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2011 ScalAgent Distributed Technologies
 * Copyright (C) 2004 France Telecom R&D
 * Copyright (C) 1996 - 2000 Dyade
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
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s): ScalAgent Distributed Technologies
 */
package org.objectweb.joram.shared.admin;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import fr.dyade.aaa.common.stream.StreamUtil;

/**
 * An <code>AdminReply</code> is a reply sent by a
 * <code>org.objectweb.joram.mom.dest.AdminTopic</code> topic and containing data or
 * information destinated to a client administrator.
 */
public class AdminReply extends AbstractAdminMessage {
  /** Define serialVersionUID for interoperability. */
  private static final long serialVersionUID = 1L;

  public final static int NAME_ALREADY_USED = 0;

  public final static int START_FAILURE = 1;

  public final static int SERVER_ID_ALREADY_USED = 2;

  public final static int UNKNOWN_SERVER = 3;

  public final static int PERMISSION_DENIED = 4;
  
  public final static int UNKNOWN_REQUEST = 5;
  
  public final static int NAME_UNKNOWN = 6;
  
  public final static int BAD_CLUSTER_REQUEST = 7;
  
  public final static int UNKNOWN_DESTINATION = 8;
  
  public final static int ILLEGAL_STATE = 9;
  
  /** <code>true</code> if this reply replies to a successful request. */
  private boolean success = false;

  /** Information. */
  private String info;

  /** Code d'erreur */
  private int errorCode = -1;

  /**
   * Constructs an <code>AdminReply</code> instance.
   *
   * @param success  <code>true</code> if this reply replies to a successful
   *          request.
   * @param info  Information to carry.
   */
  public AdminReply(boolean success, String info) {
    this.success = success;
    this.info = info;
  }

  /**
   * Constructs an <code>AdminReply</code> instance for simple error cases.
   *
   * @param success  <code>true</code> if this reply replies to a successful
   *          request.
   * @param errorCode error code defining the type of the error
   * @param info  Information to carry.
   * @param replyObj Object to carry.
   */
  public AdminReply(int errorCode, String info) {
    this.success = false;
    this.errorCode = errorCode;
    this.info = info;
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

  /** Returns the error code. */
  public final int getErrorCode() {
    return errorCode;
  }

  public String toString() {
    return '(' + super.toString() + ",success=" + success + ",info=" + info + ",errorCode=" + errorCode + ')';
  }

  protected int getClassId() {
    return ADMIN_REPLY;
  }

  /* ***** ***** ***** ***** *****
   * Streamable interface
   * ***** ***** ***** ***** ***** */
  
  public void writeTo(OutputStream os) throws IOException {
    StreamUtil.writeTo(success, os);
    StreamUtil.writeTo(info, os);
    StreamUtil.writeTo(errorCode, os);
  }
  
  public void readFrom(InputStream is) throws IOException {
    success = StreamUtil.readBooleanFrom(is);
    info = StreamUtil.readStringFrom(is);
    errorCode = StreamUtil.readIntFrom(is);
  }
}
