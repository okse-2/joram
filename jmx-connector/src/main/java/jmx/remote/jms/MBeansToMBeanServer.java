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

package jmx.remote.jms;

import java.lang.management.ManagementFactory;

import javax.jms.JMSException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

/**
 * In the Class <b>MBeansToMBeanServer</b>, are registered the MBeans in the
 * MBeanServer.
 * 
 * 
 * @author Djamel-Eddine Boumchedda
 * 
 */
public class MBeansToMBeanServer {
  public static void main(String[] args) {
    MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
    // Instantiation of MBeans
    A objetA = new A();
    BroadcastingUser broadcastingUser = new BroadcastingUser("john");

    try {
      MBeansToMBeanServer mBeansToMBeanServer = new MBeansToMBeanServer();
      mBeansToMBeanServer.registerMBeanToMBeanServer(objetA, mbs);
      mBeansToMBeanServer.registerMBeanToMBeanServer(broadcastingUser, mbs);
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  /**
   * <b>registerMBeanToMBeanServer</b> this method registers the MBean object in
   * the MBeanServer with an ObjectName.
   * 
   * @param Object
   * @param MBeanServer
   * @return ObjectName
   * @throws JMSException
   */

  public ObjectName registerMBeanToMBeanServer(Object objectMBean, MBeanServer mbs)
      throws MalformedObjectNameException, NullPointerException {
    ObjectName name;
    name = new ObjectName("SimpleAgent:name=" + objectMBean.getClass().getName());
    try {
      mbs.registerMBean(objectMBean, name);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    System.out.println("-->Registration of class: " + "'" + objectMBean.getClass().getName() + "'"
        + " in the MBeanServeur: " + "'" + mbs.getClass().getName() + "'");
    return name;

  }
}
