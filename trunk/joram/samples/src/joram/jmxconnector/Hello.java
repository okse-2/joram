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
 * Contributor(s): 
 */
package jmxconnector;

import java.lang.management.*;
import javax.management.*;

public class Hello implements HelloMBean, NotificationBroadcaster, NotificationEmitter {
  private String msg;
  
  public String getMsg() {
    return msg;
  }

  public void setMsg(String msg) {
    this.msg = msg;
  }

  private int value;

  public int getValue() {
    return value;
  }
  
  public void setValue(int value) {
    this.value = value;
  }
  
  public String saidHello() {
    return "hello:" + msg;
  }

  public void notifyHello() {
    broadcaster.sendNotification(new Notification("Hello.notifyHello",    // type
                                                  this,                   // source
                                                  ++notificationSequence, // seq. number
                                                  "Hello " + getMsg()));  // message

  }

  // broadcaster support class
  private NotificationBroadcasterSupport broadcaster = new NotificationBroadcasterSupport();

  // sequence number for notifications
  private long notificationSequence = 0;

  // notification broadcaster implementation

  public void addNotificationListener(NotificationListener listener,
                                      NotificationFilter filter,
                                      Object handback) {
    broadcaster.addNotificationListener(listener, filter, handback);
  }

  public void removeNotificationListener(NotificationListener listener) throws ListenerNotFoundException {
    broadcaster.removeNotificationListener(listener);
  }

  public void removeNotificationListener(NotificationListener listener,
                                         NotificationFilter filter,
                                         Object handback) throws ListenerNotFoundException {
    broadcaster.removeNotificationListener(listener, filter, handback);
  }

  public MBeanNotificationInfo[] getNotificationInfo() {
    return new MBeanNotificationInfo[] {
                                        new MBeanNotificationInfo(new String[] { "Hello.notifyHello" },
                                                                  Notification.class.getName(),
                                                                  "Hello Notifications.")
                                        };
  }

  public static void main(String[] args) throws Exception {
    // Get the platform MBeanServer
    MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
    
    // Unique identification of MBeans
    Hello helloBean = new Hello();
    mbs.registerMBean(helloBean, new ObjectName("SimpleAgent:name=hello"));
    
//    // Create an RMI connector and start it
//    JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:9999/server");
//    JMXConnectorServer cs = JMXConnectorServerFactory.newJMXConnectorServer(url, null, mbs);
//    cs.start();
    
    System.out.println("hit CR to notify!");
    System.in.read();
    
    for (int i=0; i<10; i++) {
      helloBean.notifyHello();
      Thread.sleep(1000L);
    }
    
    System.out.println("hit CR to exit!");
    System.in.read();    
    
    System.exit(0);
  }
}
