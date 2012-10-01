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
package org.ow2.joram.mom.amqp.marshalling;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;
import org.ow2.joram.mom.amqp.exceptions.FrameErrorException;

import fr.dyade.aaa.common.Debug;
import fr.dyade.aaa.common.stream.StreamUtil;

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

  public static Frame readFrom(InputStream in) throws IOException, FrameErrorException {
    int type = StreamUtil.readUnsignedByteFrom(in);
    int channel = StreamUtil.readShortFrom(in);
    byte[] payload = StreamUtil.readByteArrayFrom(in);
    int frameEndMarker = StreamUtil.readUnsignedByteFrom(in);

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "type = " + type + ", channel = " + channel + ", payload = " + payload
          + ", frameEndMarker = " + frameEndMarker);
    if (frameEndMarker != AMQP.FRAME_END) {
      throw new FrameErrorException("Bad frame end marker: " + frameEndMarker);
    }
    return new Frame(type, channel, payload);
  }

  public static void writeTo(Frame frame, OutputStream out, int maxBodySize) throws IOException {
    if (maxBodySize == 0 || frame.getPayload() == null) {
      StreamUtil.writeTo((byte) frame.getType(), out);
      StreamUtil.writeTo((short) frame.getChannel(), out);
      if (frame.getPayload() == null) {
        StreamUtil.writeTo(0, out);
      } else {
        StreamUtil.writeTo(frame.getPayload(), out);
      }
      StreamUtil.writeTo((byte) AMQP.FRAME_END, out);
    } else {
      int copied = 0;
      while (copied < frame.getPayload().length) {
        int length = Math.min(frame.getPayload().length - copied, maxBodySize);
        byte[] array = new byte[length];
        System.arraycopy(frame.getPayload(), copied, array, 0, length);
        StreamUtil.writeTo((byte) frame.getType(), out);
        StreamUtil.writeTo((short) frame.getChannel(), out);
        StreamUtil.writeTo(array, out);
        StreamUtil.writeTo((byte) AMQP.FRAME_END, out);
        copied += length;
        out.flush();
      }
    }
  }

  /**
   * @return the channel
   */
  public int getChannel() {
    return channel;
  }

  /**
   * @return the payload
   */
  public byte[] getPayload() {
    return payload;
  }

  /**
   * @return the type
   */
  public int getType() {
    return type;
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
