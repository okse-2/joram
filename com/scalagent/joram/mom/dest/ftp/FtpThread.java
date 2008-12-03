/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2008 ScalAgent Distributed Technologies
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

import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.util.Enumeration;

import org.objectweb.joram.mom.util.DMQManager;
import org.objectweb.joram.shared.MessageErrorConstants;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.Channel;
import fr.dyade.aaa.agent.Debug;

public class FtpThread extends Thread {
  public static Logger logger = Debug.getLogger(FtpThread.class.getName());
  
  private TransferItf transfer;
  private AgentId destId;
  private FtpMessage ftpMsg;
  private AgentId dmqId;
  private int clientContext;
  private int requestId;
  private String user;
  private String pass;
  private String path;

  public String ftpImplName;

  public FtpThread(TransferItf transfer,
                   FtpMessage ftpMsg,
                   AgentId destId,
                   AgentId dmqId,
                   int clientContext,
                   int requestId,
                   String user,
                   String pass,
                   String path) {
    this.transfer = transfer;
    this.ftpMsg = ftpMsg;
    this.destId = destId;
    this.dmqId = dmqId;
    this.clientContext = clientContext;
    this.requestId = requestId;
    this.user = user;
    this.pass = pass;
    this.path = path;
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "--- " + this);
  }

  public void run() {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "--- run()");
    doFtp(ftpMsg);
  }


  protected void doFtp(FtpMessage msg) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "doFtp(" + msg + ')');
    
    String urlName = null;
    long crc = -1;
    boolean ack = false;
    
    try {
      urlName = msg.getStringProperty(SharedObj.url);
      crc = msg.getLongProperty(SharedObj.crc);
      ack = msg.getBooleanProperty(SharedObj.ack);

      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "doFtp urlName = " + urlName + ", crc = " + crc + ", ack = " + ack);
      
      URL url = new URL(urlName);
      String urlFileName = url.getFile();
      String fileName = null;
      String type = null;
      String remotePath = null;
      
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG,
                   "--- doFtp : url host = " + url.getHost() + ", urlFileName = " + urlFileName);

      if (urlFileName.indexOf(";") > 1) {
        fileName = urlFileName.substring(
          urlFileName.lastIndexOf('/')+1,urlFileName.indexOf(";"));
        type = urlFileName.substring(
          urlFileName.lastIndexOf("=")+1,urlFileName.length());
      } else {
        fileName = urlFileName.substring(
          urlFileName.lastIndexOf('/')+1,urlFileName.length());
      }
      
      if (urlFileName.indexOf('/') < urlFileName.lastIndexOf('/'))
        remotePath = urlFileName.substring(
          urlFileName.indexOf('/')+1,urlFileName.lastIndexOf('/'));
      
      
      String userInfo = url.getUserInfo();
      String remoteUser = user;
      String remotePass = pass;
      if (userInfo != null) {
        remoteUser = userInfo.substring(0,userInfo.indexOf(':'));
        remotePass = userInfo.substring(userInfo.indexOf(':')+1,userInfo.length());
      }
      String protocol = url.getProtocol();
      int port = url.getPort();

      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, 
                   "doFtp : remoteUser = " + remoteUser + 
                   ", protocol = " + protocol + 
                   ", port = " + port);

      String file = transfer.getFile(protocol,
                                     InetAddress.getByName(url.getHost()).getHostName(),
                                     port,
                                     remoteUser,
                                     remotePass,
                                     remotePath, 
                                     path, 
                                     fileName,
                                     fileName,
                                     type,
                                     crc);
      
      file = file.replace('\\','/');
      if (!file.startsWith("/"))
        file = "/" + file;
      
      URI uri = new URI("file",null,file,null,null);
      uri = uri.normalize();

      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "--- doFtp : uri = " + uri);
      
//          StringBuffer sb = new StringBuffer();
//          sb.append(protocol + "://");
//          sb.append(user);
//          sb.append(":");
//          sb.append("****");//pass);
//          sb.append("@");
//          sb.append(InetAddress.getLocalHost().getHostName());
//          if (port > -1) {
//            sb.append(":");
//            sb.append(port);
//          }
//          sb.append("/");
//          if (path != null)
//            sb.append(path + "/");
//          sb.append(fileName);
//          sb.append(";type=");
//          sb.append(type);
 

      FtpMessage clone = (FtpMessage) msg.clone();
      clone.clearProperties();
      for (Enumeration e = msg.getPropertyNames(); e.hasMoreElements(); ) {
        String key = (String) e.nextElement();
        clone.setObjectProperty(key, msg.getObjectProperty(key));
      }
      clone.setStringProperty("url", uri.toString());
      
      Channel.sendTo(destId, new FtpNot(clientContext, 
                                        requestId,
                                        clone.getSharedMessage()));
      
    } catch (Exception exc) {
      DMQManager dmqManager = new DMQManager(dmqId, destId);
      dmqManager.addDeadMessage(msg.getSharedMessage(), MessageErrorConstants.UNEXPECTED_ERROR);
      dmqManager.sendToDMQ();
    }
  }

  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append("FtpThread (");
    buf.append("transfer=");
    buf.append(transfer);
    buf.append(", ftpMsg=");
    buf.append(ftpMsg);
    buf.append(", destId=");
    buf.append(destId);
    buf.append(", dmqId=");
    buf.append(dmqId);
    buf.append(", clientContext=");
    buf.append(clientContext);
    buf.append(", requestId=");
    buf.append(requestId);
    buf.append(", user=");
    buf.append(user);
    buf.append(", pass=***");
    //buf.append(pass);
    buf.append(", path=");
    buf.append(path);
    buf.append(")");
    return buf.toString();
  }
}
