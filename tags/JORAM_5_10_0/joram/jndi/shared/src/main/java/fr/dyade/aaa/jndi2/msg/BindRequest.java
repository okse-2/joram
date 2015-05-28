/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
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
import java.io.*;

public class BindRequest extends JndiRequest {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private Object obj;

  private boolean rebind;

  public BindRequest(CompositeName name, Object obj) 
    throws NamingException {
    this(name, obj, false);
  }

  public BindRequest(CompositeName name, Object obj, boolean rebind) 
    throws NamingException {
    super(name);
    if (obj == null ||
        obj instanceof byte[] ||
        obj instanceof Reference) {
      this.obj = obj;
    } else if (obj instanceof  Referenceable) {
      this.obj = ((Referenceable)obj).getReference();
    } else {
      this.obj = toReference(obj);
    }
    this.rebind = rebind;
  }

  public final Object getObject() {
    return obj;
  }

  public final boolean isRebind() {
    return rebind;
  }
  
  private static Reference toReference(Object obj) throws NamingException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();    
    try {
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      oos.writeObject(obj);
      byte[] bytes = baos.toByteArray();
      Reference ref = new Reference(
        obj.getClass().getName(),
        new BinaryRefAddr(ObjectFactory.ADDRESS_TYPE, bytes),
        "fr.dyade.aaa.jndi2.msg.ObjectFactory", null);
      return ref;
    } catch (Exception exc) {
      NamingException ne = new NamingException();
      ne.setRootCause(exc);
      throw ne;
    }    
  }

  public String toString() {
    return '(' + super.toString() +
      ",rebind=" + rebind + ')';
  }
}
