/*
 * Copyright (C) 2003 - ScalAgent Distributed Technologies
 *
 * Initial developer(s): Nicolas Tachker (ScalAgent)
 * Contributor(s):
 */
package com.scalagent.joram.mom.dest.mail;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.Channel;
import fr.dyade.aaa.agent.Notification;
import org.objectweb.joram.mom.MomTracing;
import org.objectweb.joram.mom.notifications.*;
import org.objectweb.joram.shared.excepts.*;
import org.objectweb.joram.shared.messages.*;
import org.objectweb.joram.shared.selectors.*;
import org.objectweb.joram.mom.dest.*;
import org.objectweb.joram.shared.admin.SpecialAdmin;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;
import org.objectweb.joram.shared.messages.Message;

import org.objectweb.util.monolog.api.BasicLevel;


/**
 * The <code>JavaMailTopicImpl</code> class implements the MOM topic behaviour,
 * basically storing messages and delivering them upon clients requests.
 */
public class JavaMailTopicImpl extends TopicImpl implements JavaMailDest {

  private long popPeriod = 60000;
  private String popServer = null;
  private String popUser = null;
  private String popPassword = null;
  private boolean expunge = false;

  private Vector senderInfos = null;

  private transient JavaMailUtil javaMailUtil = null;

  /**
   * Constructs a <code>JavaMailTopicImpl</code> instance.
   *
   * @param destId  Identifier of the agent hosting the topic.
   * @param adminId  Identifier of the administrator of the topic.
   * @param smtpServer 
   * @param to 
   * @param from
   * @param subject
   * @param selector
   * @param popPeriod
   * @param popServer
   * @param popUser
   * @param popPassword
   * @param expunge
   */
  public JavaMailTopicImpl(AgentId destId, 
                           AgentId adminId,
                           String smtpServer,
                           String to,
                           String cc,
                           String bcc,
                           String from,
                           String subject,
                           String selector,
                           long popPeriod,
                           String popServer,
                           String popUser, 
                           String popPassword,
                           boolean expunge) {
    super(destId, adminId);

    // to send mail
    senderInfos = new Vector();
    senderInfos.add(
      new SenderInfo(smtpServer,to,cc,bcc,from,subject,selector));

    // to receive mail
    this.popPeriod = popPeriod;
    this.popServer = popServer;
    this.popUser = popUser;
    this.popPassword = popPassword;
    this.expunge = expunge;

    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, 
                                    "--- " + this +
                                    " JavaMailTopicImpl : " +
                                    "\nsenderInfos=" + senderInfos +
                                    "\npopServer=" + popServer +
                                    "\npopUser=" + popUser +
                                    "\npopPeriod=" + popPeriod +
                                    "\nexpunge=" + expunge);
  }

  public String toString() {
    return "JavaMailTopicImpl:" + destId.toString();
  }

  protected void specialProcess(Notification not) {
    if (not instanceof ClientMessages) {
      doProcess((ClientMessages) not);
      super.doProcess((ClientMessages) not);
    } else
      super.specialProcess(not);
  }

  protected Object specialAdminProcess(SpecialAdminRequest not) 
    throws RequestException {

    try {
      SpecialAdmin req = not.getRequest();
      
      if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
        MomTracing.dbgDestination.log(BasicLevel.DEBUG, 
                                      "--- " + this +
                                      " specialAdminProcess : " +
                                      req);
      if (req instanceof AddSenderInfo)
        addSenderInfo(((AddSenderInfo) req).si,
                      ((AddSenderInfo) req).index);
      else if (req instanceof RemoveSenderInfo) {
        if (((RemoveSenderInfo) req).index > -1)
          removeSenderInfo(((RemoveSenderInfo) req).index);
        else 
          removeSenderInfo(((RemoveSenderInfo) req).si);
      } else if (req instanceof UpdateSenderInfo)
        updateSenderInfo(((UpdateSenderInfo) req).oldSi,
                         ((UpdateSenderInfo) req).newSi);
        
      if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
        MomTracing.dbgDestination.log(BasicLevel.DEBUG, 
                                      "--- " + this +
                                      " specialAdminProcess senderInfos=" +
                                      senderInfos);
    } catch (Exception exc) {
      if (MomTracing.dbgDestination.isLoggable(BasicLevel.WARN))
        MomTracing.dbgDestination.log(BasicLevel.WARN, 
                                      "--- " + this +
                                      " specialAdminProcess",
                                      exc);
      throw new RequestException(exc.getMessage());
    }
    return "done";
  }
    
  protected void addSenderInfo(SenderInfo si, int index) 
    throws ArrayIndexOutOfBoundsException {
    if (index > -1)
      senderInfos.add(index,si);
    else
      senderInfos.add(si);
  }

  protected SenderInfo removeSenderInfo(int index)
    throws ArrayIndexOutOfBoundsException {
    return (SenderInfo) senderInfos.remove(index);
  }

  protected boolean removeSenderInfo(SenderInfo si) {
    return senderInfos.remove(si);
  }

  protected void updateSenderInfo(SenderInfo oldSi, SenderInfo newSi)
    throws ArrayIndexOutOfBoundsException {
    int index = senderInfos.indexOf(oldSi);
    if (index > -1)
      senderInfos.set(index,newSi);
  }

  protected void doProcess(ClientMessages not) {
    for (Enumeration msgs = not.getMessages().elements();
         msgs.hasMoreElements();) {
      Message msg = (Message) msgs.nextElement();
      SenderInfo si = match(msg);

      if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
        MomTracing.dbgDestination.log(BasicLevel.DEBUG, 
                                      "--- " + this +
                                      " match=" + (si!=null));
      if (si != null) {
        try {
          javaMailUtil.sendJavaMail(si,msg);
        } catch (Exception exc) {
          ClientMessages deadM = 
            new ClientMessages(not.getClientContext(), 
                               not.getRequestId());
          
          deadM.addMessage(msg);
          sendToDMQ(deadM,not.getDMQId());
          
          MomTracing.dbgDestination.log(BasicLevel.WARN,
                                        "JavaMailTopicImpl.sendJavaMail", 
                                        exc);
        }
      }
    }
  }
  
  protected SenderInfo match(Message msg) {
    for (Enumeration e = senderInfos.elements(); e.hasMoreElements(); ) {
      SenderInfo si = (SenderInfo) e.nextElement();
      if (si.selector == null) return si;
      if (Selector.matches(msg,si.selector))
        return si;
    }
    return null;
  }

  public void doPop() {
    long count = 0;
    Vector toExpunge = new Vector();
    javax.mail.Message[] msgs = javaMailUtil.popMail(popServer,
                                                     popUser,
                                                     popPassword,
                                                     expunge);      
    if (msgs != null) {
      for (int i = 0; i < msgs.length; i++) {
        if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
          MomTracing.dbgDestination.log(BasicLevel.DEBUG, 
                                        "--- " + this +
                                        " doPop : msgs[" + i + "] = " + msgs[i]);
        try {
          count++;
          Properties prop = javaMailUtil.getMOMProperties(msgs[i]);
          Message m = 
            javaMailUtil.createMessage(prop,
                                       destId.toString()+"mail_"+count,
                                       Topic.getDestinationType(),
                                       destId.toString(),
                                       Topic.getDestinationType());
          publish(m);

          if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
            MomTracing.dbgDestination.log(BasicLevel.DEBUG, 
                                          "--- " + this +
                                          " doPop : publish m = " + m);
          if (expunge)
            toExpunge.add(msgs[i]);
        } catch (Exception exc) {
          if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
            MomTracing.dbgDestination.log(BasicLevel.DEBUG, 
                                          "--- " + this +
                                          " doPop", exc);
          continue;
        }
      }
    }
    
    javaMailUtil.closeFolder(toExpunge,expunge);
    toExpunge.clear();
  }
  
  private void publish(Message msg) {
    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, 
                                    "--- " + this +
                                    " publish msg=" + msg);

    Vector messages = new Vector();
    messages.add(msg);
    ClientMessages cm = new ClientMessages(-1,-1,messages);
    // not use channel.sendTo(...) because from=#0.0.0
    //javaMailTopic.send(destId,cm);
    Channel.sendTo(destId,cm);
  }

  private void readObject(java.io.ObjectInputStream in)
    throws IOException, ClassNotFoundException {

    in.defaultReadObject();

    if (MomTracing.dbgDestination.isLoggable(BasicLevel.DEBUG))
      MomTracing.dbgDestination.log(BasicLevel.DEBUG, 
                                    "--- " + this +
                                    " JavaMailTopicImpl.readObject : " +
                                    "\nsenderInfos=" + senderInfos +
                                    "\npopServer=" + popServer +
                                    "\npopUser=" + popUser +
                                    "\npopPeriod=" + popPeriod +
                                    "\nexpunge=" + expunge);
    javaMailUtil = new JavaMailUtil();
  }
}
