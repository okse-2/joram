/*
 * Copyright (C) 2003 - ScalAgent Distributed Technologies
 *
 * Initial developer(s): Nicolas Tachker (ScalAgent)
 * Contributor(s):
 */
package com.scalagent.joram.mom.dest.ftp;

import java.io.*;
import java.net.*;

import net.sf.jftp.net.ConnectionHandler;
import net.sf.jftp.net.ConnectionListener;
import net.sf.jftp.net.DataConnection;
import net.sf.jftp.net.FtpConnection;
import net.sf.jftp.net.BasicConnection;
import net.sf.jftp.config.Settings;

public class TransferImplJftp 
  implements TransferItf, ConnectionListener {

  private boolean established = false;

  // connection pool, not necessary but you should take a look at this class
  // if you want to use multiple event based ftp transfers.
  private ConnectionHandler handler = new ConnectionHandler();

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
    
    Settings.setProperty("jftp.disableLog","true");
    Settings.setProperty("jftp.enableMultiThreading","true");
    Settings.maxConnections = 5;
    Settings.enableResuming = true;
    
    // create a FtpConnection
    FtpConnection con = null;
    if (port > -1) 
      con = new FtpConnection(host,port,remotePath);
    else
      con = new FtpConnection(host);

    // set updatelistener, interface methods are below
    con.addConnectionListener(this);

    // set handler
    con.setConnectionHandler(handler);

    // connect and login. 
    con.login(user,pass);

    // login calls connectionInitialized() below which sets established to true
    while(!established) {
      try { 
        Thread.sleep(10); 
      } catch(Exception exc) { 
        throw exc; 
      }
    }

    if (remotePath != null)
      con.chdirRaw(remotePath);

    if (localPath != null)
      con.setLocalPath(localPath);

    //System.out.println("LocalPath = " + con.getLocalPath());

    // which spawns a new thread for the download
    con.download(remoteFileName);
    
    File file = new File(con.getLocalPath(),localFileName);
    
    if (crc > 0 && crc != file.length())
      throw new Exception("CRC ERROR.");
    
    return file.getAbsolutePath();
  }


  //------ needed by ConnectionListener interface ------
  
  // called if the remote directory has changed
  public void updateRemoteDirectory(BasicConnection con) {}

  // called if a connection has been established
  public void connectionInitialized(BasicConnection con) {
    established = true;
  }
 
  // called every few kb by DataConnection during the trnsfer (interval can be changed in Settings)
  public void updateProgress(String file, String type, long bytes) {}

  // called if connection fails
  public void connectionFailed(BasicConnection con, String why) {System.out.println("connection failed! " + why);}

  // up- or download has finished
  public void actionFinished(BasicConnection con) {}
}
