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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;
import org.ow2.joram.mom.amqp.exceptions.ConnectionException;
import org.ow2.joram.mom.amqp.exceptions.FrameErrorException;
import org.ow2.joram.mom.amqp.marshalling.AMQP.Basic.BasicProperties;

import fr.dyade.aaa.common.Debug;
import fr.dyade.aaa.common.stream.StreamUtil;

public class MarshallingHeader {
  
  public static Logger logger = Debug.getLogger(MarshallingHeader.class.getName());

  private int classId = -1;
  private long bodySize = -1;
  
  private BasicProperties basicProperties = null;
  
  public int getClassId() {
    return classId;
  }

  /**
   * @return the bodySize
   */
  public long getBodySize() {
    return bodySize;
  }

  /**
   * @return the basicProperties
   */
  public BasicProperties getBasicProperties() {
    return basicProperties;
  }
  
  private MarshallingHeader() {
  }

  public static MarshallingHeader read(byte[] payload) throws IOException, ConnectionException {
    AMQPInputStream in = new AMQPInputStream(new ByteArrayInputStream(payload));
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "MarshallingHeader.read(" + in + ')');

    MarshallingHeader marshallingHeader = new MarshallingHeader();
    int classid = in.readShort();
    if (classid != AMQP.Basic.INDEX) {
      throw new FrameErrorException("The class-id MUST match the method frame class id.");
    }
    marshallingHeader.classId = classid;
    int weight = in.readShort();
    if (weight != 0) {
      throw new FrameErrorException("The header weight field must be zero");
    }
    marshallingHeader.bodySize = in.readLonglong();
    marshallingHeader.basicProperties = new BasicProperties();
    marshallingHeader.basicProperties.readFrom(in);
    return marshallingHeader;
  }
  
  public String toString() {
    StringBuffer buff = new StringBuffer();
    buff.append("MarshallingHeader(classId=");
    buff.append(classId);
    buff.append(",bodySize=");
    buff.append(bodySize);
    buff.append(')');
    return buff.toString();
  }

  public static Frame toFrame(long bodySize, BasicProperties basicProperties, int channelNumber)
      throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "MarshallingHeader.createHeaderFrame");

    StreamUtil.writeTo((short) AMQP.Basic.INDEX, bos);
    StreamUtil.writeTo((short) 0, bos); // The weight field is unused and must be zero
    StreamUtil.writeTo(bodySize, bos);
    AMQPOutputStream stream = new AMQPOutputStream(bos);
    basicProperties.writeTo(stream);
    bos.flush();

    return new Frame(AMQP.FRAME_HEADER, channelNumber, bos.toByteArray());
  }
}
