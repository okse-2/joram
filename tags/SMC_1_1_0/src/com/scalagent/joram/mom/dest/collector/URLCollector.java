/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 ScalAgent Distributed Technologies
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
package com.scalagent.joram.mom.dest.collector;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;

import org.objectweb.joram.shared.messages.Message;
import org.objectweb.joram.shared.util.Properties;


/**
 * 
 */
public class URLCollector implements Collector, Serializable {
 
  private static final long serialVersionUID = 1L;
  private CollectorDestination collectorDest;
  private Properties prop = null;
  
  /**
   * get the file.
   * 
   * @param spec the URL
   * @return the file in byte format.
   * @throws IOException
   */
  private byte[] getResource(String spec) throws IOException {
    ByteArrayOutputStream baos = null;
    BufferedOutputStream bos = null;
    try {
    URL url = new URL(spec);

    URLConnection urlc = url.openConnection();
    InputStream is = urlc.getInputStream();
    
    baos = new ByteArrayOutputStream();
    bos = new BufferedOutputStream(baos);
    
    if (prop == null)
      prop = new Properties();
    prop.put("collector.file", url.getFile());
    prop.put("collector.path", url.getPath());
    prop.put("collector.host", url.getHost());
    
    
    int c = is.read();
    while (c != -1) {
      bos.write(c);
      c = is.read();
    }
    bos.flush();
    return baos.toByteArray();
    } finally {
      if (bos != null)
        bos.close();
      if (baos != null)
        baos.close();
    }
  }

  /**
   * Check the URL resource.
   * Store file in Queue or send to topic (collector destination.
   * 
   * @see com.scalagent.joram.mom.dest.collector.Collector#check()
   */
  public void check() throws IOException {
    String spec = collectorDest.getProperties().getProperty("collector.url");
    String typeStr = collectorDest.getProperties().getProperty("collector.type");
    int type = Message.BYTES;
    if (typeStr != null)
      type = Integer.valueOf(typeStr).intValue();
    
    if (prop == null)
      prop = new Properties();
    prop.put("collector.url", spec);
    collectorDest.sendMessage(type, getResource(spec), prop);
  }

  public void setCollectorDestination(CollectorDestination collectorDest) {
    this.collectorDest = collectorDest;
  }

}
