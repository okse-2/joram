/*
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
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
 * fr.dyade.aaa.util, fr.dyade.aaa.ip, fr.dyade.aaa.mom, and fr.dyade.aaa.joram,
 * released May 24, 2000. 
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
 */
package fr.dyade.aaa.util;

import java.io.*;
import java.net.*;

public class AdminClientProxy {

  public Socket socket = null;
  public DataInputStream dis = null;
  public DataOutputStream dos = null;

  public AdminClientProxy () {
    super ();
  }

  public void connect (String hostName, int port) throws Exception {
    socket = new Socket (hostName, port);
    socket.setTcpNoDelay(true);
    dos = new DataOutputStream (socket.getOutputStream());
    dis = new DataInputStream (socket.getInputStream());
  }

  public int putFile (String sourceName, int blockSize, String destinationName, String errorMessage) throws Exception {
    File f = null;
    long fileSize = 0;

    if ((sourceName == null) || (sourceName.trim().length() == 0))
      throw (new Exception ("Invalid source file name"));
    if ((blockSize <= 0) || (blockSize > 1024))
      blockSize = 1024;
    if ((destinationName == null) || (destinationName.trim().length() == 0))
      destinationName = null;
    f = new File (sourceName);
    fileSize = f.length ();
    dos.write (("putf "+sourceName+" "+fileSize+" "+blockSize+((destinationName != null)?" "+destinationName:"")+"\r\n").getBytes());
    FileInputStream fin = new FileInputStream (f);
    DataInputStream din = new DataInputStream (fin);
    byte [] currentBlockAck = new byte [24];
    for (int i = 0; i * blockSize < fileSize; i++) {
      byte [] currentBlock = new byte [blockSize];
      din.read (currentBlock, 0, (int) Math.min (blockSize, fileSize - (i * blockSize)));
      dos.write (currentBlock, 0, (int) Math.min (blockSize, fileSize - (i * blockSize)));
      dos.flush ();
      while (dis.read (currentBlock, 0, 1) < 1);
      if (currentBlock[0] != (byte) '.') {
        errorMessage = new String (currentBlock);
        return -1;
      }
    }
    din.close ();
    return 0;
  }

  public void disconnect () throws Exception {
    dis.close ();
    dos.close ();
  }
}
