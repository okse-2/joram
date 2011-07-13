/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2010 ScalAgent Distributed Technologies
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
package org.objectweb.joram.shared.admin;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;

import fr.dyade.aaa.common.stream.StreamUtil;

/**
 * A <code>Monitor_GetUsersRep</code> instance replies to a get users,
 * readers or writers monitoring request.
 */
public class GetRightsReply extends AdminReply {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  /** True if all users can read this destination. */
  private boolean isFreeReading;
  /** True if all users can write this destination. */
  private boolean isFreeWriting;
  /** Table holding the readers identifications. */
  private Hashtable readers;
  /** Table holding the writers identifications. */
  private Hashtable writers;

  /**
   * Constructs a <code>Monitor_GetUsersRep</code> instance.
   */
  public GetRightsReply(boolean success, String info,
                        boolean isFreeReading, boolean isFreeWriting) {
    super(success, info);
    this.isFreeReading = isFreeReading;
    this.isFreeWriting = isFreeWriting;
    readers = new Hashtable();
    writers = new Hashtable();
  }

  /**
   * Returns true if all users can read this destination
   */
  public boolean isFreeReading() {
    return isFreeReading;
  }

  /**
   * Returns true if all users write read this destination
   */
  public boolean isFreeWriting() {
    return isFreeWriting;
  }

  /** Adds a reader to the table. */
  public void addReader(String name, String proxyId) {
    readers.put(name, proxyId);
  }

  /** Returns the readers table. */
  public Hashtable getReaders() {
    return readers;
  }

  /** Adds a writer to the table. */
  public void addWriter(String name, String proxyId) {
    writers.put(name, proxyId);
  }

  /** Returns the writers table. */
  public Hashtable getWriters() {
    return writers;
  }

  public GetRightsReply() {}
  
  protected int getClassId() {
    return GET_RIGHTS_REPLY;
  }
  
  public void readFrom(InputStream is) throws IOException {
    super.readFrom(is);
    isFreeReading = StreamUtil.readBooleanFrom(is);
    isFreeWriting = StreamUtil.readBooleanFrom(is);
    int size = StreamUtil.readIntFrom(is);
    if (size <= 0) {
      readers = null;
    } else {
      readers = new Hashtable(size*4/3);
      for (int i=0; i< size; i++) {
        String key = StreamUtil.readStringFrom(is);
        String value = StreamUtil.readStringFrom(is);
        readers.put(key, value);
      }
    }
    size = StreamUtil.readIntFrom(is);
    if (size <= 0) {
      writers = null;
    } else {
      writers = new Hashtable(size*4/3);
      for (int i=0; i< size; i++) {
        String key = StreamUtil.readStringFrom(is);
        String value = StreamUtil.readStringFrom(is);
        writers.put(key, value);
      }
    }
  }

  public void writeTo(OutputStream os) throws IOException {
    super.writeTo(os);   
    StreamUtil.writeTo(isFreeReading, os);
    StreamUtil.writeTo(isFreeWriting, os);
    if (readers == null) {
      StreamUtil.writeTo(-1, os);
    } else {
      int size = readers.size();
      StreamUtil.writeTo(size, os);
      for (Enumeration keys = readers.keys(); keys.hasMoreElements(); ) {
        String key = (String) keys.nextElement();
        StreamUtil.writeTo(key, os);
        String value = (String) readers.get(key);
        StreamUtil.writeTo(value, os);
      }
    }
    if (writers == null) {
      StreamUtil.writeTo(-1, os);
    } else {
      int size = writers.size();
      StreamUtil.writeTo(size, os);
      for (Enumeration keys = writers.keys(); keys.hasMoreElements(); ) {
        String key = (String) keys.nextElement();
        StreamUtil.writeTo(key, os);
        String value = (String) writers.get(key);
        StreamUtil.writeTo(value, os);
      }
    }
  }
}
