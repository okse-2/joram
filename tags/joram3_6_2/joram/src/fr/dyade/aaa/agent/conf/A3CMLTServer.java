/*
 * Copyright (C) 2002-2003 ScalAgent Distributed Technologies 
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
 */
package fr.dyade.aaa.agent.conf;

import java.io.*;
import java.util.*;
import fr.dyade.aaa.agent.*;

/**
 * The class <code>A3CMLTServer</code> describes a transient agent server.
 */
public class A3CMLTServer extends A3CMLServer implements Serializable {
  public A3CMLTServer(short sid,
                      String name,
                      String hostname,
                      short gateway) throws Exception {
    super(sid, name, hostname);
    this.gateway = gateway;
//     A3CMLNetwork network = new A3CMLNetwork("transient", -1);
//     networks.addElement(network);    
  }

  public A3CMLTServer duplicate() throws Exception {
    A3CMLTServer clone = new A3CMLTServer(sid,
                                          name, 
                                          hostname,
                                          gateway);
    // super class
    clone.visited = visited;
    if (networks != null) {
      for (Enumeration n = networks.elements(); n.hasMoreElements(); )
        clone.networks.addElement(
          ((A3CMLNetwork) n.nextElement()).duplicate());
    }
    if (services != null) {
      for (Enumeration e = services.elements(); e.hasMoreElements(); )
        clone.services.addElement((A3CMLService) e.nextElement());
    }
    if (properties != null) {
      for (Enumeration e = properties.keys(); e.hasMoreElements(); ) {
        Short sid = (Short) e.nextElement();
        clone.properties.put(sid, properties.get(sid));
      }
    }
    if (nat != null) {
      for (Enumeration e = nat.elements(); e.hasMoreElements(); )
        clone.addNat(((A3CMLNat) e.nextElement()).duplicate());
    }
    clone.jvmArgs = jvmArgs;
    return clone;
  }

  public String toString() {
    return super.toString();
  }

  public boolean equals(Object obj) {
    return super.equals(obj);
  }
}
