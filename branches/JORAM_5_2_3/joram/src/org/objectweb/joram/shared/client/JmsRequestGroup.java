/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2008 ScalAgent Distributed Technologies
 * Copyright (C) 1996 - 2000 Dyade
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
 * 
 * Created on 3 mai 2006
 *
 */
package org.objectweb.joram.shared.client;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

import org.objectweb.joram.shared.stream.StreamUtil;

/**
 * 
 */
public final class JmsRequestGroup extends AbstractJmsRequest {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  protected int getClassId() {
    return JMS_REQUEST_GROUP;
  }

  private AbstractJmsRequest[] requests;
  
  public JmsRequestGroup(AbstractJmsRequest[] ajr) {
    requests = ajr;
  }
  
  public final AbstractJmsRequest[] getRequests() {
    return requests;
  }

  /**
   * Constructs a <code>JmsRequestGroup</code> instance.
   */
  public JmsRequestGroup() {}

  public void toString(StringBuffer strbuf) {
    super.toString(strbuf);
    strbuf.append(",requests.length=").append(requests.length);
    strbuf.append(')');
  }

  /* ***** ***** ***** ***** *****
   * Streamable interface
   * ***** ***** ***** ***** ***** */

  /**
   *  The object implements the writeTo method to write its contents to
   * the output stream.
   *
   * @param os the stream to write the object to
   */
  public void writeTo(OutputStream os) throws IOException {
    super.writeTo(os);
    if (requests == null) {
      StreamUtil.writeTo(-1, os);
    } else {
      int size = requests.length;
      StreamUtil.writeTo(size, os);
      for (int i=0; i<size; i++) {
        StreamUtil.writeTo(requests[i].getClass().getName(), os);
        requests[i].writeTo(os);
      }
    }
  }

  /**
   *  The object implements the readFrom method to restore its contents from
   * the input stream.
   *
   * @param is the stream to read data from in order to restore the object
   */
  public void readFrom(InputStream is) throws IOException {
    super.readFrom(is);
    int size = StreamUtil.readIntFrom(is);
    if (size == -1) {
      requests = null;
    } else {
      requests = new AbstractJmsRequest[size];
      for (int i=0; i<size; i++) {
        String cn = StreamUtil.readStringFrom(is);
        try {
          requests[i] = (AbstractJmsRequest) Class.forName(cn).newInstance();
        } catch (ClassNotFoundException exc) {
          throw new IOException("AbstractJmsRequest.readFrom(), Unknown class " + cn);
        } catch (InstantiationException exc) {
          throw new IOException("AbstractJmsRequest.readFrom(), Cannot Instantiate " + cn);
        } catch (IllegalAccessException exc) {
          throw new IOException("AbstractJmsRequest.readFrom(), Cannot IllegalAccessException " + cn);
        }
        requests[i].readFrom(is);
      }
    }
  }
}
