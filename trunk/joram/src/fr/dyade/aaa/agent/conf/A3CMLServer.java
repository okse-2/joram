/*
 * Copyright (C) 2001 - 2003 ScalAgent Distributed Technologies 
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

/**
 * The class <code>Server</code> describes an agent server.
 */
public class A3CMLServer implements Serializable {
  public short sid = -1;
  public String name = null;
  public String hostname = null;
  public Vector services = null;
  public String jvmArgs = null;
  public Hashtable properties = null;
  public Hashtable nat = null;
  public Vector networks = null;
  /**
   * True if the server is already visited during configuration phase.
   */
  public boolean visited = false;
  /**
   * For persistent server, Id. of router (1st hop) used to access the
   * server from current node. if -1 the server is not accessible. This
   * value is fixed during configuration phase.
   * For transient server, Id. of proxy used to access this server. This
   * value is statically fixed.
   */
  public short gateway = -1;

  public A3CMLServer(short sid,
                     String name,
                     String hostname) throws Exception {
    this.sid = sid;
    if ((name == null) || (name.length() == 0))
      this.name = "s#" + sid;
    else
      this.name = name;
    this.hostname = hostname;
    this.services = new Vector();
    this.networks = new Vector();
  }

  public void addService(A3CMLService service) {
    services.addElement(service);
  }

  public A3CMLProperty addProperty(A3CMLProperty prop) {
    if (properties == null)
      properties = new Hashtable();
    return (A3CMLProperty) properties.put(prop.name, prop);
  }
  
  public A3CMLProperty removeProperty(String name) {
    if (properties != null)
      return (A3CMLProperty) properties.remove(name);
    return null;
  }
  
  public boolean containsProperty(String name) {
    if (properties != null)
      return properties.containsKey(name);
    return false;
  }

  public A3CMLNat addNat(A3CMLNat natElement) {
    if (nat == null)
      nat = new Hashtable();
    return (A3CMLNat) nat.put(new Short(natElement.sid), natElement);
  }

  public A3CMLNat getNat(short sid) {
    if (nat == null) return null;
    return (A3CMLNat) nat.get(new Short(sid));
  }
  
  public A3CMLNat removeNat(short sid) {
    if (nat != null)
      return (A3CMLNat) nat.remove(new Short(sid));
    return null;
  }
  
  public boolean containsNat(short sid) {
    if (nat != null)
      return nat.containsKey(new Short(sid));
    return false;
  }
  public final String getJvmArgs() {
    if (jvmArgs != null) return jvmArgs;
    return "";
  }

  public final String getServiceArgs(String classname) throws UnknownServiceException {
    if (services != null) {
      for (int i = services.size() -1; i >=0; i--) {
	A3CMLService service = (A3CMLService) services.elementAt(i);
	if (service.classname.equals(classname))
	  return service.args;
      }
    }
    throw new UnknownServiceException("Unknown service \"" + classname +
                                      "\" on server#" + sid);
  }

  public String toString() {
    StringBuffer strBuf = new StringBuffer();
    strBuf.append("(");
    strBuf.append(super.toString());
    strBuf.append(",name=").append(name);
    strBuf.append(",sid=").append(sid);
    strBuf.append(",hostname=").append(hostname);
    strBuf.append(",visited=").append(visited);
    strBuf.append(",networks=").append(networks);
    strBuf.append(",jvmArgs=").append(jvmArgs);
    strBuf.append(",services=").append(services);
    strBuf.append(",properties=").append(properties);
    strBuf.append(",nat=").append(nat);
    strBuf.append(",gateway=").append(gateway);
    strBuf.append(")");
    return strBuf.toString();
  }

  public boolean equals(Object obj) {
    if (obj == null) return false;

    if (obj instanceof A3CMLServer) {
      A3CMLServer server = (A3CMLServer) obj;
      if ((sid == server.sid) &&
          name.equals(server.name) &&
          ((hostname == server.hostname) ||
           ((hostname != null) && hostname.equals(server.hostname))) &&
          services.equals(server.services) &&
          ((jvmArgs == server.jvmArgs) ||
           ((jvmArgs != null) && jvmArgs.equals(server.jvmArgs))) &&
          ((properties == server.properties) ||
           ((properties != null) && properties.equals(server.properties))) &&
          ((nat == server.nat) ||
           ((nat != null) && nat.equals(server.nat))) &&
          networks.equals(server.networks) &&
          (visited == server.visited) &&
          (gateway == server.gateway))
        return true;
    }
    return false;
  }
}
