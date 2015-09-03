/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2015 ScalAgent Distributed Technologies
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
 * A <code>GetJMXAttsReply</code> instance replies to a GetJMXAttsRequest
 * monitoring request.
 */
public class GetJMXAttsReply extends AdminReply {
  private static final long serialVersionUID = 1L;

  /** Table holding the statistic. */
  private Hashtable stats;

  /**
   * Constructs a <code>Monitor_GetStatRep</code> instance.
   */
  public GetJMXAttsReply(Hashtable stats) {
    super(true, null);
    this.stats = stats;
  }
  
  /** Returns the stats table. */
  public Hashtable getStats() {
    return stats;
  }

  public GetJMXAttsReply() { }
  
  protected int getClassId() {
    return MONITOR_GET_JMX_ATTS_REP;
  }
  
  public void readFrom(InputStream is) throws IOException {
    super.readFrom(is);   
    int size = StreamUtil.readIntFrom(is);
    if (size == -1) {
      stats = null;
    } else {
      stats = new Hashtable(size*4/3);
      for (int i=0; i< size; i++) {
        String key = StreamUtil.readStringFrom(is);
        Object value = StreamUtil.readObjectFrom(is);
        stats.put(key, value);
      }
    }
  }

  public void writeTo(OutputStream os) throws IOException {
    super.writeTo(os);   
    if (stats == null) {
      StreamUtil.writeTo(-1, os);
    } else {
      int size = stats.size();
      StreamUtil.writeTo(size, os);
      for (Enumeration keys = stats.keys(); keys.hasMoreElements(); ) {
        String key = (String) keys.nextElement();
        StreamUtil.writeTo(key, os);
        StreamUtil.writeObjectTo(stats.get(key), os);
      }
    }
  }
}
