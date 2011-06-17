/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2010 ScalAgent Distributed Technologies
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
package com.scalagent.joram.mom.dest.ftp;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class TransferImplRef implements TransferItf {

  public String getFile(String protocol,
                        String host, int port,
                        String user, String pass,
                        String remotePath, 
                        String localPath, 
                        String remoteFileName,
                        String localFileName,
                        String type,
                        long crc) throws Exception {
    
    StringBuffer sb = new StringBuffer();
    
    sb.append("ftp://").append(user).append(':').append(pass).append('@').append(host);
    if (port > -1) {
      sb.append(":").append(port);
    }
    sb.append('/');
    if (remotePath != null) {
      sb.append(remotePath).append('/');
    }
    sb.append(remoteFileName);
    sb.append(";type=").append(type);

    URL url = new URL(sb.toString());

    URLConnection urlc = url.openConnection();
    InputStream is = urlc.getInputStream();
    
    File file = new File(localPath, localFileName);
    
    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
    
    int c = is.read();
    while (c != -1) {
      bos.write(c);
      c = is.read();
    }
    bos.flush();
    bos.close();
    is.close();

    if (crc > 0 && crc != file.length())
      throw new Exception("CRC ERROR.");

    return file.getAbsolutePath();
  }
}
