/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
 *
 * The contents of this file are subject to the Joram Public License,
 * as defined by the file JORAM_LICENSE.TXT 
 * 
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License on the Objectweb web site
 * (www.objectweb.org). 
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific terms governing rights and limitations under the License. 
 * 
 * The Original Code is Joram, including the java packages fr.dyade.aaa.agent,
 * fr.dyade.aaa.ip, fr.dyade.aaa.joram, fr.dyade.aaa.mom, and
 * fr.dyade.aaa.util, released May 24, 2000.
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
 *
 * Initial developer(s): Sofiane Chibani
 * Contributor(s): David Feliot, Nicolas Tachker
 */
package fr.dyade.aaa.jndi2.msg;

import javax.naming.*;

public class ListReply extends JndiReply {
  
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
      } else {
        throw new java.util.NoSuchElementException();
      }
    }
  }
}
