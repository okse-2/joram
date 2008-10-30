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

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.util.Debug;

public abstract class AbstractMarshallingMethod implements Streamable {
  public static Logger logger = Debug.getLogger(AbstractMarshallingMethod.class.getName());

  public final static int NULL_METHOD_ID = -1;

  public abstract int getClassId();
  public abstract String getClassName();

  public int methodId = -1;
  public java.lang.String methodName;

  public abstract int getMethodId();
  public abstract java.lang.String getMethodName();

  /**
   * Constructs an <code>AbstractMarshallingMethod</code>.
   */
  public AbstractMarshallingMethod() {
    methodId = getMethodId();
  }

  public static void write(AbstractMarshallingMethod msg, OutputStream os)
      throws IOException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "AbstractMarshallingMethod.write(" + msg + ", " + os + ')');
    DataOutputStream out = new DataOutputStream(os);
    AMQPStreamUtil.writeShort(msg.getClassId(), out);
    AMQPStreamUtil.writeShort(msg.getMethodId(), out);
    msg.writeTo(out);
  }

  public static AbstractMarshallingMethod read(InputStream is)
      throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "AbstractMarshallingMethod.readFrom: " + is);

    AbstractMarshallingMethod marshallingMethod = null;
    DataInputStream in = new DataInputStream(is);
    AbstractMarshallingClass marshallingClass = AbstractMarshallingClass.read(in);
    if (marshallingClass != null) {
      int methodid = AMQPStreamUtil.readShort(in);
      if (methodid != NULL_METHOD_ID) {
        try {
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG,"AbstractMarshallingMethod read Method class : " + marshallingClass.getMethodName(methodid) + ", id = " + methodid);
          marshallingMethod = (AbstractMarshallingMethod) Class.forName(
              marshallingClass.getMethodName(methodid)).newInstance();
          marshallingMethod.readFrom(in);
        } catch (InstantiationException e) {
          throw new IOException(e);
        } catch (IllegalAccessException e) {
          throw new IOException(e);
        } catch (ClassNotFoundException e) {
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG,"AbstractMarshallingMethod read :: Class NotFound", e);
          throw new IOException(e);
        }
      }
    }
    return marshallingMethod;
  }
}
