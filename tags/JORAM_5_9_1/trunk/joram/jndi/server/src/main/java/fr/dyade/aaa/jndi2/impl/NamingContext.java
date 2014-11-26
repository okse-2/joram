/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2008 ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
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
 * Initial developer(s): Sofiane Chibani
 * Contributor(s): David Feliot, Nicolas Tachker
 */
package fr.dyade.aaa.jndi2.impl;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

import javax.naming.Binding;
import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.InvalidNameException;
import javax.naming.NameClassPair;
import javax.naming.NamingException;
import javax.naming.Reference;

import org.objectweb.util.monolog.api.BasicLevel;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.Channel;
import fr.dyade.aaa.common.Strings;
import fr.dyade.aaa.jndi2.msg.ChangeOwnerRequest;
import fr.dyade.aaa.jndi2.msg.CreateSubcontextRequest;
import fr.dyade.aaa.jndi2.msg.DestroySubcontextRequest;
import fr.dyade.aaa.jndi2.msg.JndiRequest;
import fr.dyade.aaa.jndi2.msg.UnbindRequest;
import fr.dyade.aaa.jndi2.server.JndiScriptRequestNot;

public class NamingContext implements NamingContextMBean, Serializable, Cloneable {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private NamingContextId id;

  private Object ownerId;

  private Vector records;
  
  private CompositeName contextName;

  public NamingContext(NamingContextId id,
                       Object ownerId,
                       CompositeName contextName) {
    this.id = id;
    this.ownerId = ownerId;
    records = new Vector();
    this.contextName = contextName;
  }

  public final NamingContextId getId() {
    return id;
  }

  public final Object getOwnerId() {
    return ownerId;
  }

  public void setOwnerId(Object ownerId) {
    this.ownerId = ownerId;
  }

  public Record getRecord(String name) {
    for (int i = 0; i < records.size(); i++) {
      Record r = (Record)records.elementAt(i);
      if (r.getName().equals(name)) return r;
    }
    return null;
  } 
  
  public Enumeration getEnumRecord() {
    Vector elt=new Vector();
    for (int i = 0; i < records.size(); i++) {
      Record r = (Record)records.elementAt(i);
      elt.add(r);
    }
    return elt.elements();
  } 

  public void addRecord(Record record) {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG,"\nadd record : "+record +
          " vector record : " + records +"\n");
    records.addElement(record);
  }

  public boolean removeRecord(String name) {
    for (int i = 0; i < records.size(); i++) {
      Record r = (Record)records.elementAt(i);
      if (r.getName().equals(name)) {
        records.removeElementAt(i);
        return true;
      }
    }
    return false;
  }

  public int size() {
    return records.size();
  }

  public NameClassPair[] getNameClassPairs() {
    NameClassPair[] res = new NameClassPair[records.size()];
    for (int i = 0; i < records.size(); i++) {
      Record r = (Record)records.elementAt(i);
      if (r instanceof ObjectRecord) {
        ObjectRecord or = (ObjectRecord)r;
        res[i] = new NameClassPair(
          or.getName(),
          getClassName(or.getObject()),
          true);
      } else if (r instanceof ContextRecord) {
        res[i] = new NameClassPair(
          r.getName(),
          Context.class.getName(),
          true);
      }
    }
    return res;
  }

  public Binding[] getBindings() {
    Binding[] res = new Binding[records.size()];
    for (int i = 0; i < records.size(); i++) {
      Record r = (Record)records.elementAt(i);
      if (r instanceof ObjectRecord) {
        ObjectRecord or = (ObjectRecord)r;        
        res[i] = new Binding(
          or.getName(),
          getClassName(or.getObject()),
          or.getObject(),
          true);
      } else if (r instanceof ContextRecord) {
        res[i] = new Binding(
          r.getName(),
          Context.class.getName(),
          null,
          true);
      }
    }
    return res;
  }

  private static String getClassName(Object obj) {
    if (obj instanceof Reference) {
      Reference ref = (Reference)obj;
      return ref.getClassName();
    }
    return obj.getClass().getName();
  }

  public Object clone() {
    try {
      NamingContext clone = 
        (NamingContext)super.clone();
      clone.records = (Vector)records.clone();
      // other attributes are cloned
      return clone;
    } catch (CloneNotSupportedException exc) {
      return null;
    }
  }

  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append('(' + super.toString());
    buf.append(",id=" + id);
    buf.append(",ownerId=" + ownerId);
    buf.append(",name=" + contextName);
    buf.append(",records=");
    Strings.toString(buf, records);
    buf.append(')');
    return buf.toString();
  }

  public CompositeName getContextName() {
    return contextName;  
  }
  
  // ======== MBean implementation ===========
  public String[] getNamingContext() {
    String[] array = new String[records.size()];
     for (int i=0; i < records.size(); i++) {
      Record r = (Record)records.elementAt(i);
      if (r instanceof ObjectRecord) 
        array[i] = r.getName();
      else
        array[i] = "(javax.naming.Context)- " + r.getName();
    }
    return array;    
  }
  
  public String getStrOwnerId() {
    return ownerId.toString();
  }

  public void setStrOwnerId(String strOwnerId) {
    if (!strOwnerId.equals(ownerId.toString()))
      sendTo(new ChangeOwnerRequest((CompositeName) contextName.clone(), strOwnerId));
  }

  public void createSubcontext(String ctxName) throws NamingException {
    CompositeName cn = (CompositeName) contextName.clone();
    if (contextName != null)
      cn.add(ctxName);
    else
      cn = getCompositeName(ctxName);
    sendTo(new CreateSubcontextRequest(cn));
  }

  public void destroySubcontext() throws NamingException {
    sendTo(new DestroySubcontextRequest(contextName));
  }

  public String lookup(String name) throws NamingException {
    Record rec = getRecord(name);
    if (rec != null)
      return rec.toString();
    return null;
  }

  public void unbind(String name) throws NamingException {
    CompositeName cn = (CompositeName) contextName.clone();
    if (contextName != null)
      cn.add(name);
    else
      cn = getCompositeName(name);
    sendTo(new UnbindRequest(cn));
  }
 
  public Properties getProperties(String name) throws NamingException {
    Properties prop = new Properties();
    Record rec = getRecord(name);
    if(rec instanceof ObjectRecord) {
      Reference ref = (Reference) ((ObjectRecord) rec).getObject();
      String className = ref.getClassName();
      prop.setProperty("className", className);
      if(className.equals("org.objectweb.joram.client.jms.Topic")
          || className.equals("org.objectweb.joram.client.jms.Queue")) {
        prop.setProperty("agentId", (String) ref.get("dest.agentId").getContent());        
        prop.setProperty("name", (String) ref.get("dest.adminName").getContent());
      } else if(className.equals("org.objectweb.joram.client.jms.tcp.QueueTcpConnectionFactory")
          ||className.equals("org.objectweb.joram.client.jms.tcp.TopicTcpConnectionFactory")
          || className.equals("org.objectweb.joram.client.jms.tcp.TcpConnectionFactory")) {
        prop.setProperty("host", (String) ref.get("cf.host").getContent());        
        prop.setProperty("port", (String) ref.get("cf.port").getContent());        
      }
    }
    return prop;
  }
  
  private CompositeName getCompositeName(String path) throws InvalidNameException {
    if (path.startsWith("/"))
      return new CompositeName(path.substring(1, path.length()));
    return new CompositeName(path);
  }
  
  private void sendTo(JndiRequest request) {
    Channel.sendTo((AgentId)ownerId, new JndiScriptRequestNot(new JndiRequest[]{request}));
  }
  // ======== end MBean implementation ===========
}
