/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 - 2011 ScalAgent Distributed Technologies
 * Copyright (C) 2008 CNES
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
package org.ow2.joram.mom.amqp;

import java.io.Serializable;

import org.ow2.joram.mom.amqp.marshalling.AMQP;
import org.ow2.joram.mom.amqp.marshalling.AMQP.Basic.BasicProperties;
import org.ow2.joram.mom.amqp.marshalling.AMQP.Basic.Publish;

/**
 * Class used by the {@link AMQPConnectionListener} to hold all the parameters (
 * <code>Basic.Publish</code> method, header and body) necessary to publish a
 * message while they are received on the socket.
 */
public class PublishRequest implements Serializable {
  
  private static final long serialVersionUID = 1L;
  
  // publishing properties
  private AMQP.Basic.Publish publish;
  
  // header
  private BasicProperties header;
  
  // body
  private byte[] body;
  private int bodyRead = 0;

  // channel
  public int channel;
  
  /**
   * set the header content.
   * 
   * @param classId
   * @param weight
   * @param bodySize
   * @param propertyFlags
   * @param propertyList
   */
  public void setHeader(BasicProperties header, long bodySize) {
    this.header = header;
    if (bodySize != 0) {
      body = new byte[(int) bodySize];
    }
  }
  
  public BasicProperties getHeader() {
    return header;
  }

  public void setPublish(Publish publish) {
    this.publish = publish;
  }
  
  public AMQP.Basic.Publish getPublish() {
    return publish;
  }

  public byte[] getBody() {
    return body;
  }

  public boolean appendBody(byte[] bodyPart) {
    System.arraycopy(bodyPart, 0, body, bodyRead, bodyPart.length);
    bodyRead += bodyPart.length;
    return bodyRead == body.length;
  }

}
