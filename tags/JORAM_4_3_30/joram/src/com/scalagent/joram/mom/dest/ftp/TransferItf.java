/*
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 *
 * Initial developer(s): Nicolas Tachker (ScalAgent)
 * Contributor(s):
 */
package com.scalagent.joram.mom.dest.ftp;


public interface TransferItf {

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
                        long crc) throws Exception;
}
