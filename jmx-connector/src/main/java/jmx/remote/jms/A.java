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

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

public class A implements AMBean {
  private static final JMXServiceURL MBeanServerConnection = null;
  int a, b;

  public int geta() {
    return a;
  }

  public void seta(int newval) {
    a = newval;
  }

  public int getb() {
    return b;
  }

  public void affiche() {
    System.out.println("Class A");
    System.out.println("valeur de a = " + a);
    System.out.println("valeur de b = " + b);

  }

  public int addValeurs(int a, int b) {
    int res = a + b;
    return res;
  }

  public void main(String args[]) {
    a = 2;
    b = 3;
    A objetA = new A();
    objetA.affiche();
    objetA.geta();

  }

}
