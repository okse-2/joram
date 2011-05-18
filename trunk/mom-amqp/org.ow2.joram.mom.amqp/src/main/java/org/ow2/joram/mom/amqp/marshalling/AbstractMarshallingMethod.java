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
import java.io.Serializable;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;
import org.ow2.joram.mom.amqp.exceptions.FrameErrorException;
import org.ow2.joram.mom.amqp.exceptions.SyntaxErrorException;

import fr.dyade.aaa.common.Debug;

public abstract class AbstractMarshallingMethod implements Serializable {
  
  private static final long serialVersionUID = 1L;

  public static Logger logger = Debug.getLogger(AbstractMarshallingMethod.class.getName());

  public int channelNumber;
  public abstract int getClassId();
  public abstract String getClassName();
  public abstract int getMethodId();
  public abstract String getMethodName();
  public abstract void writeTo(AMQPOutputStream os) throws IOException;
  public abstract void readFrom(AMQPInputStream is) throws IOException, SyntaxErrorException;

  /**
   * Constructs an <code>AbstractMarshallingMethod</code>.
   */
  public AbstractMarshallingMethod() {
  }

  public static AbstractMarshallingMethod read(byte[] payload) throws IOException, FrameErrorException {
    AMQPInputStream in = new AMQPInputStream(new ByteArrayInputStream(payload));
    
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "AbstractMarshallingMethod.readFrom: " + in);

    AbstractMarshallingMethod marshallingMethod = null;
    AbstractMarshallingClass marshallingClass = AbstractMarshallingClass.read(in);
    int methodid = in.readShort();
    try {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG,"AbstractMarshallingMethod read Method class : " + marshallingClass.getMethodName(methodid) + ", id = " + methodid);
      marshallingMethod = (AbstractMarshallingMethod) Class.forName(
          marshallingClass.getMethodName(methodid)).newInstance();
      marshallingMethod.readFrom(in);
    } catch (Exception e) {
      if (logger.isLoggable(BasicLevel.WARN))
        logger.log(BasicLevel.WARN, "AbstractMarshallingMethod read error :: ", e);
      throw new FrameErrorException("Error instantiating method id: " + methodid);
    }
    return marshallingMethod;
  }
  
  public Frame toFrame() throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "AbstractMarshallingMethod.write(" + this + ", " + bos + ')');

    AMQPOutputStream stream = new AMQPOutputStream(bos);
    stream.writeShort(getClassId());
    stream.writeShort(getMethodId());
    writeTo(stream);
    return new Frame(AMQP.FRAME_METHOD, channelNumber, bos.toByteArray());
  }
  
}
