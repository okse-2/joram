/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2007 ScalAgent Distributed Technologies
 * Copyright (C) 2007 France Telecom R&D
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
package org.objectweb.joram.client.jms.admin;

import javax.naming.*;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.util.Debug;

/**
 * The <code>ObjectFactory</code> class is used by the naming service
 * for retrieving or re-constructing administered objects.
 */
public class ObjectFactory implements javax.naming.spi.ObjectFactory {

  private static Logger logger = Debug.getLogger(ObjectFactory.class.getName());

  /** Returns an instance of an object given its reference. */
  public Object getObjectInstance(Object obj,
                                  Name name,
                                  Context ctx,
                                  java.util.Hashtable env) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "ObjectFactory.getObjectInstance(" + obj + ',' + name + ',' + ctx + ',' + env + ')');

    Reference ref = (Reference) obj;
    AdministeredObject ao = null;
    try {
      Class clazz = Class.forName(ref.getClassName());
      ao = (AdministeredObject) clazz.newInstance();
      ao.fromReference(ref);
    } catch (Exception exc) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR, "", exc);
    }
    return ao;
  }
}
