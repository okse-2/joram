/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
 *
 * The contents of this file are subject to the Joram Public License,
 * as defined by the file JORAM_LICENSE.TXT 
 * 
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License on the Objectweb web site
 * (www.objectweb.org). 
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific terms governing rights and limitations under the License. 
 * 
 * The Original Code is Joram, including the java packages fr.dyade.aaa.agent,
 * fr.dyade.aaa.ip, fr.dyade.aaa.joram, fr.dyade.aaa.mom, and
 * fr.dyade.aaa.util, released May 24, 2000.
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
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

public class NamingContext implements Serializable {

  private Vector records;

  public NamingContext() {
    this.records = new Vector();
  }

  public Record getRecord(String name) {
    for (int i = 0; i < records.size(); i++) {
      Record r = (Record)records.elementAt(i);
      if (r.getName().equals(name)) return r;
    }
    return null;
  } 

  public void addRecord(Record record) {
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
          or.getObject().getClass().getName(),
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
          or.getObject().getClass().getName(),
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

  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append('(' + super.toString());    
    buf.append(",records = ");
    Strings.toString(buf, records);
    buf.append(')');
    return buf.toString();
  }
}
