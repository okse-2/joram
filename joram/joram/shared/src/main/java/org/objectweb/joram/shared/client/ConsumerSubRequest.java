/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2006 - 2013 ScalAgent Distributed Technologies
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
package org.objectweb.joram.shared.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import fr.dyade.aaa.common.stream.StreamUtil;

/**
 * A <code>ConsumerSubRequest</code> is sent by a constructing
 * <code>MessageConsumer</code> destinated to consume messages on a topic.
 */
public final class ConsumerSubRequest extends AbstractJmsRequest {
  /**
   * 
   */
  private static final long serialVersionUID = 2L;
  /** The subscription's name. */
  private String subName;
  
  /** asynchronous subscription. */
  private boolean asyncSub;

  /** Sets the subscription name. */
  public void setSubName(String subName) {
    this.subName = subName;
  }

  /** Returns the name of the subscription. */
  public String getSubName() {
    return subName;
  }

  /** The selector for filtering messages. */
  private String selector;

  /** Sets the selector. */
  public void setSelector(String selector) {
    this.selector = selector;
  }

  /** Returns the selector for filtering the messages. */
  public String getSelector() {
    return selector;
  }

  /**
   * <code>true</code> if the subscriber does not wish to consume messages
   * produced by its connection.
   */
  private boolean noLocal;

  /** Sets the noLocal attribute. */
  public void setNoLocal(boolean noLocal) {
    this.noLocal = noLocal;
  }

  /** Returns <code>true</code> for not consuming the local messages. */
  public boolean getNoLocal() {
    return noLocal;
  }

  /** <code>true</code> if the subscription is durable. */
  private boolean durable;

  /** Sets the durable attribute. */
  public void setDurable(boolean durable) {
    this.durable = durable;
  }

  /** Returns <code>true</code> for a durable subscription. */
  public boolean getDurable() {
    return durable;
  }

  private String clientID = null;

  /**
   * @return the clientID
   */
  public String getClientID() {
    return clientID;
  }

  /** Returns <code>true</code> for asynchronous subscription. */
  public boolean isAsyncSubscription() {
    return asyncSub;
  }
  
  protected int getClassId() {
    return CONSUMER_SUB_REQUEST;
  }

  /**
   * Constructs a <code>ConsumerSubRequest</code>.
   *
   * @param topic  The topic identifier the client wishes to subscribe to.
   * @param subName  The subscription's name.
   * @param selector  The selector for filtering messages, if any.
   * @param noLocal  <code>true</code> for not consuming the local messages.
   * @param durable  <code>true</code> for a durable subscription.
   * @param asyncSub  <code>true</code> for a asynchronous subscription request.
   * @param clientID The clientID
   */
  public ConsumerSubRequest(String topic, String subName, String selector,
                            boolean noLocal, boolean durable, boolean asyncSub, String clientID) {
    super(topic);
    this.subName = subName;
    this.selector = selector;
    this.noLocal = noLocal;
    this.durable = durable;
    this.asyncSub = asyncSub;
    this.clientID = clientID;
  }

  /**
   * Constructs a <code>ConsumerSubRequest</code>.
   */
  public ConsumerSubRequest() {}

  /* ***** ***** ***** ***** *****
   * Streamable interface
   * ***** ***** ***** ***** ***** */

  /**
   *  The object implements the writeTo method to write its contents to
   * the output stream.
   *
   * @param os the stream to write the object to
   */
  public void writeTo(OutputStream os) throws IOException {
    super.writeTo(os);
    StreamUtil.writeTo(subName, os);
    StreamUtil.writeTo(selector, os);
    StreamUtil.writeTo(noLocal, os);
    StreamUtil.writeTo(durable, os);
    StreamUtil.writeTo(asyncSub, os);
    StreamUtil.writeTo(clientID, os);
  }

  /**
   *  The object implements the readFrom method to restore its contents from
   * the input stream.
   *
   * @param is the stream to read data from in order to restore the object
   */
  public void readFrom(InputStream is) throws IOException {
    super.readFrom(is);
    subName = StreamUtil.readStringFrom(is);
    selector = StreamUtil.readStringFrom(is);
    noLocal = StreamUtil.readBooleanFrom(is);
    durable = StreamUtil.readBooleanFrom(is);
    asyncSub = StreamUtil.readBooleanFrom(is);
    clientID = StreamUtil.readStringFrom(is);
  }
}
