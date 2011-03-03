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

public class ListReply extends JndiReply {
  
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private NameClassPair[] pairs;

  public ListReply(NameClassPair[] pairs) {
    this.pairs = pairs;
  }

  public final NamingEnumeration getEnumeration() {
    return new NamingEnumerationImpl(pairs);
  }

  private static class NamingEnumerationImpl 
      implements NamingEnumeration {
    private NameClassPair[] pairs;
    private int index;

    public NamingEnumerationImpl(NameClassPair[] pairs) {
      this.pairs = pairs;
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
      return index < pairs.length;
    }

    public Object nextElement() {
      if (index < pairs.length) {
        return pairs[index++];
      }
      throw new java.util.NoSuchElementException();
    }
  }
}
