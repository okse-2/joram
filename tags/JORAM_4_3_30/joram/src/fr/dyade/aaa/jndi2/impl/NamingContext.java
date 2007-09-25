/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
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

import fr.dyade.aaa.util.*;
import java.util.*;
import java.io.*;
import javax.naming.*;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

public class NamingContext implements Serializable, Cloneable {

  private NamingContextId id;

  private Object ownerId;

  private Vector records;

  public NamingContext(NamingContextId id,
                       Object ownerId) {
    this.id = id;
    this.ownerId = ownerId;
    records = new Vector();
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
	  Trace.logger.log(BasicLevel.DEBUG,"\n\nadd record : "+record +
			   " vector record : "+records +"\n\n");
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
    } else {
      return obj.getClass().getName();
    }
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
    buf.append(",records=");
    Strings.toString(buf, records);
    buf.append(')');
    return buf.toString();
  }
}
