/*
 * Copyright (C) 2001 - 2009 ScalAgent Distributed Technologies 
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

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * The class <code>Server</code> describes an agent server.
 */
public class A3CMLServer implements Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  public short sid = -1;
  public String name = null;
  public String hostname = null;
  /** Domain (1st hop) used to access this server from current node. */
  public String domain = null;
  /**
   * Communication port if the server is directly accessible by the root
   * server.
   */
  public int port = -1;
  public Hashtable nat = null;
  public Vector networks = null;
  public Vector services = null;
  public String jvmArgs = null;
  public Hashtable properties = null;
  /**
   * True if the server is already visited during configuration phase.
   */
  public boolean visited = false;
  /**
   * For persistent server, Id. of router (1st hop) used to access the
   * server from current node. if -1 the server is not accessible. This
   * value is fixed during configuration phase.
   */
  public short gateway = -1;
  /**
   * Logical distance between this server and the root one, i.e. the number
   * of hops to reach it from the local one.
   */
  public int hops = -1;

  public A3CMLServer(short sid,
                     String name,
                     String hostname) throws Exception {
    this.sid = sid;
    if ((name == null) || (name.length() == 0))
      this.name = "server" + sid;
    else
      this.name = name;
    this.hostname = hostname;
    this.services = new Vector();
    this.networks = new Vector();
  }

  public void addNetwork(A3CMLNetwork newNetwork) throws Exception {
    for (int i = 0; i < networks.size(); i++) {
      A3CMLNetwork network = (A3CMLNetwork)networks.elementAt(i);
      if (network.domain.equals(newNetwork.domain)) {
        throw new Exception("Network " + newNetwork.domain + "already added");
      }
    }
    networks.addElement(newNetwork);
  }

  public void removeNetwork(String domainName) {
    for (int i = 0; i < networks.size(); i++) {
      A3CMLNetwork network = (A3CMLNetwork)networks.elementAt(i);
      if (network.domain.equals(domainName)) {
        networks.removeElementAt(i);
      }
    }
  }

  public void addService(A3CMLService newService) throws Exception {
    for (int i = 0; i < services.size(); i++) {
      A3CMLService service = (A3CMLService)services.elementAt(i);
      if (service.classname.equals(newService.classname)) {
        throw new Exception("Service " + newService.classname + "already added");
      }
    }
    services.addElement(newService);
  }

  public void removeService(String serviceClassName) {
    for (int i = 0; i < services.size(); i++) {
      A3CMLService service = (A3CMLService)services.elementAt(i);
      if (service.classname.equals(serviceClassName)) {
        services.removeElementAt(i);
      }
    }
  }

  public A3CMLProperty addProperty(A3CMLProperty prop) {
    if (properties == null)
      properties = new Hashtable();
    return (A3CMLProperty) properties.put(prop.name, prop);
  }
  
  public A3CMLProperty getProperty(String name) {
    if (properties == null) return null;
    return (A3CMLProperty) properties.get(name);
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

  public final A3CMLService getService(String classname) throws UnknownServiceException {
    if (services != null) {
      for (int i = services.size() - 1; i >= 0; i--) {
	A3CMLService service = (A3CMLService) services.elementAt(i);
	if (service.classname.equals(classname))
	  return service;
      }
    }
    throw new UnknownServiceException("Unknown service \"" + classname +
                                      "\" on server#" + sid);
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

  public A3CMLNetwork getNetwork(String domainName) {
    for (int i = networks.size() -1; i >=0; i--) {
      A3CMLNetwork nw = (A3CMLNetwork) networks.elementAt(i);
      if (nw.domain.equals(domainName)) {
        return nw;
      }
    }
    return null;
  }

  public A3CMLServer duplicate(Hashtable context) throws Exception {
    A3CMLServer clone = null;
    Short serverSid = new Short(sid);
    if (!context.containsKey(serverSid)) {
      clone = duplicate();
      context.put(serverSid,clone);
    } else
      clone = (A3CMLServer) context.get(serverSid);
    return clone;
  }

  public A3CMLServer duplicate() throws Exception {
    A3CMLServer clone = new A3CMLServer(sid, 
                                        name, 
                                        hostname);
    if (networks != null) {
      for (Enumeration n = networks.elements(); n.hasMoreElements(); )
        clone.networks.addElement(
          ((A3CMLNetwork) n.nextElement()).duplicate());
    }
    clone.gateway = gateway;
    clone.domain = domain;
    clone.port = port;

    // super class
    clone.visited = visited;
    if (services != null) {
      for (Enumeration e = services.elements(); e.hasMoreElements(); )
        clone.services.addElement(
          ((A3CMLService) e.nextElement()).duplicate());
    }
    if (properties != null) {
      for (Enumeration e = properties.keys(); e.hasMoreElements(); ) {
        
        String pName = (String) e.nextElement();
        A3CMLProperty prop = (A3CMLProperty) properties.get(pName);
        if (prop != null) {
          if (clone.properties == null) clone.properties = new Hashtable();
          clone.properties.put(pName, prop.duplicate());
        }
      }
    }
    if (nat != null) {
      for (Enumeration e = nat.elements(); e.hasMoreElements(); )
        clone.addNat(
          ((A3CMLNat) e.nextElement()).duplicate());
    }
    clone.jvmArgs = jvmArgs;

    return clone;
  }

  public String toString() {
    StringBuffer strBuf = new StringBuffer();
    strBuf.append("(");
    strBuf.append(super.toString());
    strBuf.append(",name=").append(name);
    strBuf.append(",sid=").append(sid);
    strBuf.append(",hostname=").append(hostname);
    strBuf.append(",port=").append(port);
    strBuf.append(",domain=").append(domain);
    strBuf.append(",visited=").append(visited);
    strBuf.append(",networks=").append(networks);
    strBuf.append(",jvmArgs=").append(jvmArgs);
    strBuf.append(",services=").append(services);
    strBuf.append(",properties=").append(properties);
    strBuf.append(",nat=").append(nat);
    strBuf.append(",gateway=").append(gateway);
    strBuf.append(",hops=").append(hops);
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
          ((domain == server.domain) ||
           ((domain != null) && domain.equals(server.domain))) &&
          (port == server.port) &&
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

  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((domain == null) ? 0 : domain.hashCode());
    result = prime * result + gateway;
    result = prime * result + ((hostname == null) ? 0 : hostname.hashCode());
    result = prime * result + ((jvmArgs == null) ? 0 : jvmArgs.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((nat == null) ? 0 : nat.hashCode());
    result = prime * result + ((networks == null) ? 0 : networks.hashCode());
    result = prime * result + port;
    result = prime * result + ((properties == null) ? 0 : properties.hashCode());
    result = prime * result + ((services == null) ? 0 : services.hashCode());
    result = prime * result + sid;
    result = prime * result + (visited ? 1231 : 1237);
    return result;
  }
}
