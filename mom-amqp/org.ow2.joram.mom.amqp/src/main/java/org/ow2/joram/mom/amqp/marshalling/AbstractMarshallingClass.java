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

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;
import org.ow2.joram.mom.amqp.exceptions.FrameErrorException;

import fr.dyade.aaa.common.Debug;

public abstract class AbstractMarshallingClass {
  
  public static Logger logger = Debug.getLogger(AbstractMarshallingClass.class.getName());
  
  protected abstract String getMethodName(int id);

  /**
   * Constructs an <code>AbstractMarshallingClass</code>.
   */
  public AbstractMarshallingClass() {
  }
  
  public static AbstractMarshallingClass read(AMQPInputStream is) throws IOException, FrameErrorException {
    int classid = is.readShort();
    AbstractMarshallingClass marshallingClass = AMQP.getAmqpClass(classid);

    if (marshallingClass == null) {
      if (logger.isLoggable(BasicLevel.WARN)) {
        logger.log(BasicLevel.WARN, "Unknown class id: " + classid);
      }
      throw new FrameErrorException("Unknown class id: " + classid);
    }

    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "AbstractMarshallingClass.read: " + marshallingClass);
    }

    return marshallingClass;
  }
}
