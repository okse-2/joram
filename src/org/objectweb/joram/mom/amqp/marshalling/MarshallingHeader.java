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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.objectweb.joram.mom.amqp.marshalling.AMQP.Basic.BasicProperties;
import org.objectweb.joram.shared.stream.StreamUtil;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.util.Debug;

public class MarshallingHeader implements FrameBuilder {
  
  public static Logger logger = Debug.getLogger(MarshallingHeader.class.getName());

  protected final static int NULL_CLASS_ID = -1;

  private int classId = -1;
  private long bodySize = -1;
  
  private BasicProperties basicProperties = null;
  
  public int getClassId() {
    return classId;
  }

  /**
   * @param bodySize the bodySize to set
   */
  public void setBodySize(long bodySize) {
    this.bodySize = bodySize;
  }

  /**
   * @param classId
   *          the classId to set
   */
  public void setClassId(int classId) {
    this.classId = classId;
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

  /**
   * @param basicProperties the basicProperties to set
   */
  public void setBasicProperties(BasicProperties basicProperties) {
    this.basicProperties = basicProperties;
  }

  /**
   * Constructs an <code>MarshallingHeader</code>.
   */
  public MarshallingHeader(int classId, long bodySize, AMQP.Basic.BasicProperties basicProperties) {
    this.classId = classId;
    this.bodySize = bodySize;
    this.basicProperties = basicProperties;
  }
  
  public MarshallingHeader() {
  }

  public static MarshallingHeader read(byte[] payload) throws IOException {
    AMQPInputStream in = new AMQPInputStream(new ByteArrayInputStream(payload));
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "MarshallingHeader.read(" + in + ')');

    MarshallingHeader marshallingHeader = new MarshallingHeader();
    int classid = in.readShort();
    if (classid != NULL_CLASS_ID) {
      marshallingHeader.classId = classid;
      in.readShort(); // Read weight : The weight field is unused and must be zero
      marshallingHeader.bodySize = in.readLonglong();
      marshallingHeader.basicProperties = new BasicProperties();
      marshallingHeader.basicProperties.readFrom(in);
    }
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

  public Frame toFrame(int channelNumber) throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "MarshallingHeader.write(" + this + ", " + bos + ')');

    StreamUtil.writeTo((short) getClassId(), bos);
    StreamUtil.writeTo((short) 0, bos); // The weight field is unused and must be zero
    StreamUtil.writeTo(getBodySize(), bos);
    AMQPOutputStream stream = new AMQPOutputStream(bos);
    basicProperties.writeTo(stream);
    bos.flush();

    return new Frame(AMQP.FRAME_HEADER, channelNumber, bos.toByteArray());
  }
}
