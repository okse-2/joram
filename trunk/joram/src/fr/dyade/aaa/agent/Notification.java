/*
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
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
 * fr.dyade.aaa.util, fr.dyade.aaa.ip, fr.dyade.aaa.mom, and fr.dyade.aaa.joram,
 * released May 24, 2000. 
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
 */
package fr.dyade.aaa.agent;

import java.io.*;

/**
 * Class Notification is the root of the notifications hierarchy. Every
 * notification's class has Notification as a superclass.
 */
public class Notification implements Serializable, Cloneable {
  public static final String RCS_VERSION="@(#)$Id: Notification.java,v 1.12 2002-12-11 11:22:12 maistrfr Exp $";

  static final long serialVersionUID = 3007264908616389613L;

  /**
   * Context of the notification.
   */
  private Object context;

  /**
   * Sets the context of the notification.
   *
   * @param context the context of the notification.
   */
  public final void setContext(Object context) {
    this.context = context;
  }

  /**
   * Returns the context of the notification.
   *
   * @return the context of the notification.
   */
  public final Object getContext() {
    return context;
  }

  /**
   * Returns a clone of this notification.
   *
   * @return  a clone of this notification.
   */
  public synchronized Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) { 
      // this shouldn't happen, since we are Cloneable
      throw new InternalError();
    }
  }

  public String toString() {
    StringBuffer output = new StringBuffer();
    output.append("(");
    output.append(super.toString());
    output.append(",context=");
    output.append(context);
    output.append(")");
    return output.toString();
  }

}
