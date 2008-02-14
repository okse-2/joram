/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - 2007 ScalAgent Distributed Technologies
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
package com.scalagent.joram.mom.dest.mail;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

import org.objectweb.joram.mom.dest.Queue;
import org.objectweb.joram.mom.dest.QueueImpl;
import org.objectweb.joram.mom.notifications.ClientMessages;
import org.objectweb.joram.mom.notifications.SpecialAdminRequest;
import org.objectweb.joram.shared.admin.SpecialAdmin;
import org.objectweb.joram.shared.excepts.RequestException;
import org.objectweb.joram.shared.messages.Message;
import org.objectweb.joram.shared.selectors.Selector;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.util.Debug;

/**
 * The <code>JavaMailQueueImpl</code> class implements the MOM queue behaviour,
 * basically storing messages and delivering them upon clients requests.
 */
public class JavaMailQueueImpl extends QueueImpl implements JavaMailQueueImplMBean {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public static Logger logger = Debug.getLogger(JavaMailQueueImpl.class.getName());

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

  private Vector senderInfos = null;

  private transient JavaMailUtil javaMailUtil = null;

  /**
   * Constructs a <code>JavaMailQueueImpl</code> instance.
   *
   * @param destId  	Identifier of the agent hosting the queue.
   * @param adminId  	Identifier of the administrator of the queue.
   * @param prop	Properties to configure the queue.
   */
  public JavaMailQueueImpl(AgentId destId, 
                           AgentId adminId,
                           Properties prop) {
    super(destId, adminId, prop);
    setProperties(prop);

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "--- " + this + " JavaMailQueueImpl : " +
                 "\nsenderInfos=" + senderInfos +
                 "\npopServer=" + popServer +
                 "\npopUser=" + popUser +
                 "\npopPeriod=" + popPeriod +
                 "\nexpunge=" + expunge);
  }

  public void setProperties(Properties prop) {
    smtpServer = prop.getProperty("smtpServer", smtpServer);
    to = prop.getProperty("to", to);
    cc = prop.getProperty("cc", cc);
    bcc = prop.getProperty("bcc", bcc);
    from = prop.getProperty("from", from);
    subject = prop.getProperty("subject", subject);
    selector = prop.getProperty("selector", selector);

    // to send mail
    senderInfos = new Vector();
    senderInfos.add(new SenderInfo(smtpServer,
                                   to, cc, bcc, from, subject,
                                   selector));

    try {
      popPeriod = Long.valueOf(prop.getProperty("popPeriod")).longValue();
    } catch (NumberFormatException exc) {
      popPeriod = -1;
    }
    popServer = prop.getProperty("popServer", popServer);
    popUser = prop.getProperty("popUser", popUser);
    popPassword = prop.getProperty("popPassword", popPassword);
    expunge = Boolean.valueOf(prop.getProperty("expunge")).booleanValue();
  }

  // ==================================================
  // MBean interface
  // ==================================================
  /**
   * Returns the default SMTP server for outgoing mail.
   *
   * @return the default SMTP server.
   */
  public String getSMTPServer() {
    return smtpServer;
  }

  /**
   * Sets or unsets the default SMTP server for outgoing mail.
   *
   * @param period The default SMTP server or null for unsetting
   *               previous value.
   */
  public void setSMTPServer(String smtpServer) {
    this.smtpServer = smtpServer;
  }

  /**
   * Returns the default <code>to</code> field.
   *
   * @return the default <code>to</code> field.
   */
  public String getDefaultTo() {
    return to;
  }

  /**
   * Sets or unsets the default <code>to</code> field.
   *
   * @param to 	The default <code>to</code> field or null for unsetting
   *            previous value.
   */
  public void setDefaultTo(String to) {
    this.to = to;
  }

  /**
   * Returns the default <code>cc</code> field.
   *
   * @return the default <code>cc</code> field.
   */
  public String getDefaultCC() {
    return cc;
  }

  /**
   * Sets or unsets the default <code>cc</code> field.
   *
   * @param cc 	The default <code>cc</code> field or null for unsetting
   *            previous value.
   */
  public void setDefaultCC(String cc) {
    this.cc = cc;
  }

  /**
   * Returns the default <code>bcc</code> field.
   *
   * @return the default <code>bcc</code> field.
   */
  public String getDefaultBcc() {
    return bcc;
  }

  /**
   * Sets or unsets the default <code>bcc</code> field.
   *
   * @param bcc The default <code>bcc</code> field or null for unsetting
   *            previous value.
   */
  public void setDefaultBcc(String bcc) {
    this.bcc = bcc;
  }

  /**
   * Returns the default <code>from</code> field.
   *
   * @return the default <code>from</code> field.
   */
  public String getDefaultFrom() {
    return from;
  }

  /**
   * Sets or unsets the default <code>from</code> field.
   *
   * @param from The default <code>from</code> field or null for unsetting
   *             previous value.
   */
  public void setDefaultFrom(String from) {
    this.from = from;
  }

  /**
   * Returns the default <code>subject</code> field.
   *
   * @return the default <code>subject</code> field.
   */
  public String getDefaultSubject() {
    return subject;
  }

  /**
   * Sets or unsets the default <code>subject</code> field.
   *
   * @param subject 	The default <code>subject</code> field or null for
   *             	unsetting previous value.
   */
  public void setDefaultSubject(String subject) {
    this.subject = subject;
  }

  /**
   * Returns the default <code>selector</code>.
   *
   * @return the default <code>selector</code>.
   */
  public String getDefaultSelector() {
    return selector;
  }

  /**
   * Sets or unsets the default <code>selector</code>.
   *
   * @param selector 	The default <code>selector</code> or null for
   *             	unsetting previous value.
   */
  public void setDefaultSelector(String selector) {
    this.selector = selector;
  }

  /**
   * Returns the period value to collect mail, -1 if not set.
   *
   * @return the period value to collect mail; -1 if not set.
   */
  public long getPopPeriod() {
    return popPeriod;
  }

  /**
   * Sets or unsets the period value to collect mail.
   *
   * @param period The period value to collect mail or -1 for unsetting
   *               previous value.
   */
  public void setPopPeriod(long period) {
    popPeriod = period;
  }

  /**
   * Returns the pop server for incoming mail.
   *
   * @return the pop server for incoming mail.
   */
  public String getPopServer() {
    return popServer;
  }

  /**
   * Sets or unsets the pop server for incoming mail.
   *
   * @param server 	The pop server or null for unsetting previous value.
   */
  public void setPopServer(String server) {
    this.popServer = server;
  }

  /**
   * Returns the username for pop account.
   *
   * @return the username for pop account.
   */
  public String getPopUser() {
    return popUser;
  }

  /**
   * Sets or unsets the username for pop account.
   *
   * @param user	The username for pop account or null for
   *			unsetting previous value.
   */
  public void setPopUser(String user) {
    this.popUser = user;
  }

  /**
   * Returns the password for pop account.
   *
   * @return the password for pop account.
   */
  public String getPopPassword() {
    return popPassword;
  }

  /**
   * Sets or unsets the password for pop account.
   *
   * @param pass	The password for pop account or null for
   *			unsetting previous value.
   */
  public void setPopPassword(String pass) {
    this.popPassword = pass;
  }

  /**
   * Returns  the default <code>expunge</code> field.
   *
   * @return the default <code>expunge</code> field.
   */
  public boolean getExpunge() {
    return expunge;
  }

  /**
   * Sets or unsets the default <code>expunge</code> field.
   *
   * @param expunge	The default <code>expunge</code> field or null for
   *			unsetting previous value.
   */
  public void setExpunge(boolean expunge) {
    this.expunge = expunge;
  }

  // ==================================================

  public String toString() {
    return "JavaMailQueueImpl:" + destId.toString();
  }

  protected Object specialAdminProcess(SpecialAdminRequest not) 
    throws RequestException {

    try {
      SpecialAdmin req = not.getRequest();
      
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, 
                   "--- " + this + " specialAdminProcess : " + req);
      if (req instanceof AddSenderInfo)
        addSenderInfo(((AddSenderInfo) req).si,
                      ((AddSenderInfo) req).index);
      else if (req instanceof RemoveSenderInfo) {
        if (((RemoveSenderInfo) req).index > -1)
          removeSenderInfo(((RemoveSenderInfo) req).index);
        else 
          removeSenderInfo(((RemoveSenderInfo) req).si);
      }  else if (req instanceof UpdateSenderInfo)
        updateSenderInfo(((UpdateSenderInfo) req).oldSi,
                         ((UpdateSenderInfo) req).newSi);
      
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, 
                   "--- " + this + " specialAdminProcess senderInfos=" + senderInfos);
    } catch (Exception exc) {
      if (logger.isLoggable(BasicLevel.WARN))
        logger.log(BasicLevel.WARN, 
                   "--- " + this + " specialAdminProcess", exc);
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

  protected boolean updateSenderInfo(SenderInfo oldSi, SenderInfo newSi)
    throws ArrayIndexOutOfBoundsException {
    int index = senderInfos.indexOf(oldSi);
    if (index > -1) return false;
    senderInfos.set(index,newSi);
    return true;
  }

  public ClientMessages preProcess(AgentId from, ClientMessages not) {
    for (Enumeration msgs = not.getMessages().elements();
         msgs.hasMoreElements();) {
      Message msg = (Message) msgs.nextElement();
      SenderInfo si = match(msg);

      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, 
                   "--- " + this + " match=" + (si!=null));
      if (si != null) {
        try {
          javaMailUtil.sendJavaMail(si, new MailMessage(msg));
        } catch (Exception exc) {
          ClientMessages deadM = 
            new ClientMessages(not.getClientContext(), 
                               not.getRequestId());
          
          deadM.addMessage(msg);
          sendToDMQ(deadM,not.getDMQId());
          
          logger.log(BasicLevel.WARN,
                     "JavaMailQueueImpl.sendJavaMail",  exc);
        }
        not.getMessages().remove(msg);
      }
    }
    if (not.getMessages().size() > 0) {
      return not;
    }
    return null;
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
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, 
                     "--- " + this + " doPop : msgs[" + i + "] = " + msgs[i]);
        try {
          count++;
          Properties prop = javaMailUtil.getMOMProperties(msgs[i]);
          MailMessage m = 
            javaMailUtil.createMessage(prop,
                                       destId.toString()+"mail_"+count,
                                       Queue.getDestinationType(),
                                       destId.toString(),
                                       Queue.getDestinationType());
          storeMessage(new org.objectweb.joram.mom.messages.Message(m.getSharedMessage()));

          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, 
                       "--- " + this + " doPop : storeMessage m = " + m);
          if (expunge)
            toExpunge.add(msgs[i]);
        } catch (Exception exc) {
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, 
                       "--- " + this + " doPop", exc);
          continue;
        }
      }
      deliverMessages(0);
    }

    javaMailUtil.closeFolder(toExpunge,expunge);
    toExpunge.clear();
  }

  private void readObject(java.io.ObjectInputStream in)
    throws IOException, ClassNotFoundException {

    in.defaultReadObject();

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "--- " + this + " JavaMailQueueImpl.readObject : " +
                 "\nsenderInfos=" + senderInfos +
                 "\npopServer=" + popServer +
                 "\npopUser=" + popUser +
                 "\npopPeriod=" + popPeriod +
                 "\nexpunge=" + expunge);
    javaMailUtil = new JavaMailUtil();
  }
}
