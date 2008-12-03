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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.objectweb.joram.shared.messages.ConversionHelper;
import org.objectweb.joram.shared.messages.Message;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.util.Debug;

/**
 * The <code>JavaMailUtil</code> class giv utils to get, send, ...
 * mail.
 */
public class JavaMailUtil {
  public static Logger logger = Debug.getLogger(JavaMailUtil.class.getName());
  
  private Store store = null;
  private Folder folder = null;

  public void sendJavaMail(SenderInfo si, MailMessage message)
    throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "--- " + this + " sendJavaMail(" + si + "," + message + ")");
    
    if (si.smtpServer == null || si.smtpServer.length() < 0) {
      logger.log(BasicLevel.ERROR, 
                 "--- " + this + " sendJavaMail : smtpServer is empty.");
      throw new Exception("sendJavaMail : smtpServer is empty.");
    }
    
    
    Properties props = System.getProperties();
    props.put("mail.smtp.host", si.smtpServer);
    Session session = Session.getDefaultInstance(props);
    MimeMessage msg = new MimeMessage(session);
    MimeMultipart mimeMultiPart = new MimeMultipart();
    MimeBodyPart mimeBodyPart = new MimeBodyPart();
    
    msg.setFrom(new InternetAddress(si.from));
    
    if (si.to != null) {
      StringTokenizer st = new StringTokenizer(si.to,",");
      while (st.hasMoreTokens()) {
        msg.setRecipients(javax.mail.Message.RecipientType.TO,
                          InternetAddress.parse(st.nextToken(),false));
      }
    } else {
      logger.log(BasicLevel.ERROR, 
                 "--- " + this + " sendJavaMail : to is null.");
      throw new Exception("sendJavaMail : to is null.");
    }
    
    if (si.cc != null) {
      StringTokenizer st = new StringTokenizer(si.cc,",");
      while (st.hasMoreTokens()) {
        msg.setRecipients(javax.mail.Message.RecipientType.CC
                          ,InternetAddress.parse(st.nextToken(),false));
      }
    }
    
    if (si.bcc != null) {
      StringTokenizer st = new StringTokenizer(si.bcc,",");
      while (st.hasMoreTokens()) {
        msg.setRecipients(javax.mail.Message.RecipientType.BCC
                          ,InternetAddress.parse(st.nextToken(),false));
      }
    }
    
    msg.setSubject(si.subject);
    
    if (ConversionHelper.toBoolean(message.getProperty("showProperties"))) {
      try {
        mimeMultiPart.addBodyPart(getMultipartProp(message));
      } catch (Exception exc) {
        logger.log(BasicLevel.WARN,
                   "--- " + this + " sendJavaMail: setMultipartProp", exc);
      }
    }
    
    if (message.getType() == Message.TEXT) {
      mimeBodyPart.setText("JoramMessage :\n" + message.getText());
      mimeMultiPart.addBodyPart(mimeBodyPart);
    } else {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG,"not yet implemented");
      //message.getBytes()
    }
    
    msg.setHeader("X-Mailer", "JORAM:JavaMailUtil");
    msg.setSentDate(new Date());
    
    msg.setContent(mimeMultiPart);
    Transport.send(msg);
  }


  private MimeBodyPart getMultipartProp(MailMessage msg) throws Exception {
    MimeBodyPart mbp = new MimeBodyPart();

    StringBuffer buf = new StringBuffer();
    buf.append("type=" + msg.getType() + "\n");
    buf.append("id=" + msg.getIdentifier() + "\n");
    buf.append("persistent=" + msg.getPersistent() + "\n");
    buf.append("priority=" + msg.getJMSPriority() + "\n");
    buf.append("expiration=" + msg.getJMSExpiration() + "\n");
    buf.append("timestamp=" + msg.getTimestamp() + "\n");
    buf.append("toId=" + msg.getDestinationId() + "\n");
    buf.append("destType=" + msg.getToType() + "\n");
    buf.append("correlationId=" + msg.getCorrelationId() + "\n");
    buf.append("replyToId=" + msg.getReplyToId() + "\n");
    buf.append("replyDestType=" + msg.replyToType() + "\n");
    buf.append("deliveryCount=" + msg.getDeliveryCount() + "\n");
    buf.append("denied=" + msg.getDenied() + "\n");
    
    if (msg.getProperty("JMS_JORAM_ERRORCOUNT") != null) {
      int errorCount = ((Integer) msg.getProperty("JMS_JORAM_ERRORCOUNT")).intValue();
      buf.append("errorCount=" + errorCount + "\n");
      for (int i = 1; i <= errorCount; i++) {
        buf.append("errorCode" + i + "=" + msg.getProperty("JMS_JORAM_ERRORCODE_" + i) + "\n");
        buf.append("errorCause" + i + "=" + msg.getProperty("JMS_JORAM_ERRORCAUSE_" + i) + "\n");
      }
    }
 
    mbp.setText(buf.toString());
    return mbp;
  }

  public synchronized javax.mail.Message[] popMail(String popServer, 
                                                   String popUser, 
                                                   String popPassword,
                                                   boolean expunge) {
    
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "--- " + this + " popMail : " + "popServer=" + popServer +
                 ", popUser=" + popUser +  ", expunge=" + expunge);

    javax.mail.Message[] msgs = null;

    try {
      Properties props = System.getProperties();
      Session session = Session.getDefaultInstance(props, null);
      
      store = session.getStore("pop3");
      store.connect(popServer, popUser, popPassword);
      
      folder = store.getDefaultFolder();
      if (folder == null) throw new Exception("No default folder");

      folder = folder.getFolder("INBOX");
      if (folder == null) throw new Exception("No POP3 INBOX");

      if (expunge) {
        folder.open(Folder.READ_WRITE);
        msgs = folder.getMessages();
      } else {
        folder.open(Folder.READ_ONLY);
        msgs = folder.getMessages();
      }
      return msgs;
    } catch (Exception exc) {
      logger.log(BasicLevel.ERROR, "JavaMailUtil.popMail", exc);
      return msgs;
    }
  }

  public synchronized void closeFolder(Vector msgs, boolean expunge) {
    try {
      if (expunge && (msgs != null)) {
        for (Enumeration elements = msgs.elements();
             elements.hasMoreElements();) {
          javax.mail.Message msg = 
            (javax.mail.Message) elements.nextElement();
          msg.setFlag(Flags.Flag.DELETED,true);
        }
      }
      if (folder != null) folder.close(expunge);
      if (store != null) store.close();
    } catch (Exception exc) {
      logger.log(BasicLevel.ERROR, "JavaMailUtil.closeFolder", exc);
    } 
  }

  protected MailMessage createMessage(Properties prop, 
                                      String mailId,
                                      String destType,
                                      String toId,
                                      String replyDestType) 
    throws Exception {
    MailMessage msg = new MailMessage();
    
    msg.setIdentifier(mailId);
    msg.setPersistent(ConversionHelper.toBoolean(prop.getProperty("persistent","true")));
    msg.setPriority(ConversionHelper.toInt(prop.getProperty("priority","4")));
    msg.setExpiration(ConversionHelper.toLong(prop.getProperty("expiration","0")));
    if (prop.containsKey("timestamp"))
      msg.setTimestamp(ConversionHelper.toLong(prop.getProperty("timestamp")));
    msg.setDestination(prop.getProperty("toId",toId),
                       prop.getProperty("destType",destType));
    if (prop.containsKey("correlationId"))
      msg.setCorrelationId(prop.getProperty("correlationId"));
    if (prop.containsKey("replyToId"))
      msg.setReplyTo(prop.getProperty("replyToId"),
                     prop.getProperty("replyDestType",replyDestType));
    msg.setDeliveryCount(ConversionHelper.toInt(prop.getProperty("deliveryCount","0")));
    msg.setDenied(ConversionHelper.toBoolean(prop.getProperty("denied","false")));
    
    if (prop.containsKey("errorCount")) {
      int errorCount = ConversionHelper.toInt(prop.getProperty("errorCount"));
      msg.setProperty("JMS_JORAM_ERRORCOUNT", new Integer(errorCount));
      for (int i = 1; i <= errorCount; i++) {
        msg.setProperty("JMS_JORAM_ERRORCODE_" + i, new Short(prop.getProperty("errorCode" + i)));
        msg.setProperty("JMS_JORAM_ERRORCAUSE_" + i, new Short(prop.getProperty("errorCause" + i)));
      }
    }
    
    msg.setText(prop.getProperty("mailMessage"));
    
    return msg;
  }

  public Properties getMOMProperties(javax.mail.Message message)
    throws Exception, MessagingException {
    Properties prop = new Properties();
    
    String subject = message.getSubject();
    prop.setProperty("subject",subject);

    Part messagePart = message;
    Object content = messagePart.getContent();
    if (content instanceof Multipart)
      messagePart=((Multipart)content).getBodyPart(0);
    
    String contentType = messagePart.getContentType();

    if (contentType.startsWith("text/plain")
        || contentType.startsWith("text/html")) {
      InputStream is = messagePart.getInputStream();
      BufferedReader reader = new BufferedReader(
        new InputStreamReader(is));

      String currentLine = reader.readLine();
      while (currentLine != null) {
        currentLine = currentLine.trim();
        if (currentLine.equalsIgnoreCase("JoramMessage")) break;
        if (currentLine.length() > 1) {
          String[] buf = currentLine.split("=");
          prop.setProperty(buf[0],buf[1]);
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG,buf[0] + "=" + buf[1]);
        }
        currentLine = reader.readLine();
      }

      StringBuffer sb = new StringBuffer();
      currentLine = reader.readLine();
      while (currentLine != null) {
        sb.append(currentLine + "\n");
        currentLine = reader.readLine();
      }
      prop.setProperty("mailMessage",sb.toString());
    }

    if (content instanceof Multipart) {
      messagePart=((Multipart)content).getBodyPart(1);
      
      contentType = messagePart.getContentType();
      
      if (contentType.startsWith("text/plain")
          || contentType.startsWith("text/html")) {
        InputStream is = messagePart.getInputStream();
        BufferedReader reader = new BufferedReader(
          new InputStreamReader(is));
        
        String currentLine = reader.readLine();
        StringBuffer sb = new StringBuffer();
        while (currentLine != null) {
          if (currentLine.equalsIgnoreCase("JoramMessage")) continue;
          sb.append(currentLine + "\n");
          currentLine = reader.readLine();
        }
        prop.setProperty("mailMessage",sb.toString());
      }
    }

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
                 "--- " + this + " getMOMProperties : prop=" + prop);
    return prop;
  }
}
