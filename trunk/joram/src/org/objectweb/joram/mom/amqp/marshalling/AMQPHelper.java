/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2007-2008 ScalAgent Distributed Technologies
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

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.util.Debug;

/**
 * 
 */
public class AMQPHelper {
  public static Logger logger = Debug.getLogger(AMQPHelper.class.getName());
  public final static String joramAMQP = "JORAM_AMQP_" + AMQP.PROTOCOL.MAJOR + "_"+ AMQP.PROTOCOL.MINOR;
 
  public static AbstractMarshallingMethod readMethod(Frame frame) throws IOException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "AMQPHelper.readMethod(" + frame + ')');
    AbstractMarshallingMethod msg = null;
    if (frame.getType() == AMQP.FRAME_METHOD) {
      try {
        msg = AbstractMarshallingMethod.read(frame.getInputStream());
      } catch (ClassNotFoundException e) {
        if (logger.isLoggable(BasicLevel.ERROR))
          logger.log(BasicLevel.ERROR, "EXCEPTION:: AMQPHelper.readMethod", e);
      } catch (InstantiationException e) {
        if (logger.isLoggable(BasicLevel.ERROR))
          logger.log(BasicLevel.ERROR, "EXCEPTION:: AMQPHelper.readMethod", e);
      } catch (IllegalAccessException e) {
        if (logger.isLoggable(BasicLevel.ERROR))
          logger.log(BasicLevel.ERROR, "EXCEPTION:: AMQPHelper.readMethod", e);
      }
    }
    return msg;
  }

  public static MarshallingHeader readContentHeader(Frame frame) throws IOException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "AMQPHelper.readContentHeader(" + frame + ')');
    MarshallingHeader msg = null;
    if (frame.getType() == AMQP.FRAME_HEADER) {
      msg = MarshallingHeader.read(frame.getInputStream());
    }
    return msg;
  }  

  public static MarshallingBody readBody(Frame frame, int bodySize) throws IOException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "AMQPHelper.readBody(" + frame + ", " + bodySize + ')');
    MarshallingBody body = null;
    if (frame.getType() == AMQP.FRAME_BODY) {
      body = MarshallingBody.read(frame.getInputStream(), bodySize);
    }
    return body;
  }

  public static Frame writeMethod(AbstractMarshallingMethod msg, int channelNumber)
      throws IOException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "AMQPHelper.writeMethod(" + msg + ", " + channelNumber + ')');
    Frame frame = new Frame(AMQP.FRAME_METHOD, channelNumber);
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    AbstractMarshallingMethod.write(msg, bos);
    frame.setPayload(bos.toByteArray());
    return frame;
  }
  
  public static Frame writeContentHeader(MarshallingHeader header, int channelNumber) throws IOException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "AMQPHelper.writeContentHeader(" + header + ", " + channelNumber + ')');
    Frame frame = new Frame(AMQP.FRAME_HEADER, channelNumber);
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    MarshallingHeader.write(header, bos);
    frame.setPayload(bos.toByteArray());
    return frame;
  }
   
  public static Frame writeContentBody(MarshallingBody body, int channelNumber) throws IOException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "AMQPHelper.writeContentBody(" + body + ", " + channelNumber + ')');
    Frame frame = new Frame(AMQP.FRAME_BODY, channelNumber);
    //ByteArrayOutputStream bos = new ByteArrayOutputStream();
    //MarshallingBody.write(body, bos);
    frame.setPayload(body.getBinPayload());
    return frame;
  }
  
  public static void writeProtocolHeader(DataOutputStream out) throws IOException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "AMQPHelper.writeProtocolHeader(" + out + ')');
    AMQPStreamUtil.writeByteArray("AMQP".getBytes("US-ASCII"), out);
    AMQPStreamUtil.writeByte(1, out);
    AMQPStreamUtil.writeByte(1, out);
    AMQPStreamUtil.writeByte(AMQP.PROTOCOL.MAJOR, out);
    AMQPStreamUtil.writeByte(AMQP.PROTOCOL.MINOR, out);
  }
  
  public static void readProtocolHeader(DataInputStream in) throws IOException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "AMQPHelper.readProtocolHeader(" + in + ')');
    StringBuffer buff = new StringBuffer();
    char c = (char) AMQPStreamUtil.readByte(in);
    buff.append(c);
    if (c != 'A')
      badProtocolHeader(buff.toString());
    c = (char) AMQPStreamUtil.readByte(in);
    buff.append(c);
    if (c != 'M')
      badProtocolHeader(buff.toString());
    c = (char) AMQPStreamUtil.readByte(in);
    buff.append(c);
    if (c != 'Q')
      badProtocolHeader(buff.toString());
    c = (char) AMQPStreamUtil.readByte(in);
    buff.append(c);
    if (c != 'P')
      badProtocolHeader(buff.toString());
    int i = (int) AMQPStreamUtil.readByte(in);
    buff.append(i);
//    if (i != 1)
//      badProtocolHeader(buff.toString());
    i = (int) AMQPStreamUtil.readByte(in);
    buff.append(i);
//    if (i != 1)
//      badProtocolHeader(buff.toString());
    i = (int) AMQPStreamUtil.readByte(in);
    buff.append(i);
//    if (i != AMQP.PROTOCOL.MAJOR)
//      badProtocolHeader(buff.toString());
    i = (int) AMQPStreamUtil.readByte(in);
    buff.append(i);
//    if (i != AMQP.PROTOCOL.MINOR)
//      badProtocolHeader(buff.toString());
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "AMQPHelper.readProtocolHeader: client protocol = " + buff.toString());
  }
 
  public static void badProtocolHeader(String header) throws IOException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "AMQPHelper.badProtocolHeader(" + header + ')');
    throw new IOException("bad header : " + header);
  }
}
