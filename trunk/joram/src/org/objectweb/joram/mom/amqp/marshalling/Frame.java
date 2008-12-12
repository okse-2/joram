/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 ScalAgent Distributed Technologies
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
package org.objectweb.joram.mom.amqp.marshalling;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.objectweb.joram.shared.stream.StreamUtil;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.util.Debug;

/**
 * Represents an AMQP wire-protocol frame: 
 * type, channel, size, payload, end.
 */
public class Frame {
  
  public static Logger logger = Debug.getLogger(Frame.class.getName());
  
  /** type code AMQP.FRAME_XXX constants */
  private int type;
  
  /** channel number, 0-65535 */
  private int channel;
  
  /** payload bytes */
  private byte[] payload;
 
  public Frame(int type, int channel) {
    this.type = type;
    this.channel = channel;
    this.payload = null;
  }
  
  public Frame(int type, int channel, byte[] payload) {
    this.type = type;
    this.channel = channel;
    this.payload = payload;
  }

  public static Frame readFrom(InputStream in) throws IOException {
    int type = StreamUtil.readUnsignedByteFrom(in);
    //int empty = AMQPStreamUtil.readInt(in);// empty spec: 0.8
    int channel = StreamUtil.readShortFrom(in);
    byte[] payload = StreamUtil.readByteArrayFrom(in);
    int frameEndMarker = StreamUtil.readUnsignedByteFrom(in);
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
          "type = " + type + ", channel = " + channel + 
          ", payload = " + payload + ", frameEndMarker = " + frameEndMarker);
    if (frameEndMarker != AMQP.FRAME_END) {
      throw new MalformedFrameException("Bad frame end marker: " + frameEndMarker);
    }
    return new Frame(type, channel, payload);
  }

  public static void writeTo(Frame frame, OutputStream out) throws IOException {
    StreamUtil.writeTo((byte) frame.getType(), out);
    //AMQPStreamUtil.writeInt(-1, out); // empty spec: 0.8
    StreamUtil.writeTo((short) frame.getChannel(), out);
    StreamUtil.writeTo(frame.getPayload(), out);
    StreamUtil.writeTo((byte) AMQP.FRAME_END, out);
  }

  /**
   * @return the channel
   */
  public int getChannel() {
    return channel;
  }

  /**
   * @param channel the channel to set
   */
  public void setChannel(int channel) {
    this.channel = channel;
  }

  /**
   * @return the payload
   */
  public byte[] getPayload() {
    return payload;
  }

  /**
   * @param payload the payload to set
   */
  public void setPayload(byte[] payload) {
    this.payload = payload;
  }

  /**
   * @return the type
   */
  public int getType() {
    return type;
  }

  /**
   * @param type the type to set
   */
  public void setType(int type) {
    this.type = type;
  }

  public String toString() {
    StringBuffer buff = new StringBuffer();
    buff.append("Frame(");
    buff.append("type=");
    buff.append(type);
    buff.append(",channel=");
    buff.append(channel);
    buff.append(",payloadSize=");
    if (payload != null)
      buff.append(payload.length);
    else
      buff.append("-1");
    buff.append(')');
    return buff.toString();
  }
}
