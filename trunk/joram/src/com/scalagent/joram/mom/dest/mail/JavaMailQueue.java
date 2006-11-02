/*
 * Copyright (C) 2003 - ScalAgent Distributed Technologies
 *
 * Initial developer(s): Nicolas Tachker (ScalAgent)
 * Contributor(s):
 */
package com.scalagent.joram.mom.dest.mail;

import java.util.Properties;
import org.objectweb.joram.mom.dest.*;
import fr.dyade.aaa.agent.AgentId;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.joram.mom.MomTracing;

/**
 * A <code>JavaMailQueue</code> agent is an agent hosting a MOM queue, and which
 * behaviour is provided by a <code>JavaMailQueueImpl</code> instance.
 *
 * @see JavaMailQueueImpl
 */
public class JavaMailQueue extends Queue {

  public static final String MAIL_QUEUE_TYPE = "queue.mail";

  public static String getDestinationType() {
    return MAIL_QUEUE_TYPE;
  }

  private String smtpServer = null;
  private String to = null;
  private String cc = null;
  private String bcc = null;
  private String from = null;
  private String subject = null;
  private String selector = null;
  private long popPeriod = -1;
  private String popServer = null;
  private String popUser = null;
  private String popPassword = null;
  private boolean expunge = false;

  /**
   * Empty constructor for newInstance(). 
   */ 
  public JavaMailQueue() {
    if (popPeriod != -1)
      new PopTask((JavaMailQueueImpl) destImpl,popPeriod);
  }

  public DestinationImpl createsImpl(AgentId adminId) {
    JavaMailQueueImpl queueImpl = new JavaMailQueueImpl(getId(),
                                                        adminId,
                                                        smtpServer,
                                                        to,
                                                        cc,
                                                        bcc,
                                                        from,
                                                        subject,
                                                        selector,
                                                        popPeriod,
                                                        popServer,
                                                        popUser, 
                                                        popPassword,
                                                        expunge);
    if (popPeriod != -1)
      new PopTask(queueImpl,popPeriod);
    return queueImpl;
  }

  public void setProperties(Properties prop) {
    smtpServer = prop.getProperty("smtpServer");
    to = prop.getProperty("to");
    cc = prop.getProperty("cc");
    bcc = prop.getProperty("bcc");
    from = prop.getProperty("from");
    subject = prop.getProperty("subject");
    selector = prop.getProperty("selector");
    try {
      popPeriod = Long.valueOf(prop.getProperty("popPeriod")).longValue();
    } catch (NumberFormatException exc) {
      popPeriod = 120000;
    }
    popServer = prop.getProperty("popServer");
    popUser = prop.getProperty("popUser");
    popPassword = prop.getProperty("popPassword");
    expunge = Boolean.valueOf(prop.getProperty("expunge")).booleanValue();
  }
}
