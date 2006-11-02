/*
 * Copyright (C) 2003 - ScalAgent Distributed Technologies
 *
 * Initial developer(s): Nicolas Tachker (ScalAgent)
 * Contributor(s):
 */
package com.scalagent.joram.mom.dest.mail;

import java.util.Properties;
import fr.dyade.aaa.agent.AgentId;
import org.objectweb.joram.mom.dest.*;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.joram.mom.MomTracing;

/**
 * A <code>JavaMailTopic</code> agent is an agent hosting a MOM queue, and which
 * behaviour is provided by a <code>JavaMailQueueImpl</code> instance.
 *
 * @see JavaMailTopicImpl
 */
public class JavaMailTopic extends Topic {
  
  public static final String MAIL_TOPIC_TYPE = "topic.mail";

  public static String getDestinationType() {
    return MAIL_TOPIC_TYPE;
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
  public JavaMailTopic() {
    if (popPeriod != -1)
      new PopTask((JavaMailTopicImpl)destImpl,popPeriod);
  }

  public DestinationImpl createsImpl(AgentId adminId) {
    JavaMailTopicImpl topicImpl = new JavaMailTopicImpl(getId(),
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
      new PopTask(topicImpl,popPeriod);
    return topicImpl;
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

