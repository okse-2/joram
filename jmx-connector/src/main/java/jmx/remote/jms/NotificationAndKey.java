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
 * Initial developer(s): Djamel-Eddine Boumchedda
 * 
 */

package jmx.remote.jms;

import java.io.Serializable;

import javax.management.Notification;

/**
 * When a notification is issued by an MBean registered in the MBeanServer we
 * instantiate the object <i>NotificatinoAndKey</i> in the
 * <i>handleNotification</i> method, of the <i>NotificationListener</i>
 * interface passing it a parameters, <i>notification</i> and </i>key<i>, and
 * then object is sent to the client so that it can receive the notification.
 * 
 * 
 * @author Djamel-Eddine Boumchedda
 * 
 */

public class NotificationAndKey implements Serializable {
  Notification notification;
  Object handback;

  public NotificationAndKey(Notification notification, Object handback) {
    this.notification = notification;
    this.handback = handback;

  }

}
