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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.objectweb.joram.mom.amqp.marshalling.AMQP.Basic.BasicProperties;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.util.Debug;

public class MarshallingHeader {
  public static Logger logger = Debug.getLogger(MarshallingHeader.class.getName());

  protected final static int NULL_CLASS_ID = -1;

  private int classId = -1;
  private int weight = -1;
  private long bodySize = -1;
  private int propertyFlags = 0;
  private Map propertyList = null;
  
  private String className = null;
  
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
   * @param classId the classId to set
   */
  public void setClassId(int classId) {
    this.classId = classId;
  }
  
  /**
   * @param propertyFlags the propertyFlags to set
   */
  public void setPropertyFlags(int propertyFlags) {
    this.propertyFlags = propertyFlags;
  }

  /**
   * @param propertyList the propertyList to set
   */
  public void setPropertyList(Map propertyList) {
    this.propertyList = propertyList;
  }

  /**
   * @param weight the weight to set
   */
  public void setWeight(int weight) {
    this.weight = weight;
  }

  /**
   * @return the bodySize
   */
  public long getBodySize() {
    return bodySize;
  }

  /**
   * @return the propertyFlags
   */
  public int getPropertyFlags() {
    return propertyFlags;
  }

  /**
   * @return the weight
   */
  public int getWeight() {
    return weight;
  }

  /**
   * @return the propertyList
   */
  public Map getPropertyList() {
    return propertyList;
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
  public MarshallingHeader() {
  }

  public static void write(MarshallingHeader msg, OutputStream os)
      throws IOException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "MarshallingHeader.write(" + msg + ", " + os + ')');
    DataOutputStream out = new DataOutputStream(os);
    AMQPStreamUtil.writeShort(msg.getClassId(), out);
    AMQPStreamUtil.writeShort(msg.getWeight(), out);
    AMQPStreamUtil.writeLonglong(msg.getBodySize(), out);
    msg.basicProperties.writeTo(out);
    out.flush();
  }

  public static MarshallingHeader read(InputStream is)
      throws IOException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "MarshallingHeader.read(" + is + ')');

    MarshallingHeader marshallingHeader = new MarshallingHeader();
    DataInputStream in = new DataInputStream(is);
    int classid = AMQPStreamUtil.readShort(in);
    if (classid != NULL_CLASS_ID) {
      marshallingHeader.classId = classid;
      // just for Basic class.
      marshallingHeader.className = new AMQP.Basic().getMethodName(classid);
      // end just for Basic class.
      marshallingHeader.weight = AMQPStreamUtil.readShort(in);
      marshallingHeader.bodySize = AMQPStreamUtil.readLonglong(in);
      marshallingHeader.basicProperties = new BasicProperties();
      marshallingHeader.basicProperties.readFrom(in);
    }
    return marshallingHeader;
  }
  
  public String toString() {
    StringBuffer buff = new StringBuffer();
    buff.append("MarshallingHeader(classId=");
    buff.append(classId);
    buff.append(",className=");
    buff.append(className);
    buff.append(",weight=");
    buff.append(weight);
    buff.append(",bodySize=");
    buff.append(bodySize);
    buff.append(",propertyFlags=");
    buff.append(propertyFlags);
    buff.append(",propertyList=");
    buff.append(propertyList);
    buff.append(')');
    return buff.toString();
  }
}
