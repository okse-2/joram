/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2007 ScalAgent Distributed Technologies
 * Copyright (C) 1996 - 2000 Dyade
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
 * Initial developer(s): Sofiane Chibani
 * Contributor(s): David Feliot, Nicolas Tachker
 */
package fr.dyade.aaa.jndi2.msg;

import javax.naming.*;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

public class LookupReply extends JndiReply {
  
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public static final Logger logger = fr.dyade.aaa.util.Debug.getLogger(
      LookupReply.class.getName());
  
  private Object obj;

  public LookupReply(Object obj) {
    this.obj = obj;
  }

  public final Object getObject() throws NamingException {
    return resolveObject(obj);
  }

  public final static Object resolveObject(Object obj) throws NamingException {
    if (! (obj instanceof Reference))
      return obj;

    try {
      return javax.naming.spi.NamingManager.getObjectInstance(
        obj, null, null, null);
    } catch (Exception e) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "", e);
      NamingException ne = new NamingException(e.getMessage());
      ne.setRootCause(e);
      throw ne;
    }
  }
}
