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

import java.util.*;

public class ListBindingsReply extends JndiReply {
  
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
