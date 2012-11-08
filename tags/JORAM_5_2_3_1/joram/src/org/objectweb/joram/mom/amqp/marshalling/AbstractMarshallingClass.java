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

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.util.Debug;

public abstract class AbstractMarshallingClass {
  
  public static Logger logger = Debug.getLogger(AbstractMarshallingClass.class.getName());
  
  protected final static int NULL_CLASS_ID = -1;
  
  protected int classId;
  
  protected abstract int getClassId();
  
  protected abstract String getClassName();
  
  protected abstract String getMethodName(int id);

  /**
   * Constructs an <code>AbstractMarshallingClass</code>.
   */
  public AbstractMarshallingClass() {
    classId = getClassId();
  }
  
  private static int getPosition(int id) {
    for (int i = 0; i < AMQP.ids.length; i++) {
      if (AMQP.ids[i] == id)
        return i;
    }
    return -1;
  }
  
  private static String getClassName(int id) {
    int pos = getPosition(id);
    if (pos < 0)
      return "";
    return AMQP.classnames[pos];  
  }

  static public AbstractMarshallingClass read(AMQPInputStream is) throws IOException, ClassNotFoundException,
      InstantiationException, IllegalAccessException {
    int classid = -1;
    AbstractMarshallingClass marshallingClass = null;

    classid = is.readShort();
    if (classid != NULL_CLASS_ID) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG,  "AbstractMarshallingClass read Class : " + getClassName(classid));
      marshallingClass = (AbstractMarshallingClass) Class.forName(getClassName(classid)).newInstance();
    }

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "AbstractMarshallingClass.read: " + marshallingClass);

    return marshallingClass;
  }
}
