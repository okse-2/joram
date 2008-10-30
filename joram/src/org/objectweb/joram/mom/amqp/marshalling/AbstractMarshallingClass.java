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
import java.io.Externalizable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.OutputStream;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.util.Debug;


public abstract class AbstractMarshallingClass implements Externalizable, Streamable {
  public static Logger logger = Debug.getLogger(AbstractMarshallingClass.class.getName());
  
  protected final static int NULL_CLASS_ID = -1;
  protected int classId;
  static protected String className;
  
  protected abstract int getClassId();
  protected abstract String getClassName();
  
  protected abstract java.lang.String getMethodName(int id);

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
  
  /** ***** ***** ***** ***** ***** ***** ***** *****
   * Externalizable interface
   * ***** ***** ***** ***** ***** ***** ***** ***** */

  public final void writeExternal(ObjectOutput out) throws IOException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,  "AbstractMarshallingClass.writeExternal: " + out);
    writeTo((DataOutputStream) out);
  }

  public final void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,  "AbstractMarshallingClass.readExternal: " + in);
    readFrom((DataInputStream)in);
  }

  /** ***** ***** ***** ***** ***** ***** ***** *****
   * Streamable interface
   * ***** ***** ***** ***** ***** ***** ***** ***** */

  static public void write(AbstractMarshallingClass marshallingClass,
                           OutputStream os) throws IOException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,  "AbstractMarshallingClass.write: " + marshallingClass);

    DataOutputStream out = new DataOutputStream(os);
    if (marshallingClass == null) {
      AMQPStreamUtil.writeShort(NULL_CLASS_ID, out);
    } else {
      AMQPStreamUtil.writeShort(marshallingClass.getClassId(), out);
      marshallingClass.writeTo(out);
    }
  }

  static public AbstractMarshallingClass read(InputStream is) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
    int classid = -1;
    AbstractMarshallingClass marshallingClass = null;

    classid = AMQPStreamUtil.readShort(new DataInputStream(is));
    if (classid != NULL_CLASS_ID) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG,  "AbstractMarshallingClass read Class : " + getClassName(classid));
      marshallingClass = (AbstractMarshallingClass) Class.forName(getClassName(classid)).newInstance();
      marshallingClass.readFrom((DataInputStream) is);
    }

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "AbstractMarshallingClass.read: " + marshallingClass);

    return marshallingClass;
  }
}
