/*
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 *
 * Initial developer(s): Nicolas Tachker (ScalAgent)
 * Contributor(s):
 */
package com.scalagent.joram.mom.dest.ftp;

import java.io.*;
import java.net.*;

public class TransferImplRef 
  implements TransferItf {

  public String getFile(String protocol,
                        String host,
                        int port,
                        String user,
                        String pass,
                        String remotePath, 
                        String localPath, 
                        String remoteFileName,
                        String localFileName,
                        String type,
                        long crc) throws Exception {
    
    StringBuffer sb = new StringBuffer();
    
    sb.append("ftp://");
    sb.append(user);
    sb.append(":");
    sb.append(pass);
    sb.append("@");
    sb.append(host);
    if (port > -1) {
      sb.append(":");
      sb.append(port);
    }
    sb.append("/");
    if (remotePath != null)
      sb.append(remotePath + "/");
    sb.append(remoteFileName);
    sb.append(";type=");
    sb.append(type);

    URL url = new URL(sb.toString());

    URLConnection urlc = url.openConnection();
    InputStream is = urlc.getInputStream();
    
    File file = new File(localPath,localFileName);
    
    BufferedOutputStream bos = new BufferedOutputStream(
      new FileOutputStream(file));
    
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
