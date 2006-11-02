/*
 * Copyright (C) 2003 - ScalAgent Distributed Technologies
 *
 * Initial developer(s): Nicolas Tachker (ScalAgent)
 * Contributor(s):
 */
package com.scalagent.joram.mom.dest.ftp;

import java.util.Properties;

import fr.dyade.aaa.agent.AgentId;
import org.objectweb.joram.mom.dest.*;

/**
 * A <code>FtpQueue</code> agent is an agent hosting a MOM queue, and which
 * behaviour is provided by a <code>FtpQueueImpl</code> instance.
 *
 * @see FtpQueueImpl
 */
public class FtpQueue extends Queue {
  
  public static final String FTP_QUEUE_TYPE = "queue.ftp";

  public static String getDestinationType() {
    return FTP_QUEUE_TYPE;
  }

  private String user;
  private String pass;
  private String path;

  /**
   * Empty constructor for newInstance(). 
   */ 
  public FtpQueue() {}

  public DestinationImpl createsImpl(AgentId adminId) {
    return new FtpQueueImpl(getId(),adminId,user,pass,path);
  }

  public void setProperties(Properties prop) {
    user = prop.getProperty("user");
    pass = prop.getProperty("pass");
    path = prop.getProperty("path");
  }
}
