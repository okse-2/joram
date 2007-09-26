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

import java.util.*;

public class ListBindingsReply extends JndiReply {
  
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private Binding[] bindings;

  public ListBindingsReply(Binding[] bindings) {
    this.bindings = bindings;
  }

  public final NamingEnumeration getEnumeration() {
    return new NamingEnumerationImpl(bindings);
  }

  public Binding[] getContexts() {
    Vector contexts = new Vector();
    for (int i = 0; i < bindings.length; i++) {
      if (bindings[i].getObject() == null &&
          bindings[i].getClassName().equals(
            Context.class.getName())) {
        contexts.addElement(bindings[i]);              
      }
    }
    Binding[] res = new Binding[contexts.size()];
    contexts.copyInto(res);
    return res;
  }

  public void resolveReferences() throws NamingException {
    for (int i = 0; i < bindings.length; i++) {
      bindings[i].setObject(
        LookupReply.resolveObject(
          bindings[i].getObject()));      
    }
  }

  private static class NamingEnumerationImpl 
      implements NamingEnumeration {
    private Binding[] bindings;
    private int index;

    public NamingEnumerationImpl(Binding[] bindings) {
      this.bindings = bindings;
      this.index = 0;
    }

    public boolean hasMore() throws NamingException {
      return hasMoreElements();
    }

    public Object next() throws NamingException {
      return nextElement();
    }

    public void close() {}

    public boolean hasMoreElements() {
      return index < bindings.length;
    }

    public Object nextElement() {
      if (index < bindings.length) {
        return bindings[index++];
      } else {
        throw new java.util.NoSuchElementException();
      }
    }
  }
}
