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
import java.io.*;

public class BindRequest extends JndiRequest {

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
}
