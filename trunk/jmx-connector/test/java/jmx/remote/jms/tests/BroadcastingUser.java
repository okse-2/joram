/**
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

package jmx.remote.jms.tests;

import java.lang.management.*;
import javax.management.*;


class User {
  String name;

  public String getName() {
    return name;
  }
}

public class BroadcastingUser extends User implements BroadcastingUserMBean, NotificationBroadcaster,
    NotificationEmitter {

  BroadcastingUser(String name) {
    this.name = name;
  }

  // broadcaster support class
  private NotificationBroadcasterSupport broadcaster = new NotificationBroadcasterSupport();

  // sequence number for notifications
  private long notificationSequence = 0;

  // management operations
  public void remove() {

    broadcaster.sendNotification(new Notification("user.remove", // type
        this, // source
        ++notificationSequence, // seq. number
        "User " + getName() + " removed." // message
    ));
  }

  // notification broadcaster implementation

  public void addNotificationListener(NotificationListener listener, NotificationFilter filter,
      Object handback) {

    broadcaster.addNotificationListener(listener, filter, handback);
  }

  public void removeNotificationListener(NotificationListener listener) throws ListenerNotFoundException {

    broadcaster.removeNotificationListener(listener);
  }

  public MBeanNotificationInfo[] getNotificationInfo() {
    return new MBeanNotificationInfo[] { new MBeanNotificationInfo(new String[] { "user.remove" }, // notif.
                                                                                                   // types
        Notification.class.getName(), // notif. class
        "User Notifications." // description
    ) };
  }

  public static void main(String args[]) throws Exception {
    MBeanServer server = ManagementFactory.getPlatformMBeanServer();

    // create the listener and filter instances
    UserListener listener = new UserListener(server);
    UserFilter filter = new UserFilter();

    ObjectName john = new ObjectName("user:name=John");
    server.registerMBean(new BroadcastingUser("john"), john);
    ObjectName mike = new ObjectName("user:name=Mike");
    server.registerMBean(new BroadcastingUser("mike"), mike);
    ObjectName xena = new ObjectName("user:name=Xena");
    server.registerMBean(new BroadcastingUser("xena"), xena);

    server.addNotificationListener(john, listener, filter, "john");
    server.addNotificationListener(mike, listener, filter, "mike");
    server.addNotificationListener(xena, listener, filter, "xena");

    server.registerMBean(listener, new ObjectName("user:name=Listener"));

    System.in.read();
  }

  public void removeNotificationListener(NotificationListener listener, NotificationFilter filter,
      Object handback) throws ListenerNotFoundException {
    broadcaster.removeNotificationListener(listener, filter, handback);
  }

}

class UserListener implements NotificationListener, UserListenerMBean {
  MBeanServer server = null;

  UserListener(MBeanServer server) {
    this.server = server;
  }

  public void handleNotification(Notification notif, Object handback) {
    String type = notif.getType();

    if (type.equals("user.remove")) {
      try {
        System.out.println(notif.getMessage());
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}

//
// Notification filter implementation.
//
class UserFilter implements NotificationFilter {
  public boolean isNotificationEnabled(Notification n) {
    return (n.getType().equals("user.remove")) ? true : false;
  }
}
