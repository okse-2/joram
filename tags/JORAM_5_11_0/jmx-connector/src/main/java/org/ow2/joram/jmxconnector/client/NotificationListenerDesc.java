/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2011 ScalAgent Distributed Technologies
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
 */
package org.ow2.joram.jmxconnector.client;

import javax.jms.JMSException;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;

/**
 * <b>AddNotificationListenerStored</b> is the object which is registered in the
 * hashNotificationListener hashMap when a requestor call the
 * subscribeToNotifications method.
 */
public class NotificationListenerDesc {
  ObjectName name;
  NotificationListener listener;
  NotificationFilter filter;
  Object handback;
  Long key;
  
  public NotificationListenerDesc(ObjectName name,
                                  NotificationListener listener,
                                  NotificationFilter filter,
                                  Object handback) {
    this.name = name;
    this.listener = listener;
    this.filter = filter;
    this.handback = handback;
  }

  /**
   * <b>equals</b> is called for compare two object of type
   * AddNotificationListenerStored
   * 
   * @param Object
   * @return boolean
   * @throws JMSException
   */
  public boolean equals(Object object) {
    System.out.println("méthode equals ... ");
    if (object instanceof NotificationListenerDesc) {
      NotificationListenerDesc objectANLStored = (NotificationListenerDesc) object;
      System.out.println("equals " + this.name.equals(objectANLStored.name) + "  "
          + this.listener.equals(objectANLStored.listener));
      if (this.filter == null && this.handback == null) {
        System.out.println("Cas où le handback et le filtrer sont égal à null");
        if (this.name.equals(objectANLStored.name) && (this.listener.equals(objectANLStored.listener))) {
          return true;
        }
      } else if (this.filter == null && this.handback != null) {
        System.out.println("Cas où le handback != null et   filter = null");
        if ((this.name.equals(objectANLStored.name)) && (this.listener.equals(objectANLStored.listener))
            && (this.handback.equals(objectANLStored.handback))) {
          return true;
        }
      } else if (this.filter != null && this.handback == null) {
        System.out.println("Cas où le handback = null et   filter != null");
        if ((this.name.equals(objectANLStored.name)) && (this.listener.equals(objectANLStored.listener))
            && (this.filter.equals(objectANLStored.filter))) {
          return true;
        }
      } else if (this.filter != null && this.handback != null) {
        System.out.println("Cas où le handback != null et   filter != null");
        if ((this.name.equals(objectANLStored.name)) && (this.listener.equals(objectANLStored.listener))
            && (this.handback.equals(objectANLStored.handback))
            && (this.filter.equals(objectANLStored.filter))) {
          return true;
        }
      } else {
        return false;
      }
    }
    return false;
  }

  public int hashCode() {
    // Should compute a specific one.
    int h = 0;
    if (this.handback == null && this.filter == null) {
      h = this.name.hashCode() + this.listener.hashCode();
      System.out.println("HashCode de l'objet : = " + h);
      return h;
    } else if (this.handback != null && this.filter == null) {
      h = this.name.hashCode() + this.listener.hashCode() + this.handback.hashCode();
      System.out.println("HashCode de l'objet : = " + h);
      return h;
    } else if (this.handback == null && this.filter != null) {
      h = this.name.hashCode() + this.listener.hashCode() + this.filter.hashCode();
      System.out.println("HashCode de l'objet : = " + h);
      return h;
    } else if (this.handback != null && this.filter != null) {
      h = this.name.hashCode() + this.listener.hashCode() + this.filter.hashCode() + this.handback.hashCode();
      System.out.println("HashCode de l'objet : = " + h);
      return h;
    }

    return h;

  }

}
