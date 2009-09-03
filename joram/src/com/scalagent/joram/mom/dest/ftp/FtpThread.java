/*
 * Copyright (C) 2003 - ScalAgent Distributed Technologies
 *
 * Initial developer(s): Nicolas Tachker (ScalAgent)
 * Contributor(s):
 */
package com.scalagent.joram.mom.dest.ftp;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.Channel;
import org.objectweb.joram.shared.messages.Message;
import org.objectweb.joram.mom.notifications.ClientMessages;
import java.util.Enumeration;
import java.net.*;

import org.objectweb.joram.mom.MomTracing;
import org.objectweb.util.monolog.api.BasicLevel;

public class FtpThread extends Thread {

  private TransferItf transfer;
  private AgentId destId;
  private Message ftpMsg;
  private AgentId dmqId;
  private int clientContext;
  private int requestId;
  private String user;
  private String pass;
  private String path;

  public String ftpImplName;

  public FtpThread(TransferItf transfer,
                   Message ftpMsg,
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
    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, "--- " + this);
  }

  public void run() {
    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, "--- run()");
    doFtp(ftpMsg);
  }


  protected void doFtp(Message msg) {
    String urlName = null;
    long crc = -1;
    boolean ack = false;
    
    try {
      urlName = msg.getStringProperty(SharedObj.url);
      crc = msg.getLongProperty(SharedObj.crc);
      ack = msg.getBooleanProperty(SharedObj.ack);

      URL url = new URL(urlName);
      String urlFileName = url.getFile();
      String fileName = null;
      String type = null;
      String remotePath = null;
      
      if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
        MomTracing.dbgDestination.log(BasicLevel.DEBUG, 
                                      "--- doFtp : url host = " + url.getHost() + 
                                      ", urlFileName = " + urlFileName);

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
      String remoteUser = "anonymous";
      String remotePass = "no@no.no";
      if (userInfo != null) {
        remoteUser = userInfo.substring(0,userInfo.indexOf(':'));
        remotePass = userInfo.substring(userInfo.indexOf(':')+1,userInfo.length());
      }
      String protocol = url.getProtocol();
      int port = url.getPort();

      if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
        MomTracing.dbgDestination.log(BasicLevel.DEBUG, 
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

      if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
        MomTracing.dbgDestination.log(BasicLevel.DEBUG, "--- doFtp : uri = " + uri);
      
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
 

      Message clone = (Message) msg.clone();
      clone.clearProperties();
      for (Enumeration e = msg.getPropertyNames(); e.hasMoreElements(); ) {
        String key = (String) e.nextElement();
        clone.setObjectProperty(key,msg.getObjectProperty(key));
      }
      clone.setStringProperty("url",uri.toString());
      
      Channel.sendTo(destId,new FtpNot(clientContext, 
                                       requestId,
                                       clone));
      
    } catch (Exception exc) {
      ClientMessages deadM = 
        new ClientMessages(clientContext, 
                           requestId);
      msg.notWriteable = true;
      deadM.addMessage(msg);
      if (dmqId != null)
        Channel.sendTo(dmqId,deadM);
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
    buf.append(", pass=");
    buf.append(pass);
    buf.append(", path=");
    buf.append(path);
    buf.append(")");
    return buf.toString();
  }
}
