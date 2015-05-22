/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2006 ScalAgent Distributed Technologies
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
package org.objectweb.joram.mom.notifications;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.common.encoding.Decoder;
import fr.dyade.aaa.common.encoding.EncodableHelper;
import fr.dyade.aaa.common.encoding.Encoder;

/**
 * A <code>ReceiveRequest</code> instance is used by a client agent for 
 * requesting a message on a queue.
 */
public class ReceiveRequest extends AbstractRequestNot {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  /**
   * String selector for filtering messages, null or empty for no selection.
   */
  private String selector;
  /**
   * The time-to-live value in milliseconds, during which a receive request
   * is valid.
   */
  private long timeOut;
  /** The expiration time of the request. */
  private long expirationTime;
  /**
   * If <code>true</code>, the consumed message will be immediately
   * deleted on the queue.
   */
  private boolean autoAck;
  /**
   * Identifier of the client requesting a message, set by the queue if
   * storing the request.
   */
  public fr.dyade.aaa.agent.AgentId requester;

  private String[] msgIds;

  private int msgCount;
  
  /**
   * Used by the encoding framework.
   */
  public ReceiveRequest() {}

  /**
   * Constructs a <code>ReceiveRequest</code> instance.
   *
   * @param clientContext  Identifies a client context.
   * @param requestId  Request identifier.
   * @param selector  Selector expression for filtering messages, null or empty
   *          for no selection.
   * @param timeOut  Time-to-live value. For immediate delivery, should be set
   *          to 0. For infinite time-to-live, should be negative.
   * @param autoAck  <code>true</code> for immediately acknowledging the
   *          delivered message on the queue, <code>false</code> otherwise.
   */
  public ReceiveRequest(int clientContext, 
                        int requestId, 
                        String selector,
                        long timeOut, 
                        boolean autoAck,
                        String[] msgIds,
                        int msgCount) {
    super(clientContext, requestId);
    this.selector = selector;
    this.timeOut = timeOut;
    this.autoAck = autoAck;
    this.msgIds = msgIds;
    this.msgCount = msgCount;
  }


  /** Returns the selector of the request. */
  public String getSelector() {
    return selector;
  }

  /**
   * Returns the time-to-live parameter of this request, in milliseconds (0 for
   * immediate delivery, negative for infinite validity).
   */
  public long getTimeOut() {
    return timeOut;
  }

  /** Checks the autoAck mode of this request. */
  public boolean getAutoAck() {
    return autoAck;
  }

  public final String[] getMessageIds() {
    return msgIds;
  }

  /**
   * Updates the expiration time field, if needed.
   * This method calculate the expiration time of the request from the
   * current time (1st argument) and the timeout attribute.
   *
   * @param startTime	The starting time to calculate the expiration time.
   */
  public void setExpiration(long startTime) {
    if (timeOut > 0)
      this.expirationTime = startTime + timeOut;
  }

  /**
   * Returns <code>false</code> if the request expired.
   *
   * @param currentTime	The current time to verify the expiration time.
   */
  public boolean isValid(long currentTime) {
    if (timeOut > 0)
      return currentTime < expirationTime;
    return true;
  }

  public final int getMessageCount() {
    return msgCount;
  }
  
  public int getClassId() {
    // Not defined
    return -1;
  }
  
  public int getEncodedSize() throws Exception {
    int encodedSize = super.getEncodedSize();
    encodedSize += BOOLEAN_ENCODED_SIZE + LONG_ENCODED_SIZE + INT_ENCODED_SIZE
        + BOOLEAN_ENCODED_SIZE;
    if (msgIds != null) {
      encodedSize += INT_ENCODED_SIZE;
      for (String msgId : msgIds) {
        encodedSize += EncodableHelper.getStringEncodedSize(msgId);
      }
    }
    encodedSize += BOOLEAN_ENCODED_SIZE;
    if (requester != null) {
      encodedSize += requester.getEncodedSize();
    }
    encodedSize += BOOLEAN_ENCODED_SIZE;
    if (selector != null) {
      encodedSize += EncodableHelper.getStringEncodedSize(selector);
    }
    encodedSize += LONG_ENCODED_SIZE;
    return encodedSize;
  }
  
  public void encode(Encoder encoder) throws Exception {
    super.encode(encoder);
    encoder.encodeBoolean(autoAck);
    encoder.encodeUnsignedLong(expirationTime);
    encoder.encodeUnsignedInt(msgCount);
    if (msgIds == null) {
      encoder.encodeBoolean(true);
    } else {
      encoder.encodeBoolean(false);
      encoder.encodeUnsignedInt(msgIds.length);
      for (String msgId : msgIds) {
        encoder.encodeString(msgId);
      }
    }
    if (requester == null) {
      encoder.encodeBoolean(true);
    } else {
      encoder.encodeBoolean(false);
      requester.encode(encoder);
    }
    if (selector == null) {
      encoder.encodeBoolean(true);
    } else {
      encoder.encodeBoolean(false);
      encoder.encodeString(selector);
    }
    encoder.encodeUnsignedLong(timeOut);
  }

  public void decode(Decoder decoder) throws Exception {
    super.decode(decoder);
    autoAck = decoder.decodeBoolean();
    expirationTime = decoder.decodeUnsignedLong();
    msgCount = decoder.decodeUnsignedInt();
    boolean isNull = decoder.decodeBoolean();
    if (isNull) {
      msgIds = null;
    } else {
      int msgIdsLength = decoder.decodeUnsignedInt();
      msgIds = new String[msgIdsLength];
      for (int i = 0; i < msgIdsLength; i++) {
        msgIds[i] = decoder.decodeString();
      }
    }
    isNull = decoder.decodeBoolean();
    if (isNull) {
      requester = null;
    } else {
      requester = new AgentId((short) 0, (short) 0, 0); 
      requester.decode(decoder);
    }
    isNull = decoder.decodeBoolean();
    if (isNull) {
      selector = null;
    } else {
      selector = decoder.decodeString();
    }
    timeOut = decoder.decodeUnsignedLong();
  }
  
} 
